package bob.growingmdal.service;

import bob.growingmdal.adapter.DekaReaderAdapter;
import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.config.WebSocketSessionManager;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.entity.OperationResultEvent;
import bob.growingmdal.entity.baseinfo.DomesticIDCard;
import bob.growingmdal.entity.baseinfo.ForeignIDCard;
import bob.growingmdal.hardware.LifecycleManaged;
import org.springframework.boot.actuate.health.Health;
import bob.growingmdal.util.Base64Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class DekaService extends AnnotationDrivenHandler implements LifecycleManaged {

    private final ApplicationEventPublisher eventPublisher;
    private final AtomicBoolean isCheckingCard = new AtomicBoolean(false);
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final DekaReaderAdapter dekaReaderAdapter;
    private final String workDir;

    @Value("${deka.reader.wait.time}")
    private int timeout;

    @Value("${deka.reader.loop.period}")
    private int interval = 2000;

    @Autowired
    public DekaService(ApplicationEventPublisher eventPublisher,
                       WebSocketSessionManager sessionManager,
                       ObjectMapper objectMapper) {
        this(eventPublisher, sessionManager, objectMapper, DekaReaderAdapter.load());
    }

    DekaService(ApplicationEventPublisher eventPublisher,
                WebSocketSessionManager sessionManager,
                ObjectMapper objectMapper,
                DekaReaderAdapter dekaReaderAdapter) {
        this.eventPublisher = eventPublisher;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.dekaReaderAdapter = dekaReaderAdapter;
        this.workDir = System.getProperty("user.dir") + File.separator
                + "src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "lib" + File.separator
                + "deka_T10-MX4_x64";
    }

    public void performOperation(DeviceCommand command) {
        eventPublisher.publishEvent(new OperationResultEvent(command.getSession(), command.toString()));
    }

    @Override
    public String getDeviceType() {
        return "IDCard";
    }

    private int initDevice(DekaDeviceContext ctx) {
        int handle = dekaReaderAdapter.dc_init(DekaReaderAdapter.PORT_USB, DekaReaderAdapter.BAUD);
        ctx.setHandle(handle);
        setWorkDir(workDir);
        if (handle >= 0) {
            dekaReaderAdapter.dc_beep(handle, (short) 10);
        }
        return handle;
    }

    private boolean setWorkDir(String dir) {
        try {
            dekaReaderAdapter.LibMain(1, DekaReaderAdapter.string_to_gbk_bytes(dir));
            log.debug("Set deka workdir success");
        } catch (UnsupportedEncodingException e) {
            log.error("Set deka workdir failed: {}", e.getMessage());
            return false;
        }
        return true;
    }

    private boolean exitDevice(DekaDeviceContext ctx) {
        int handle = ctx.getHandle();
        if (handle < 0) {
            return true;
        }
        int status = dekaReaderAdapter.dc_exit(handle);
        ctx.setHandle(-1);
        ctx.setStatus(status);
        if (!DekaReaderAdapter.isSuccess(status)) {
            log.error("Exit device failed. status = {}", status);
            return false;
        }
        log.debug("Exit device success. status = {}", status);
        return true;
    }

    /**
     * 获取身份证信息
     *
     * @return 身份证实体类或错误信息字符串
     */
    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "getIDCardInfo")
    public Object getIDCardInfo(DeviceCommand command) {
        if (!isCheckingCard.compareAndSet(false, true)) {
            log.warn("Another ID card check is already in progress");
            return "Another ID card check is already in progress";
        }

        DekaDeviceContext ctx = new DekaDeviceContext();
        Path tmpPhoto = null;
        try {
            int handle = initDevice(ctx);
            if (handle < 0) {
                log.error("Init deka readcard device failed. handle = {}", handle);
                return String.format("init deka readcard device failed. handle = %s", handle);
            }

            boolean insertStatus = waitForCardInsertion(command, ctx);
            if (!insertStatus) {
                return "timeout: id card is not inserting.";
            }

            return readCardInfo(ctx, command);
        } catch (Exception e) {
            log.error("Unexpected error during ID card read", e);
            return "error: " + e.getMessage();
        } finally {
            exitDevice(ctx);
            deleteTempPhoto(tmpPhoto);
            isCheckingCard.set(false);
        }
    }

    private boolean waitForCardInsertion(DeviceCommand command, DekaDeviceContext ctx) {
        int loopCount = Math.max(1, timeout / interval);
        for (int i = 0; i < loopCount; i++) {
            if (!isCheckingCard.get()) {
                log.info("ID card check was cancelled");
                return false;
            }
            boolean insertStatus = idCardExists(ctx);
            if (insertStatus) {
                log.debug("ID card inserted");
                command.setTransferData("id card inserted.");
                return true;
            }
            log.debug("Waiting for ID card insertion");
            command.setTransferData("waiting for id card inserting." + i);
            performOperation(command);
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("ID card check interrupted");
                return false;
            }
        }
        return false;
    }

    private Object readCardInfo(DekaDeviceContext ctx, DeviceCommand command)
            throws UnsupportedEncodingException {
        int handle = ctx.getHandle();

        int[] textLen = new int[1];
        byte[] text = new byte[1024];
        int[] photoLen = new int[1];
        byte[] photo = new byte[1024];
        int[] fingerprintLen = new int[1];
        byte[] fingerprint = new byte[1024];
        int[] extraLen = new int[1];
        byte[] extra = new byte[70];

        int status = dekaReaderAdapter.dc_SamAReadCardInfo(handle, 3, textLen, text,
                photoLen, photo, fingerprintLen, fingerprint, extraLen, extra);
        if (!DekaReaderAdapter.isSuccess(status)) {
            log.error("Read ID card info failed. status = {}", status);
            return String.format("read id card info failed. status = %s", status);
        }

        int type = ((text[0] >= 'A') && (text[0] <= 'Z') && (text[1] == 0)) ? 1 : 0;
        ctx.setType(type);

        if (type == 0) {
            return readDomesticCard(ctx, textLen[0], text, photoLen[0], photo);
        } else if (type == 1) {
            return readForeignCard(ctx, textLen[0], text);
        }

        log.error("Unknown ID card type. type = {}", type);
        return "error: unknown id card type";
    }

    private Object readDomesticCard(DekaDeviceContext ctx, int textLen, byte[] text,
                                    int photoLen, byte[] photo) throws UnsupportedEncodingException {
        int handle = ctx.getHandle();
        log.info("Read domestic ID card");

        byte[] name = new byte[64];
        byte[] sex = new byte[8];
        byte[] nation = new byte[12];
        byte[] birthDay = new byte[36];
        byte[] address = new byte[144];
        byte[] idNumber = new byte[76];
        byte[] department = new byte[64];
        byte[] expireStartDay = new byte[36];
        byte[] expireEndDay = new byte[36];
        byte[] reserved = new byte[76];

        int status = dekaReaderAdapter.dc_ParseTextInfo(handle, 0, textLen, text,
                name, sex, nation, birthDay, address, idNumber, department,
                expireStartDay, expireEndDay, reserved);

        if (!DekaReaderAdapter.isSuccess(status)) {
            log.error("Parse text info failed. status = {}", status);
            return String.format("parse text info failed. status = %s", status);
        }

        DomesticIDCard card = new DomesticIDCard(
                DekaReaderAdapter.gbk_bytes_to_string(name),
                DekaReaderAdapter.gbk_bytes_to_string(sex),
                DekaReaderAdapter.gbk_bytes_to_string(nation),
                DekaReaderAdapter.gbk_bytes_to_string(birthDay),
                DekaReaderAdapter.gbk_bytes_to_string(address),
                DekaReaderAdapter.gbk_bytes_to_string(idNumber),
                DekaReaderAdapter.gbk_bytes_to_string(department),
                DekaReaderAdapter.gbk_bytes_to_string(expireStartDay),
                DekaReaderAdapter.gbk_bytes_to_string(expireEndDay),
                DekaReaderAdapter.gbk_bytes_to_string(reserved));

        Path tmpPhoto = createTempPhotoFile();
        try {
            status = dekaReaderAdapter.dc_ParsePhotoInfo(handle, 0, photoLen, photo,
                    null, DekaReaderAdapter.string_to_gbk_bytes(tmpPhoto.toString()));
            if (!DekaReaderAdapter.isSuccess(status)) {
                log.error("Parse photo info failed. status = {}", status);
                return String.format("parse photo info failed. status = %s", status);
            }
            card.setPhoto(Base64Util.convertBmpToBase64Prefix(tmpPhoto.toString()));
        } finally {
            deleteTempPhoto(tmpPhoto);
        }

        return card;
    }

    private Object readForeignCard(DekaDeviceContext ctx, int textLen, byte[] text)
            throws UnsupportedEncodingException {
        int handle = ctx.getHandle();
        log.info("Read foreign ID card");

        byte[] englishName = new byte[244];
        byte[] sex = new byte[8];
        byte[] idNumber = new byte[64];
        byte[] citizenship = new byte[16];
        byte[] chineseName = new byte[64];
        byte[] expireStartDay = new byte[36];
        byte[] expireEndDay = new byte[36];
        byte[] birthDay = new byte[36];
        byte[] versionNumber = new byte[12];
        byte[] departmentCode = new byte[20];
        byte[] typeSign = new byte[8];
        byte[] reserved = new byte[16];

        int status = dekaReaderAdapter.dc_ParseTextInfoForForeigner(handle, 0, textLen, text,
                englishName, sex, idNumber, citizenship, chineseName,
                expireStartDay, expireEndDay, birthDay, versionNumber,
                departmentCode, typeSign, reserved);
        if (!DekaReaderAdapter.isSuccess(status)) {
            log.error("Parse foreigner text info failed. status = {}", status);
            return String.format("parse Foreigner text info failed. status = %s", status);
        }

        return new ForeignIDCard(
                DekaReaderAdapter.gbk_bytes_to_string(englishName),
                DekaReaderAdapter.gbk_bytes_to_string(sex),
                DekaReaderAdapter.gbk_bytes_to_string(idNumber),
                DekaReaderAdapter.gbk_bytes_to_string(citizenship),
                DekaReaderAdapter.gbk_bytes_to_string(chineseName),
                DekaReaderAdapter.gbk_bytes_to_string(expireStartDay),
                DekaReaderAdapter.gbk_bytes_to_string(expireEndDay),
                DekaReaderAdapter.gbk_bytes_to_string(birthDay),
                DekaReaderAdapter.gbk_bytes_to_string(versionNumber),
                DekaReaderAdapter.gbk_bytes_to_string(departmentCode),
                DekaReaderAdapter.gbk_bytes_to_string(typeSign),
                DekaReaderAdapter.gbk_bytes_to_string(reserved));
    }

    private Path createTempPhotoFile() {
        try {
            Path workDirPath = Paths.get(workDir);
            if (!Files.exists(workDirPath)) {
                Files.createDirectories(workDirPath);
            }
            return Files.createTempFile(workDirPath, "deka-photo-", ".bmp");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temporary photo file", e);
        }
    }

    private void deleteTempPhoto(Path tmpPhoto) {
        if (tmpPhoto == null) {
            return;
        }
        try {
            Files.deleteIfExists(tmpPhoto);
        } catch (IOException e) {
            log.warn("Failed to delete temporary photo file: {}", tmpPhoto, e);
        }
    }

    /**
     * 读取身份证
     *
     * @return 0 成功，非0失败
     */
    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "cardExists")
    public boolean IdCardExists() {
        DekaDeviceContext ctx = new DekaDeviceContext();
        try {
            int handle = initDevice(ctx);
            if (handle < 0) {
                log.error("Init device failed for cardExists. handle = {}", handle);
                return false;
            }
            return idCardExists(ctx);
        } finally {
            exitDevice(ctx);
        }
    }

    private boolean idCardExists(DekaDeviceContext ctx) {
        int status = dekaReaderAdapter.dc_find_i_d(ctx.getHandle());
        return status == 0;
    }

    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "test")
    public Object test() {
        log.info("Invoke test success");
        return "invoke test success. ";
    }

    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "cancelCheck")
    public String cancelIdCardCheck() {
        if (isCheckingCard.compareAndSet(true, false)) {
            log.info("ID card check cancelled successfully");
            return "ID card check cancelled successfully";
        }
        return "No ID card check in progress to cancel";
    }

    @Override
    public void initialize() {
        log.info("Deka service initialized");
    }

    @Override
    public Health health() {
        DekaDeviceContext ctx = new DekaDeviceContext();
        try {
            int handle = initDevice(ctx);
            if (handle < 0) {
                return Health.down().withDetail("device", "Deka T10-MX4").withDetail("reason", "init failed").build();
            }
            return Health.up().withDetail("device", "Deka T10-MX4").build();
        } catch (Exception e) {
            return Health.down(e).withDetail("device", "Deka T10-MX4").build();
        } finally {
            exitDevice(ctx);
        }
    }

    @Override
    public void shutdown() {
        log.info("Deka service shutdown");
    }
}
