package bob.growingmdal.service;

import bob.growingmdal.entity.baseinfo.DomesticIDCard;
import bob.growingmdal.entity.baseinfo.ForeignIDCard;
import bob.growingmdal.entity.baseinfo.IDCard;
import bob.growingmdal.util.reader.DekaReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DekaService {

    DekaReader dekaReader = DekaReader.load();
    int handle = -1;
    int status = -1;
    int type = 0;

    // 初始化变量
    int[] text_len = new int[1];    // 身份证信息长度
    byte[] text = new byte[1024];   // 身份证信息
    int[] photo_len = new int[1];   // 照片信息长度
    byte[] photo = new byte[1024];  // 照片信息
    int[] fingerprint_len = new int[1]; // 指纹信息长度
    byte[] fingerprint = new byte[1024]; // 指纹信息
    int[] extra_len = new int[1];
    byte[] extra = new byte[1024];


    public int initDevice() {
        return dekaReader.dc_init(DekaReader.PORT_USB, DekaReader.BAUD);
    }

    public boolean exitDevice(int handle) {
        status = dekaReader.dc_exit(handle);
        if (!DekaReader.isSuccess(status)) {
            log.error("exit device failed . status = {}", status);
            return false;
        } else {
            log.info("exit device success . status = {}", status);
            return true;
        }


    }

    public Object getIDCardInfo() {

        handle = initDevice();
        if (handle < 0) {
            log.error("init deka readcard device failed . handle = {}", handle);
            return null;
        }

        // read id card info
        status = dekaReader.dc_SamAReadCardInfo(handle, 3, text_len, text, photo_len, photo, fingerprint_len, fingerprint, extra_len, extra);
        if (!DekaReader.isSuccess(status)) {
            log.error("read id card info failed . status = {} , exit device ...", status);
            status = dekaReader.dc_exit(handle);
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
            if(!DekaReader.isSuccess(status)){
                log.error("parse text info failed . status = {}", status);
                status = dekaReader.dc_exit(handle);
                return null;
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
                return null;
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
            return foreignIDCard;

        }



        return null;


    }


}
