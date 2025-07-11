package bob.growingmdal.service;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.config.WebSocketSessionManager;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.entity.OperationResultEvent;
import bob.growingmdal.entity.baseinfo.DomesticIDCard;
import bob.growingmdal.entity.baseinfo.ForeignIDCard;
import bob.growingmdal.adapter.DekaReaderAdapter;
import bob.growingmdal.util.Base64Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class DekaService extends AnnotationDrivenHandler {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final AtomicBoolean isCheckingCard = new AtomicBoolean(false);
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final String workDir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "lib" + File.separator + "deka_T10-MX4_x64";
    @Value("${deka.reader.wait.time}")
    private int timeout;  // 超时时间
    @Value("${deka.reader.loop.period}")
    private int interval = 2000;  // 每次循环间隔

    DekaReaderAdapter dekaReaderAdapter = DekaReaderAdapter.load();
    int handle = -1;
    int status = -1;
    int type = 0;


    @Autowired
    public DekaService(WebSocketSessionManager sessionManager,
                       ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    public void performOperation(DeviceCommand command) {
        eventPublisher.publishEvent(new OperationResultEvent(command.getSession(), command.toString()));
    }

    @Override
    public boolean supports(DeviceCommand command) {
        return "IDCard".equals(command.getDeviceType());
    }

    private int initDevice() {
        handle = dekaReaderAdapter.dc_init(DekaReaderAdapter.PORT_USB, DekaReaderAdapter.BAUD);
        setWorkDir(workDir);
        dekaReaderAdapter.dc_beep(handle, (short) 10);
        return handle;
    }

    private int initDevice(short port, int baud) {
        handle = dekaReaderAdapter.dc_init(port, baud);
        setWorkDir(workDir);
        dekaReaderAdapter.dc_beep(handle, (short) 10);
        return handle;
    }

    private boolean setWorkDir(String dir) {
        try {
            dekaReaderAdapter.LibMain(1, DekaReaderAdapter.string_to_gbk_bytes(dir));
            log.debug("Set deka workdir success. {}", dir);
        } catch (UnsupportedEncodingException e) {
            log.error("Set deka workdir failed. {}", e.getMessage());
            return false;
        }
        return true;
    }

    private boolean exitDevice(int handle) {
        status = dekaReaderAdapter.dc_exit(handle);
        handle = -1;
        if (!DekaReaderAdapter.isSuccess(status)) {
            log.error("exit device failed . status = {}", status);
            return false;
        } else {
            log.debug("exit device success . status = {}", status);
            return true;
        }
    }

    /**
     * 获取身份证信息
     *
     * @return 身份证实体类
     */
    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "getIDCardInfo")
    public Object getIDCardInfo(DeviceCommand command) throws UnsupportedEncodingException {

        Object result = null;

        if (!isCheckingCard.compareAndSet(false, true)) {
            log.warn("Another ID card check is already in progress");
            return "Another ID card check is already in progress";
        }

        // 初始化变量
        int[] text_len = new int[1];    // 身份证信息长度
        byte[] text = new byte[1024];   // 身份证信息
        int[] photo_len = new int[1];   // 照片信息长度
        byte[] photo = new byte[1024];  // 照片信息
        int[] fingerprint_len = new int[1]; // 指纹信息长度
        byte[] fingerprint = new byte[1024]; // 指纹信息
        int[] extra_len = new int[1];
        byte[] extra = new byte[70];

        handle = initDevice();
//        if (handle < 0) {
//            log.error("init deka readcard device failed . handle = {}", handle);
//            return String.format("init deka readcard device failed . handle = %s", handle);
//        }

        // read id card inserted status
        boolean insertStatus = false;
        for (int i = 0; i < timeout / interval; i++) {
            if (!isCheckingCard.get()) {
                log.info("ID card check was cancelled");
                return "ID card check was cancelled";
            }
            insertStatus = IdCardExists();
            if (insertStatus) {
                log.debug("id card inserted.");
                command.setTransferData("id card inserted.");
                break;
            } else {
                log.debug("waiting for id card inserted.");
                command.setTransferData("waiting for id card inserting." + i);
                performOperation(command);
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                isCheckingCard.set(false);
                return "error: id card is not inserting. " + e.getMessage();
            }
        }
        if (!insertStatus) {
            isCheckingCard.set(false);
            return "timeout : id card is not inserting. ";
        }
        isCheckingCard.set(false);


        // read id card info
        status = dekaReaderAdapter.dc_SamAReadCardInfo(handle, 3, text_len, text, photo_len, photo, fingerprint_len, fingerprint, extra_len, extra);
        if (!DekaReaderAdapter.isSuccess(status)) {
            log.error("read id card info failed . status = {} , exit device ...", status);
            status = dekaReaderAdapter.dc_exit(handle);
            return String.format("read id card info failed . status = %s, exit device ...", status);
        }
        if ((text[0] >= 'A') && (text[0] <= 'Z') && (text[1] == 0))
            type = 1;

        // 国内身份证
        if (type == 0) {
            log.info("read domestic id card .");
            byte[] name = new byte[64];
            byte[] sex = new byte[8];
            byte[] nation = new byte[12];
            byte[] birth_day = new byte[36];
            byte[] address = new byte[144];
            byte[] id_number = new byte[76];
            byte[] department = new byte[64];
            byte[] expire_start_day = new byte[36];
            byte[] expire_end_day = new byte[36];
            byte[] reserved = new byte[76];
            status = dekaReaderAdapter.dc_ParseTextInfo(handle, 0, text_len[0], text, name, sex, nation, birth_day, address, id_number, department, expire_start_day, expire_end_day, reserved);

            // debug
            log.debug("text: {}", DekaReaderAdapter.gbk_bytes_to_string(text));
            log.debug("name: {}", DekaReaderAdapter.gbk_bytes_to_string(name));
            log.debug("sex: {}", DekaReaderAdapter.gbk_bytes_to_string(sex));
            log.debug("nation: {}", DekaReaderAdapter.gbk_bytes_to_string(nation));
            log.debug("birth_day: {}", DekaReaderAdapter.gbk_bytes_to_string(birth_day));
            log.debug("address: {}", DekaReaderAdapter.gbk_bytes_to_string(address));
            log.debug("id_number: {}", DekaReaderAdapter.gbk_bytes_to_string(id_number));
            log.debug("department: {}", DekaReaderAdapter.gbk_bytes_to_string(department));
            log.debug("expire_start_day: {}", DekaReaderAdapter.gbk_bytes_to_string(expire_start_day));
            log.debug("expire_end_day: {}", DekaReaderAdapter.gbk_bytes_to_string(expire_end_day));

            if (!DekaReaderAdapter.isSuccess(status)) {
                log.error("parse text info failed . status = {}", status);
                status = dekaReaderAdapter.dc_exit(handle);
                return String.format("parse text info failed . status = %s, exit device ...", status);
            }
            DomesticIDCard domesticIDCard = new DomesticIDCard(DekaReaderAdapter.gbk_bytes_to_string(name),
                    DekaReaderAdapter.gbk_bytes_to_string(sex),
                    DekaReaderAdapter.gbk_bytes_to_string(nation),
                    DekaReaderAdapter.gbk_bytes_to_string(birth_day),
                    DekaReaderAdapter.gbk_bytes_to_string(address),
                    DekaReaderAdapter.gbk_bytes_to_string(id_number),
                    DekaReaderAdapter.gbk_bytes_to_string(department),
                    DekaReaderAdapter.gbk_bytes_to_string(expire_start_day),
                    DekaReaderAdapter.gbk_bytes_to_string(expire_end_day),
                    DekaReaderAdapter.gbk_bytes_to_string(reserved));

            log.info("domestic id card info: {}", domesticIDCard.toJson().toString());

            // 转换照片信息
            status = dekaReaderAdapter.dc_ParsePhotoInfo(handle, 0, photo_len[0], photo, null, DekaReaderAdapter.string_to_gbk_bytes("tmp.bmp"));
            if (!DekaReaderAdapter.isSuccess(status)) {
                log.error("parse photo info failed . status = {}", status);
                status = dekaReaderAdapter.dc_exit(handle);
                return String.format("parse photo info failed . status = %s, exit device ...", status);
            }
            domesticIDCard.setPhoto(Base64Util.convertBmpToBase64Prefix(workDir + File.separator + "tmp.bmp"));

            result = domesticIDCard;
        } else if (type == 1) {
            log.info("read foreign id card .");
            byte[] english_name = new byte[244];
            byte[] sex = new byte[8];
            byte[] id_number = new byte[64];
            byte[] citizenship = new byte[16];
            byte[] chinese_name = new byte[64];
            byte[] expire_start_day = new byte[36];
            byte[] expire_end_day = new byte[36];
            byte[] birth_day = new byte[36];
            byte[] version_number = new byte[12];
            byte[] department_code = new byte[20];
            byte[] type_sign = new byte[8];
            byte[] reserved = new byte[16];
            status = dekaReaderAdapter.dc_ParseTextInfoForForeigner(handle, 0, text_len[0], text, english_name, sex, id_number, citizenship, chinese_name, expire_start_day, expire_end_day, birth_day, version_number, department_code, type_sign, reserved);
            if (!DekaReaderAdapter.isSuccess(status)) {
                log.error("parse Foreigner text info failed . status = {}", status);
                status = dekaReaderAdapter.dc_exit(handle);
                return String.format("parse Foreigner text info failed . status = %s, exit device ...", status);
            }
            ForeignIDCard foreignIDCard = new ForeignIDCard(DekaReaderAdapter.gbk_bytes_to_string(english_name),
                    DekaReaderAdapter.gbk_bytes_to_string(sex),
                    DekaReaderAdapter.gbk_bytes_to_string(id_number),
                    DekaReaderAdapter.gbk_bytes_to_string(citizenship),
                    DekaReaderAdapter.gbk_bytes_to_string(chinese_name),
                    DekaReaderAdapter.gbk_bytes_to_string(expire_start_day),
                    DekaReaderAdapter.gbk_bytes_to_string(expire_end_day),
                    DekaReaderAdapter.gbk_bytes_to_string(birth_day),
                    DekaReaderAdapter.gbk_bytes_to_string(version_number),
                    DekaReaderAdapter.gbk_bytes_to_string(department_code),
                    DekaReaderAdapter.gbk_bytes_to_string(type_sign),
                    DekaReaderAdapter.gbk_bytes_to_string(reserved));
            log.info("foreigner id card info: {}", foreignIDCard.toJson().toString());
            result = foreignIDCard;
        } else {
            log.error("unknown id card type . type = {}", type);
        }


        exitDevice(handle);
        return result;

    }

    /**
     * 读取身份证
     *
     * @return 0 成功，非0失败
     */
    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "cardExists")
    public boolean IdCardExists() {
        // 判断是否插卡
        status = dekaReaderAdapter.dc_find_i_d(handle);
        return status == 0;
    }

    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "test")
    public Object test() {
        log.info("invoke test success. ");
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


}
