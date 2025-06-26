package bob.growingmdal.service;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.entity.OperationResultEvent;
import bob.growingmdal.entity.baseinfo.DomesticIDCard;
import bob.growingmdal.entity.baseinfo.ForeignIDCard;
import bob.growingmdal.adapter.DekaReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DekaService extends AnnotationDrivenHandler {

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final int timeout = 60000;  // 超时时间
    private final int interval = 2000;  // 每次循环间隔

    DekaReader dekaReader = DekaReader.load();
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
        handle = dekaReader.dc_init(DekaReader.PORT_USB, DekaReader.BAUD);
        dekaReader.dc_beep(handle,(short)10);
        return handle;
    }

    private boolean exitDevice(int handle) {
        status = dekaReader.dc_exit(handle);
        handle = -1;
        if (!DekaReader.isSuccess(status)) {
            log.error("exit device failed . status = {}", status);
            return false;
        } else {
            log.debug("exit device success . status = {}", status);
            return true;
        }
    }

    /**
     * 获取身份证信息
     * @return 身份证实体类
     */
    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "getIDCardInfo")
    public Object getIDCardInfo(DeviceCommand command) {
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
        if (handle < 0) {
            log.error("init deka readcard device failed . handle = {}", handle);
            return String.format("init deka readcard device failed . handle = %s", handle);
        }

        // read id card inserted status
        boolean insertStatus = false;
        for(int i = 0; i < timeout / interval; i++){
            insertStatus = IdCardExists();
            if (insertStatus) {
                log.debug("id card inserted.");
                command.setTransferData("id card inserted.");
                performOperation( command );
                break;
            }else{
                log.debug("waiting for id card inserted.");
                command.setTransferData("id card not inserting.");
                performOperation( command );
            }
        }
        if(!insertStatus){
            return "timeout : id card is not inserting. ";
        }



        // read id card info
        status = dekaReader.dc_SamAReadCardInfo(handle, 3, text_len, text, photo_len, photo, fingerprint_len, fingerprint, extra_len, extra);
        if (!DekaReader.isSuccess(status)) {
            log.error("read id card info failed . status = {} , exit device ...", status);
            status = dekaReader.dc_exit(handle);
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
            status = dekaReader.dc_ParseTextInfo(handle, 0, text_len[0], text, name, sex, nation, birth_day, address, id_number, department, expire_start_day, expire_end_day, reserved);

            // debug
            log.debug("text: {}", DekaReader.gbk_bytes_to_string(text));
            log.debug("name: {}", DekaReader.gbk_bytes_to_string(name));
            log.debug("sex: {}", DekaReader.gbk_bytes_to_string(sex));
            log.debug("nation: {}", DekaReader.gbk_bytes_to_string(nation));
            log.debug("birth_day: {}", DekaReader.gbk_bytes_to_string(birth_day));
            log.debug("address: {}", DekaReader.gbk_bytes_to_string(address));
            log.debug("id_number: {}", DekaReader.gbk_bytes_to_string(id_number));
            log.debug("department: {}", DekaReader.gbk_bytes_to_string(department));
            log.debug("expire_start_day: {}", DekaReader.gbk_bytes_to_string(expire_start_day));
            log.debug("expire_end_day: {}", DekaReader.gbk_bytes_to_string(expire_end_day));

            if(!DekaReader.isSuccess(status)){
                log.error("parse text info failed . status = {}", status);
                status = dekaReader.dc_exit(handle);
                return String.format("parse text info failed . status = %s, exit device ...", status);
            }
            DomesticIDCard domesticIDCard = new DomesticIDCard(DekaReader.gbk_bytes_to_string(name),
                    DekaReader.gbk_bytes_to_string(sex),
                    DekaReader.gbk_bytes_to_string(nation),
                    DekaReader.gbk_bytes_to_string(birth_day),
                    DekaReader.gbk_bytes_to_string(address),
                    DekaReader.gbk_bytes_to_string(id_number),
                    DekaReader.gbk_bytes_to_string(department),
                    DekaReader.gbk_bytes_to_string(expire_start_day),
                    DekaReader.gbk_bytes_to_string(expire_end_day),
                    DekaReader.gbk_bytes_to_string(reserved));

            log.info("domestic id card info: {}", domesticIDCard.toJson().toString());
            exitDevice(handle);
            return domesticIDCard;
        }else if (type == 1) {
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
            status = dekaReader.dc_ParseTextInfoForForeigner(handle, 0, text_len[0], text, english_name, sex, id_number, citizenship, chinese_name, expire_start_day, expire_end_day, birth_day, version_number, department_code, type_sign, reserved);
            if(!DekaReader.isSuccess(status)){
                log.error("parse Foreigner text info failed . status = {}", status);
                status = dekaReader.dc_exit(handle);
                return String.format("parse Foreigner text info failed . status = %s, exit device ...", status);
            }
            ForeignIDCard foreignIDCard = new ForeignIDCard(DekaReader.gbk_bytes_to_string(english_name),
                    DekaReader.gbk_bytes_to_string(sex),
                    DekaReader.gbk_bytes_to_string(id_number),
                    DekaReader.gbk_bytes_to_string(citizenship),
                    DekaReader.gbk_bytes_to_string(chinese_name),
                    DekaReader.gbk_bytes_to_string(expire_start_day),
                    DekaReader.gbk_bytes_to_string(expire_end_day),
                    DekaReader.gbk_bytes_to_string(birth_day),
                    DekaReader.gbk_bytes_to_string(version_number),
                    DekaReader.gbk_bytes_to_string(department_code),
                    DekaReader.gbk_bytes_to_string(type_sign),
                    DekaReader.gbk_bytes_to_string(reserved));
            log.info("foreigner id card info: {}", foreignIDCard.toJson().toString());
            exitDevice(handle);
            return foreignIDCard;
        }else{
            log.error("unknown id card type . type = {}", type);
        }
        exitDevice(handle);
        return null;

    }

    /**
     * 读取身份证
     * @return 0 成功，非0失败
     */
    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "cardExists")
    public boolean IdCardExists() {
        // 判断是否插卡
        status = dekaReader.dc_find_i_d(handle);
        return status == 0;
    }

    @DeviceOperation(DeviceType = "IDCard", ProcessCommand = "test")
    public Object test(){
        log.info("invoke test success. ");
        return "invoke test success. ";
    }



}
