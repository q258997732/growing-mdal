package bob.growingmdal.util.reader;

import bob.growingmdal.util.loader.CustomDllLoader;
import bob.growingmdal.util.loader.NativeLibraryLoader;
import jnr.ffi.Pointer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @brief Deka 读卡器接口，注意返回结果为UTF_16LE编码格式
 */
public interface DekaReader {

    short PORT_USB = 100;  // 新增常量定义
    int BAUD = 115200;  // 波特率,串口才需要
    int PORT_COM1 = 0;
    int PORT_COM2 = 1;

    /**
     * 加载DLL
     *
     * @return 加载结果
     */
    static DekaReader load() {
        return NativeLibraryLoader.load(
                DekaReader.class,
                "dcrf32.dll",
                "/lib/deka_T10-MX4_x64/"
        );
    }

    static DekaReader load(String dllName, String resourcePath) {
        return NativeLibraryLoader.load(
                DekaReader.class,
                dllName,
                resourcePath
        );
    }

    /**
     * @brief  库入口。
     * @par    说明：
     * 可以获取或设置一些库相关参数，此接口可不掉用，如需调用必须放在所有其它接口之前调用。
     * @param[in] flag 标志，用于决定 @a context 的类型和含义。
     * @n 0 - 表示获取库版本， @a context 类型为char *，请至少分配64个字节。
     * @n 1 - 表示设置库的工作目录， @a context 类型为const char *。
     * @n 2 - 表示设置库调用者的工作目录， @a context 类型为const char *。
     * @n 3 - 保留内部使用，表示注入端口辅助接口上下文， @a context 类型为自定义结构指针。
     * @n 4 - 保留内部使用，表示获取库的外部认证接口上下文， @a context 类型为自定义结构指针。
     * @n 5 - 保留内部使用，设置库的版本， @a context 类型为const char *。
     * @n 6 - 保留内部使用，表示注入日志辅助接口上下文， @a context 类型为自定义结构指针。
     * @n 7 - 保留内部使用，使得注入接口库可以获取控制上下文， @a context 类型为自定义结构指针。
     * @n 8 - 保留内部使用，进行库的内部认证。
     * @param[in,out] context 参数实际类型和含义由 @a flag 的值来决定。
     */
    void LibMain(int flag, byte[] version);

    int dc_init(short port, int baud);

    short dc_exit(int handle);

    short dc_beep(int handle, short _Msec);

    short dc_reset(int handle, short _Msec);

    short dc_config_card(int handle, byte cardtype);

    short dc_card_n(int handle, byte _Mode, int[] SnrLen, byte[] _snr);

    short dc_authentication_passaddr(int handle, byte _Mode, byte _Addr, byte[] passbuff);

    short dc_write(int handle, byte _Adr, byte[] _Data);

    /**
     * @return <0表示失败，==0表示成功。
     * @brief 读卡数据。
     * @par 说明：
     * 读取卡内数据，对于M1卡，一次读取一个块的数据，为16个字节；对于ML卡，一次读取相同属性的两页，为8个字节。
     * @param[in] icdev 设备标识符。
     * @param[in] _Adr 地址。
     * @n M1卡 - S50块地址（0~63），S70块地址（0~255）。
     * @n ML卡 - 页地址（0~11）。
     * @param[out] _Data 固定返回16个字节数据，真实数据可能小于16个字节。
     */
    short dc_read(int handle, byte _Adr, byte[] _Data);

    short dc_initval(int handle, byte _Adr, int _Value);

    short dc_readval(int handle, byte _Adr, int[] _Value);

    short dc_increment(int handle, byte _Adr, int _Value);

    short dc_decrement(int handle, byte _Adr, int _Value);

    short dc_restore(int handle, byte _Adr);

    short dc_transfer(int handle, byte _Adr);

    short dc_pro_resetInt(int handle, byte[] rlen, byte[] receive_data);

    short dc_pro_commandlinkInt(int handle, int slen, byte[] sendbuffer, int[] rlen, byte[] databuffer, byte timeout);

    short hex_a(byte[] hex, byte[] a, short length);

    short a_hex(byte[] a, byte[] hex, short len);

    short dc_card_b(int handle, byte[] rbuf);

    short dc_MFPL0_writeperso(int handle, int BNr, byte[] dataperso);

    short dc_auth_ulc(int handle, byte[] dataperso);

    short dc_verifypin_4442(int handle, byte[] passwd);

    short dc_write_4442(int handle, int offset, int length, byte[] data_buffer);

    short dc_read_4442(int handle, int offset, int length, byte[] data_buffer);

    short dc_verifypin_4428(int handle, byte[] passwd);

    short dc_write_4428(int handle, int offset, int length, byte[] data_buffer);

    short dc_read_4428(int handle, int offset, int length, byte[] data_buffer);

    short dc_setcpu(int handle, byte _Byte);

    short dc_write_24c(int handle, int offset, int length, byte[] snd_buffer);

    short dc_read_24c(int handle, int offset, int length, byte[] receive_buffer);

    short dc_cpureset(int handle, byte[] rlen, byte[] databuffer);

    short dc_cpuapduInt(int handle, int slen, byte[] sendbuffer, int[] rlen, byte[] databuffer);

    short dc_getver(int handle, byte[] version);

    short dc_startreadmag(int handle);

    short dc_stopreadmag(int handle);

    short dc_readmag(int handle, byte[] t1_data, int[] t1_len, byte[] t2_data, int[] t2_len, byte[] t3_data, int[] t3_len);

    short dc_card_exist(int handle, byte[] flag);

    short dc_GetBankAccountNumber(int handle, int type, byte[] flag);

    /**
     * @return <0表示失败，==0表示成功。
     * @brief 读身份证。
     * @par 说明：
     * 读取身份证的原始信息数据。
     * @param[in] icdev 设备标识符。
     * @param[in] type 类型。
     * @n 1 - 读取文字信息、相片信息和指纹信息。
     * @n 2 - 读取追加住址信息。
     * @n 3 - 读取文字信息、相片信息、指纹信息和追加住址信息。
     * @param[out] text_len 返回文字信息的长度。
     * @param[out] text 返回的文字信息，请至少分配256个字节。
     * @param[out] photo_len 返回相片信息的长度。
     * @param[out] photo 返回的相片信息，请至少分配1024个字节。
     * @param[out] fingerprint_len 返回指纹信息的长度。
     * @param[out] fingerprint 返回的指纹信息，请至少分配1024个字节。
     * @param[out] extra_len 返回追加住址信息的长度。
     * @param[out] extra 返回的追加住址信息，请至少分配70个字节。
     **/
    short dc_SamAReadCardInfo(int handle, int type, int[] text_len, byte[] text, int[] photo_len,
                              byte[] photo, int[] fingerprint_len, byte[] fingerprint, int[] extra_len, byte[] extra);

    short dc_ParseTextInfo(int handle, int charset, int info_len, byte[] info, byte[] name,
                           byte[] sex, byte[] nation, byte[] birth_day, byte[] address, byte[] id_number,
                           byte[] department, byte[] expire_start_day, byte[] expire_end_day, byte[] reserved);

    /**
     * @brief  解析文字信息。
     * @par    说明：
     * 解析外国人永久居留证（2017版）文字信息，获取相应的条目。
     * @param[in] icdev 设备标识符。
     * @param[in] charset 获取条目将采用的字符集，0表示GBK，1表示UCS-2LE，2表示UTF-8。
     * @param[in] info_len 文字信息的长度。
     * @param[in] info 文字信息。
     * @param[out] english_name 英文姓名，请至少分配244个字节。
     * @param[out] sex 性别代码，请至少分配8个字节。
     * @param[out] id_number 永久居留证号码，请至少分配64个字节。
     * @param[out] citizenship 国籍或所在地区代码，请至少分配16个字节。
     * @param[out] chinese_name 中文姓名，请至少分配64个字节。
     * @param[out] expire_start_day 证件签发日期，请至少分配36个字节。
     * @param[out] expire_end_day 证件终止日期，请至少分配36个字节。
     * @param[out] birth_day 出生日期，请至少分配36个字节。
     * @param[out] version_number 证件版本号，请至少分配12个字节。
     * @param[out] department_code 当次申请受理机关代码，请至少分配20个字节。
     * @param[out] type_sign 证件类型标识，请至少分配8个字节。
     * @param[out] reserved 预留项，请至少分配16个字节。
     * @return <0表示失败，==0表示成功。
     */
    short dc_ParseTextInfoForForeigner(int handle, int charset, int info_len, byte[] info,
                                       byte[] english_name, byte[] sex, byte[] id_number, byte[] citizenship, byte[] chinese_name,
                                       byte[] expire_start_day, byte[] expire_end_day, byte[] birth_day, byte[] version_number,
                                       byte[] department_code, byte[] type_sign, byte[] reserved);

    /**
     * @brief  解析相片信息。
     * @par    说明：
     * 解析相片信息，通过公安部相片解码库还原相片图像数据。
     * @param[in] icdev 设备标识符。
     * @param[in] type 相片图像数据的格式，0表示BMP文件，1表示BMP缓存，2表示BMP Base64字符串。
     * @param[in] info_len 相片信息的长度。
     * @param[in] info 相片信息。
     * @param[in,out] photo_len 数据的长度。
     * @n BMP文件 - 无效。
     * @n BMP缓存 - 传入 @a photo 分配的字节数，返回相片图像数据的长度。
     * @n BMP Base64字符串 - 传入 @a photo 分配的字节数，返回相片图像数据Base64字符串的长度，不含'\0'。
     * @param[in,out] photo 数据。
     * @n BMP文件 - 传入文件名，请确保有写入的权限。
     * @n BMP缓存 - 返回的相片图像数据，请至少分配65536个字节。
     * @n BMP Base64字符串 - 返回的相片图像数据Base64字符串，请至少分配65536个字节。
     * @return <0表示失败，==0表示成功。
     */
    short dc_ParsePhotoInfo(int handle, int type, int info_len, byte[] info, int[] photo_len,
                            byte[] photo);

    short dc_Scan2DBarcodeStart(int handle, byte mode);

    short dc_Scan2DBarcodeGetData(int handle, int[] rlen, byte[] rdata);

    short dc_Scan2DBarcodeExit(int handle);

    /**
     * @brief  生成相片图像文件。
     * @par    说明：
     * 使用内部保存的相片原始数据，通过调用公安部相片解码库解码生成相片图像文件。
     * @param[in] idhandle 身份证标识符。
     * @param[in] FileName 文件名，请确保有写入的权限。
     * @return <0表示失败，==0表示成功。
     */
    short dc_i_d_query_photo_file(int handle, String fileName);

    public static short returnActualLength(byte[] data) {
        short i = 0;
        for (; i < data.length; i++) {
            if (data[i] == '\0')
                break;
        }
        return i;
    }
    public static String gbk_bytes_to_string(byte[] data) {
        int i;
        String s = "";

        for (i = 0; i < data.length; ++i) {
            if (data[i] == 0) {
                break;
            }
        }

        byte[] temp = new byte[i];
        System.arraycopy(data, 0, temp, 0, temp.length);

        try {
            s = new String(temp, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return s;
    }
    public static byte[] string_to_gbk_bytes(String data) {
        int i = 0;
        byte[] s = null;

        try {
            s = data.getBytes("GBK");
            i = s.length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] temp = new byte[i + 1];
        System.arraycopy(s, 0, temp, 0, i);
        temp[i] = 0;

        return temp;
    }
    public static void print_bytes(byte[] b, int length) {
        for (int i = 0; i < length; ++i) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase());
        }
    }
}
