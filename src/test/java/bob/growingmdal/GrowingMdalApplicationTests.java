package bob.growingmdal;

import bob.growingmdal.util.reader.DekaReader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.provider.MemoryManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


@SpringBootTest
class GrowingMdalApplicationTests {

    @Autowired
    DekaReader dekaReader;

    @Test
    void contextLoads() throws IOException {

        // 初始化参数
        int[] text_len = new int[1];
        byte[] text = new byte[256];
        int info_len = 1024;
        byte[] info = new byte[info_len];
        int[] photo_len = new int[]{65536};
        byte[] photo = new byte[photo_len[0]];
        int[] fingerprint_len = new int[1];
        byte[] fingerprint = new byte[1024];
        int[] extra_len = new int[1];
        byte[] extra = new byte[70];
        int type = 0;
        byte[] _Snr = new byte[100];
        byte[] szSnr = new byte[200];
        int[] SnrLen = new int[10];
        byte[] version = new byte[64];
        java.util.Arrays.fill(_Snr, (byte) 0);
        java.util.Arrays.fill(szSnr, (byte) 0);
        java.util.Arrays.fill(SnrLen, (byte) 0);

        int status = -1;



        // 获取与设置库信息
        dekaReader.LibMain(0,version);
        System.out.println("LibMain: " + DekaReader.gbk_bytes_to_string(version));

        // 初始化并获取句柄
        int handle = dekaReader.dc_init(DekaReader.PORT_USB, DekaReader.BAUD);
        System.out.println("dc_init: " + handle);

        // beep
//        dekaReader.dc_beep( handle, (short) 10);

        // 复位射频
//        dekaReader.dc_reset(handle, (short)1);

        // 配置非接触卡类型 ==0 success
//        status = dekaReader.dc_config_card(handle, (byte) 'A');
//        System.out.println("dc_config_card: " + status);

        // 寻卡请求、防卡冲突、选卡操作  <0表示失败，==0表示成功，==1表示无卡或无法寻到卡片
//        status = dekaReader.dc_card_n(handle, (byte)0, SnrLen, _Snr);
//        System.out.println("dc_card_n: " + status);

        // 读卡数据
//        byte[] rdata = new byte[100];
//        byte[] rdatahex = new byte[100];
//        status = dekaReader.dc_read(handle, (byte)4, rdata);
//        System.out.println("dc_read(0) :" + status);

        //
        status = dekaReader.dc_SamAReadCardInfo(handle, 3, text_len, text, photo_len, photo, fingerprint_len, fingerprint, extra_len, extra);
        System.out.println("dc_SamAReadCardInfo: " + status);

        if ((text[0] >= 'A') && (text[0] <= 'Z') && (text[1] == 0)) {
            type = 1;
            System.out.println("type: " + type);
        }
        // 读取文字信息、相片信息和指纹信息处理
        if (type == 0) {
            System.out.println("读取到国内身份证:" + type);

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

            System.out.println("dc_ParseTextInfo ... ");
            status = dekaReader.dc_ParseTextInfo(handle, 0, text_len[0], text, name, sex, nation, birth_day, address, id_number, department, expire_start_day, expire_end_day, reserved);
            System.out.println("dc_ParseTextInfo: " + status);
            System.out.println("name: " + DekaReader.gbk_bytes_to_string(name));
            System.out.println("sex: " + DekaReader.gbk_bytes_to_string(sex));
            System.out.println("nation: " + DekaReader.gbk_bytes_to_string(nation));
            System.out.println("birth_day: " + DekaReader.gbk_bytes_to_string(birth_day));
            System.out.println("address: " + DekaReader.gbk_bytes_to_string(address));
            System.out.println("id_number: " + DekaReader.gbk_bytes_to_string(id_number));
            System.out.println("department: " + DekaReader.gbk_bytes_to_string(department));
            System.out.println("expire_start_day: " + DekaReader.gbk_bytes_to_string(expire_start_day));
            System.out.println("expire_end_day: " + DekaReader.gbk_bytes_to_string(expire_end_day));
        } else if (type == 1) {
            System.out.println("读取到外国身份证:" + type);

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

            System.out.println("dc_ParseTextInfo ... ");
            status = dekaReader.dc_ParseTextInfoForForeigner(handle, 0, text_len[0], text, english_name, sex, id_number, citizenship, chinese_name, expire_start_day, expire_end_day, birth_day, version_number, department_code, type_sign, reserved);
            System.out.println("dc_ParseTextInfoForForeigner ... " + status);

            // 输出身份证信息
            System.out.println("english_name: " + DekaReader.gbk_bytes_to_string(english_name));
            System.out.println("sex: " + DekaReader.gbk_bytes_to_string(sex));
            System.out.println("id_number: " + DekaReader.gbk_bytes_to_string(id_number));
            System.out.println("citizenship: " + DekaReader.gbk_bytes_to_string(citizenship));
            System.out.println("chinese_name: " + DekaReader.gbk_bytes_to_string(chinese_name));
            System.out.println("expire_start_day: " + DekaReader.gbk_bytes_to_string(expire_start_day));
            System.out.println("expire_end_day: " + DekaReader.gbk_bytes_to_string(expire_end_day));
            System.out.println("birth_day: " + DekaReader.gbk_bytes_to_string(birth_day));
            System.out.println("version_number: " + DekaReader.gbk_bytes_to_string(version_number));
            System.out.println("department_code: " + DekaReader.gbk_bytes_to_string(department_code));
            System.out.println("type_sign: " + DekaReader.gbk_bytes_to_string(type_sign));
        }

//        DekaReader.print_bytes(photo,photo.length);
        // 获取照片信息
        status = dekaReader.dc_ParsePhotoInfo(handle,2,info_len,info, photo_len, photo);
//        status = dekaReader.dc_ParsePhotoInfo(handle, 0, info_len, info, photo_len, DekaReader.string_to_gbk_bytes("me.bmp"));
//        status = dekaReader.dc_i_d_query_photo_file(handle, "me.bmp");
        System.out.println("dc_ParsePhotoInfo: " + status);
        //        System.out.println("infoLength: " + info_len);
        DekaReader.print_bytes(photo, photo_len[0]);
        System.out.println("photoLength: " + photo_len[0]);

//        System.out.println();https://u.wechat.com/MBBY1Otpoat5bkhUJVe4KX4?s=2

//        String input2 = "me.bmp";
//        String output2 = "me.jpg";
//            RenderedOp src2 = JAI.create("fileload", input2);
//            OutputStream os2 = null;
//            try
//            {
//                os2 = new FileOutputStream(output2);
//            }
//            catch(IOException ex)
//            {
//
//            }
//            JPEGEncodeParam param2 = new JPEGEncodeParam();
//            ImageEncoder enc2 = ImageCodec.createImageEncoder("JPEG", os2, param2);
//            try
//            {
//                enc2.encode(src2);
//            }
//            catch(IOException ex)
//            {
//
//            }
//            try
//            {
//                os2.close();
//            }
//            catch(IOException ex)
//            {
//
//            }
//            ImageIcon image = new ImageIcon("me.jpg");
//            picture1.setText("");
//
//            picture1.setIcon(image);  //add a label contains the image which have to change to ImageIcon;


    }
}
