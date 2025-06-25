package bob.growingmdal;

import jnr.ffi.Struct;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.byref.LongLongByReference;
import jnr.ffi.types.u_int16_t;
import jnr.ffi.types.u_int8_t;
import jnr.ffi.util.EnumMapper;

import java.io.UnsupportedEncodingException;

public interface PinpadCJNR {

    int BASE_COMMON = 1000; /// common for all

    int BASE_PINPAD = 2000; /// Pinpad(2000 ~ 2099)
    int WARN_PINPAD = 2100; /// Pinpad warn(2100 ~ 2179)

    /**
     * 设备错误码, TODO: 增加完成错误码和信息
     */
    enum ERROR_CODE {
        EC_SUCCESS(0, "Success"), ///< 成功
        EC_OPEN_FAILED(BASE_COMMON + 1, ""), ///< 打开端口失败
        EC_INVALID_PORT(BASE_COMMON + 2, ""), ///< 无效端口
        EC_INVALID_PARA(BASE_COMMON + 3, ""), ///< 无效参数
        EC_INVALID_DATA(BASE_COMMON + 4, ""), ///< 无效数据
        EC_SEND_FAILED(BASE_COMMON + 5, ""), ///< 发送失败
        EC_RECEIVED_FAILED(BASE_COMMON + 6, ""), ///< 接收失败
        EC_USER_CANCEL(BASE_COMMON + 7, ""), ///< 用户取消
        EC_DATA_TOOLONG(BASE_COMMON + 8, ""), ///< 数据太长
        EC_NAK_RECEIVED(BASE_COMMON + 9, ""), ///< NAK 接收
        EC_READ_TIMEOUT(BASE_COMMON + 10, ""), ///< 读取数据超时
        EC_WRITE_TIMEOUT(BASE_COMMON + 11, ""), ///< 写数据超时
        EC_WAITEVENT_FAILED(BASE_COMMON + 12, ""), ///< 
        EC_SET_FAILED(BASE_COMMON + 13, ""), ///< 
        EC_STEP_ERROR(BASE_COMMON + 14, ""), ///< 
        EC_POINTER_NULL(BASE_COMMON + 15, ""), ///< 
        EC_FULL_NOW(BASE_COMMON + 16, ""), ///< 
        EC_NET_ERROR(BASE_COMMON + 17, ""), ///< 
        EC_INVALID_FILE(BASE_COMMON + 18, ""), ///< 
        EC_TEST_MODE(BASE_COMMON + 19, ""), ///< 
        EC_EXIT(BASE_COMMON + 20, ""), ///< 
        EC_ALLOC_FAILED(BASE_COMMON + 21, ""), ///< 
        EC_TYPE_UNMATCH(BASE_COMMON + 22, ""), ///< 
        EC_RETURN_FAILED(BASE_COMMON + 23, ""), ///< 
        EC_SERIOUS_ERROR(BASE_COMMON + 24, ""), ///< 
        EC_UNSUPPORT(BASE_COMMON + 25, ""), ///< 
        EC_COMMAND_UNMATCH(BASE_COMMON + 26, ""), ///< 
        EC_SEQ_UNMATCH(BASE_COMMON + 27, ""), ///< 

        PIN_INVALID_COMMAND_PARA(BASE_PINPAD + 1, ""),  ///
        PIN_MAC_XOR_ERROR(BASE_PINPAD + 2, ""),  ///
        PIN_MAC_CRC_ERROR(BASE_PINPAD + 3, ""),  ///
        PIN_MAC_KEYCOMMAND_ERROR(BASE_PINPAD + 4, ""),  ///
        PIN_INNER_ERROR(BASE_PINPAD + 5, ""),  ///
        PIN_INVALID_DATA(BASE_PINPAD + 6, ""),  ///
        PIN_DATA_TOOLONG(BASE_PINPAD + 7, ""),  ///
        PIN_COMMAND_UNSUPPORT(BASE_PINPAD + 8, ""),  ///
        PIN_ALGORITHM_UNSUPPORT(BASE_PINPAD + 9, ""),  ///
        PIN_SERIAL_NUM_ERROR(BASE_PINPAD + 10, ""),  ///
        PIN_INVALID_RSA_SN(BASE_PINPAD + 11, ""),  ///
        PIN_EPP_NOT_INITIALIZED(BASE_PINPAD + 12, ""),  ///
        PIN_SELFTEST_ERROR(BASE_PINPAD + 13, ""),  ///
        PIN_PRESS_KEY_TIMEOUT(BASE_PINPAD + 14, ""),  ///
        PIN_KEY_UNRELEASED(BASE_PINPAD + 15, ""),  ///
        PIN_NOPSW_OR_ERROR(BASE_PINPAD + 16, ""),  ///
        PIN_INVALID_PIN_LENGTH(BASE_PINPAD + 17, ""),  ///
        PIN_GET_PINBLOCK_ERROR(BASE_PINPAD + 18, ""),  ///
        PIN_RANDOM_DATA_ERROR(BASE_PINPAD + 19, ""),  ///
        PIN_INVALID_ENTRY_MODE(BASE_PINPAD + 20, ""),  ///
        PIN_INVALID_WRITE_MODE(BASE_PINPAD + 21, ""),  ///
        PIN_INVALID_KEYID(BASE_PINPAD + 22, ""),  ///
        PIN_KEY_USEVIOLATION(BASE_PINPAD + 23, ""),  ///
        PIN_KEY_NOT_LOADED(BASE_PINPAD + 24, ""),  ///
        PIN_KEY_LOCKED(BASE_PINPAD + 25, ""),  ///
        PIN_INVALID_MASTER_KEY(BASE_PINPAD + 26, ""),  ///
        PIN_IMK_NOT_EXIST(BASE_PINPAD + 27, ""),  ///
        PIN_TMK_NOT_EXIST(BASE_PINPAD + 28, ""),  ///
        PIN_KEY_NOT_EXIST(BASE_PINPAD + 29, ""),  ///
        PIN_SAME_KEY_VALUE(BASE_PINPAD + 30, ""),  ///
        PIN_INVALID_KEY_VALUE(BASE_PINPAD + 31, ""),  ///
        PIN_INVALID_KEY_LENGTH(BASE_PINPAD + 32, ""),  ///
        PIN_INVALID_IV_ATTRIBUTES(BASE_PINPAD + 33, ""),  ///
        PIN_INVALID_KEY_ATTRIBUTES(BASE_PINPAD + 34, ""),  ///
        PIN_INVALID_OFFSET_LENGTH(BASE_PINPAD + 35, ""),  ///
        PIN_INVALID_LENGTH_OR_SUM(BASE_PINPAD + 36, ""),  ///
        PIN_ENCRYPT_SUSPENDED(BASE_PINPAD + 37, ""),  ///
        PIN_AUTHENTICATE_LOCKED_HOURS(BASE_PINPAD + 38, ""),  ///
        PIN_COMMAND_LOCKED(BASE_PINPAD + 39, ""),  ///
        PIN_INVALID_USERBLOCK_ADDRESS(BASE_PINPAD + 40, ""),  ///
        PIN_INVALID_MODULUS_LENGTH(BASE_PINPAD + 41, ""),  ///
        PIN_INVALID_EXPONENT_LENGTH(BASE_PINPAD + 42, ""),  ///
        PIN_INVALID_PKCS_STRUCTURE(BASE_PINPAD + 43, ""),  ///
        PIN_INVALID_PKCS_PADDING(BASE_PINPAD + 44, ""),  ///
        PIN_INVALID_SIGNATURE_LENGTH(BASE_PINPAD + 45, ""),  ///
        PIN_INVALID_SIGNATURE_SHA(BASE_PINPAD + 46, ""),  ///
        PIN_SIG_VERIFICATION_FAILED(BASE_PINPAD + 47, ""),  ///
        PIN_KCV_VERIFICATION_FAILED(BASE_PINPAD + 48, ""),  ///
        PIN_PIN_VERIFICATION_FAILED(BASE_PINPAD + 49, ""),  ///
        PIN_VERIFICATION_FAILED(BASE_PINPAD + 50, ""),  ///
        PIN_NOT_AUTHENTE(BASE_PINPAD + 51, ""),  ///
        PIN_INVALID_AUTHENTICATION_MODE(BASE_PINPAD + 52, ""),  ///
        PIN_CERTIFICATE_NOT_EXIST(BASE_PINPAD + 53, ""),  ///
        PIN_RECV_SPECIAL_KEY(BASE_PINPAD + 54, ""),  ///
        PIN_INVALID_CERTIFICATE_FORMAT(BASE_PINPAD + 55, ""),  ///
        PIN_INVALID_CERTIFICATE_VERSION(BASE_PINPAD + 56, ""),  ///
        PIN_INVALID_CERTIFICATE_ISSUER(BASE_PINPAD + 57, ""),  ///
        PIN_INVALID_CERTIFICATE_VALIDITY(BASE_PINPAD + 58, ""),  ///
        PIN_INVALID_CERTIFICATE_SUBJECT(BASE_PINPAD + 59, ""),  ///
        PIN_INVALID_CERTIFICATE_ALGOR(BASE_PINPAD + 60, ""),  ///
        PIN_NO_CARD(BASE_PINPAD + 61, ""),  ///
        PIN_CARD_APDU_ERROR(BASE_PINPAD + 62, ""),  ///
        PIN_EMV_NOT_INITIALIZED(BASE_PINPAD + 63, ""),  ///
        PIN_EMV_NOT_READY(BASE_PINPAD + 64, ""),  ///
        PIN_EMV_NEED_REINITIALIZE(BASE_PINPAD + 65, ""),  ///
        PIN_EMV_TIMEOUT(BASE_PINPAD + 66, ""),  ///
        PIN_PSW_NOT_INITIALIZED(BASE_PINPAD + 67, ""),  ///
        PIN_EPP_NOT_INSTALLED(BASE_PINPAD + 68, ""),  ///
        PIN_INVALID_PADDING(BASE_PINPAD + 69, ""),  ///
        PIN_PHYSICALLY_UNINSTALLED(BASE_PINPAD + 70, ""),  ///
        PIN_LOGICALLY_UNINSTALLED(BASE_PINPAD + 71, ""),  ///
        PIN_INPUT_KEY_TIMEOUT(BASE_PINPAD + 72, ""),  ///
        PIN_INVALID_PASSWORD_LENGTH(BASE_PINPAD + 73, ""),  ///
        PIN_INVALID_PASSWORD(BASE_PINPAD + 74, ""),  ///
        PIN_INPUT_PASSWORD_LOCKED(BASE_PINPAD + 75, ""),  ///
        PIN_SYSTEM_TIME_NOT_SET(BASE_PINPAD + 76, ""),  ///
        PIN_SYSTEM_TIME_ALREADY_SET(BASE_PINPAD + 77, ""),  ///
        PIN_MRAM_HARDWARE_ERROR(BASE_PINPAD + 78, ""),  ///
        PIN_DEVICE_TAMPERED(BASE_PINPAD + 79, ""),  ///
        PIN_SM2_ENCRYPT_FAILURE(BASE_PINPAD + 80, ""),  ///
        PIN_SM2_DECRYPT_FAILURE(BASE_PINPAD + 81, ""),  ///
        PIN_SM2_SIGNATURE_FAILURE(BASE_PINPAD + 82, ""),  ///
        PIN_SM2_VERSIG_FAILURE(BASE_PINPAD + 83, ""),  ///
        PIN_SM2_KEYEXC_FAILURE(BASE_PINPAD + 84, ""),  ///
        PIN_SM2_VER_KEYEXC_FAILURE(BASE_PINPAD + 85, ""),  ///
        PIN_CHIP_TIMEOUT(BASE_PINPAD + 86, ""),  ///
        PIN_INVALID_SM4_KEYVAL(BASE_PINPAD + 87, ""),  ///
        PIN_INVALID_INSTALLATION_MODE(BASE_PINPAD + 88, ""),  ///
        PIN_CHIP_INNER_ERROR(BASE_PINPAD + 89, ""),  ///
        PIN_CRYPT_FAILURE(BASE_PINPAD + 90, ""),  ///
        PIN_CARD_UNSUPPORT(BASE_PINPAD + 91, ""),  ///
        PIN_CARD_POWER_OFF(BASE_PINPAD + 92, ""),  ///
        PIN_PSAM_ERROR(BASE_PINPAD + 93, ""), ///
        
        PIN_EMV_ALREADY_INITIALIZED(WARN_PINPAD + 1, ""),  ///
        PIN_POWER_ERROR(WARN_PINPAD + 2, ""),  ///
        PIN_CERTIFICATE_ALREADY(WARN_PINPAD + 3, ""),  ///
        PIN_EPP_ALREADY_INITIALIZED(WARN_PINPAD + 4, ""); ///<

        /**
         * enum value
         */
        private final int value;
        /**
		 * error code info
		 */
		private final String info;

        ERROR_CODE(int value, String info) {
            this.value = value;
            this.info = info;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        /**
         * 获取Enum整型值.
         *
         * @return 整型值
         */
        public int getValue() {
            return value;
        }

        /**
		 * 获取错误码信息
         *
         * @return 信息
		 */
		public String getInfo() {
			return this.info;
		}

        /**
		 * 
		 */
		@Override
		public String toString() {
			return this.info;
		}
    }

     /**
     * 日志等级
     */
     enum LOG_LEVEL implements EnumMapper.IntegerEnum {
        ALL(0x00),     /// LOG_LEVEL_ALL
        DEBUG(0x1),   ///< LOG_LEVEL_DEBUG
        COMMON(0x10), ///< LOG_LEVEL_COMMON
        WARN(0x20),    ///< LOG_LEVEL_WARN
        DATA(0x40),     ///< LOG_LEVEL_DATA
        ERROR(0x80),  ///< LOG_LEVEL_ERROR
        FATTAL(0x100),    ///< LOG_LEVEL_FATTAL
        NONE(0x200);     ///< LOG_LEVEL_NONE

        /**
         * enum value
         */
        private final int value;

        LOG_LEVEL(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 设备端口类型
     */
    enum EPORT implements EnumMapper.IntegerEnum {
        eCOM(0x0),   ///< Serial("/dev/ttyUSB0:9600,N,8,1")
        eUSB_FTDI(0x1), ///< only for FTDI chip with baudrate, such as EPP("FT232R USB UART:9600,N,8,1")
        eUSB(0x2),      ///< Windows(such as "VID_23AB&PID_0002","VID_23AB&PID_0002&REV_0900","USB\\VID_23AB&PID_0002")
        ///< Linux  ("lp0")
        eHID(0x3),     ///< Windows(such as "VID_23AB&PID_1003","VID_23AB&PID_1003&REV_0100","HID\\VID_23AB&PID_1003")
        ///< Linux ("hiddev0")
        ePC_SC(0x20),  ///< only for windows PC/SC()
        eLPT(0x40),     ///< LPT("LPT1")
        eTCPIP(0x80),    ///< TCP(such as "127.0.0.1:36860")
        eCOMBINE(0x100); ///< It is combined with master device, and must behind master device instantiated

        /**
         * enum value
         */
        private final int value;

        EPORT(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 键盘类型，指令类型
     */
    enum EPIN_TYPE implements EnumMapper.IntegerEnum {
        ePIN_UNKNOWN(0x0),   ///< Unknown type(DLL will try to detect pinpad's type,when finish do that you cann't change other command pinpad)
        ePIN_EPP(0x10000),   ///< EPP command type(Bxx Cxx Exx Vxx)
        ePIN_VISA(0x20000),  ///< VISA command type(Dxx)
        ePIN_PCI(0x40000),   ///< PCI command type(Hxx)
        ePIN_WOSA(0x80000),  ///< WOSA command type(Fxx)

        ePIN_PCI_3X(0x40000 + 0x1),  ///< PCI_3X/4X
        ePIN_VISA_3X(0x20000 + 0x2),  ///< VISA_3X
        ePIN_EPP_BR(0x10000 + 0x4), ///< EPP_3X
        ePIN_WOSA_3X(0x80000 + 0x8),  ///< WOSA_3X

        ePIN_EPP_BD(0x10000 + 0x10),  ///< EPP_BDX

        eECC_VISA(0x20000 + 0x80),  ///< ECC-ZT128A
        eECC_PCI(0x40000 + 0x80);  ///< ECC-ZT128B/ECC-ZT130

        /**
         * enum value
         */
        private final int value;

        EPIN_TYPE(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 键盘扩展参数
     */
    enum EPIN_EXTEND implements EnumMapper.IntegerEnum {
        eEX_NONE(0x00),  ///< No extend
        eEX_STRING_NAME(0x01),  ///< Key name is string, set this to use XFS_XXX function
        eEX_ENLARGE_KEY(0x02),  ///< Auto enlarge key usage or key attribute
        eEX_PC_KB(0x04),  ///< use as PC keyboard

        eEX_SAVE2EPP(0x08),  ///< mapped table save to EPP(it can't use singleness and some EPP unsupport)
        eEX_SAVE2DB(0x10),  ///< mapped table save to DB(it can't use singleness)

        eEX_1_8(0x01 | 0x08),  ///< eEX_STRING_NAME | eEX_SAVE2EPP
        eEX_2_8(0x02 | 0x08),  ///< eEX_ENLARGE_KEY | eEX_SAVE2EPP
        eEX_4_8(0x04 | 0x08), ///< eEX_PC_KB | eEX_SAVE2EPP

        eEx_2_10(0x02 | 0x10),  ///< eEX_ENLARGE_KEY | eEX_SAVE2DB

        eEX_1_2(0x01 | 0x02),  ///< eEX_STRING_NAME | eEX_ENLARGE_KEY
        eEX_1_4(0x01 | 0x04), ///< eEX_STRING_NAME | eEX_PC_KB
        eEX_1_2_8(0x01 | 0x02 | 0x08), ///< eEX_STRING_NAME | eEX_ENLARGE_KEY | eEX_SAVE2EPP
        eEX_1_4_8(0x01 | 0x04 | 0x08), ///< eEX_STRING_NAME | eEX_PC_KB | eEX_SAVE2EPP
        eEx_1_2_10(0x01 | 0x02 | 0x10); ///< eEX_STRING_NAME | eEX_PC_KB | eEX_SAVE2DB

        /**
         * enum value
         */
        private final int value;

        EPIN_EXTEND(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 加解密算法
     */
    enum ECRYPT implements EnumMapper.IntegerEnum {
        CRYPT_DESECB(0x0001),  ///< DES ECB
        CRYPT_DESCBC(0x0002),  ///< DES CBC
        CRYPT_DESCFB(0x0004),  ///< DES CFB
        CRYPT_RSA(0x0008),  ///< RSA
        CRYPT_ECMA(0x0010),  ///< ECMA
        CRYPT_DESMAC(0x0020),  ///< DES MAC
        CRYPT_TRIDESECB(0x0040),  ///< TDES ECB
        CRYPT_TRIDESCBC(0x0080),  ///< TDES CBC
        CRYPT_TRIDESCFB(0x0100),  ///< TDES CFB
        CRYPT_TRIDESMAC(0x0200),  ///< TDES MAC
        CRYPT_MAAMAC(0x0400),  ///< MAA MAC
        CRYPT_SM4ECB(0x1000),    ///< SM4 ECB
        CRYPT_SM4MAC(0x2000),    ///< SM4 CBC
        CRYPT_SM4CBC(0x4000),    ///< SM4 MAC

        CRYPT_SM2(0x80000),   ///< SM2
        CRYPT_AESECB(0x100000),  ///< AES ECB
        CRYPT_AESCBC(0x200000),  ///< AES CBC
        CRYPT_AESOFB(0x400000),  ///< AES OFB
        CRYPT_AESCFB(0x800000),  ///< AES CFB
        CRYPT_AESPCBC(0x1000000), ///< AES PCBC
        CRYPT_AESCTR(0x2000000), ///< AES CTR
        CRYPT_SM4CFB(0x10000000), ///< SM4 CFB
        CRYPT_SM4OFB(0x20000000), ///< SM4 OFB
        CRYPT_DESOFB(0x40000000), ///< DES OFB
        CRYPT_SM2_123(0x80008);   ///< HL-银商测试改为C1C2C3 SM2

        /**
         * enum value
         */
        private final int value;

        ECRYPT(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * Pinblock 算法
     */
    enum EPINFORMAT implements EnumMapper.IntegerEnum {
        FORMAT_FREE(0x0000),      ///< arithmetic choose
        FORMAT_IBM3624(0x0001),   ///< IBM3624
        FORMAT_ANSI(0x0002),      ///< ANSI 9.8
        FORMAT_ISO0(0x0004),     ///< ISO9564 0
        FORMAT_ISO1(0x0008),      ///< ISO9564 1
        FORMAT_ECI2(0x0010),      ///< ECI2
        FORMAT_ECI3(0x0020),      ///< ECI3
        FORMAT_VISA(0x0040),      ///< VISA/VISA2
        FORMAT_DIEBOLD(0x0080),   ///< DIEBOLD
        FORMAT_DIEBOLDCO(0x0100), ///< DIEBOLDCO
        FORMAT_VISA3(0x0200),      ///< VISA3
        FORMAT_BANKSYS(0x0400),    ///< Bank system
        FORMAT_EMV(0x0800),      ///< EMV
        FORMAT_ISO3(0x2000),     ///< ISO9564 3
        FORMAT_AP(0x4000),      ///< AP
        FORMAT_ISO4(0x8000),    ///< ISO9564 4
        FORMAT_ISO0_Left(0x10000), ///< ISO9564 0 SM4 PAN padding Left
        FORMAT_SM4(0x20000);     //SM4 Alg 

        /**
         * enum value
         */
        private final int value;

        EPINFORMAT(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * MAC 算法
     */
    enum EMAC implements EnumMapper.IntegerEnum {
        MAC_X9(0x00),      ///< X9.9
        MAC_X919(0x01),     ///< X9.19
        MAC_PSAM(0x02),     ///< PSAM
        MAC_PBOC(0x03),    ///< PBOC
        MAC_CBC(0x04),      ///< CBC(ISO 16609)
        MAC_BANKSYS(0x05),   ///< Bank system, China pos union

        AES_MAC_CBC(0x10),    ///< AES CBC
        AES_MAC_PBOC(0x11),    ///< AES PBOC
        AES_HMAC(0x12), ///< AES HMAC with SHA256
        AES_CMAC(0x06), ///< AES-CMAC-PRF
        AES_XCBC(0x07),  ///< AES-XCBC-PRF-128
        SM4MAC_PBOC(0x08),  ///< PBOC
        SM4MAC_CBC(0x09), ///< SM4 CBC
        SM4MAC_BANKSYS(0x0A),  ///< SM4 China POS UnionPay

        DES_MAC_BANKSYS(0x51); ///< HL- 银商测试-TDES密钥只用DES计算

        /**
         * enum value
         */
        private final int value;

        EMAC(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 密钥属性
     */
    enum EKEYATTR implements EnumMapper.IntegerEnum {
        ATTR_SPECIAL(0x0000),  ///< special key(UID | UAK | KBPK | IMK)

        ATTR_DK(0x0001),  ///< DATA KEY(WFS_PIN_USECRYPT)
        ATTR_PK(0x0002),  ///< PIN KEY(WFS_PIN_USEFUNCTION)
        ATTR_AK(0x0004),  ///< MAC KEY(WFS_PIN_USEMACING)
        ATTR_MK(0x0020),  ///< MASTER KEY / MK only for MK(WFS_PIN_USEKEYENCKEY)
        ATTR_IV(0x0080),  ///< IV KEY(WFS_PIN_USESVENCKEY)

        ATTR_ANSTR31(0x0400),  ///< ANSTR31 MASTER KEY(WFS_PIN_USEANSTR31MASTER)
        ATTR_RESTRICTED(0x0800),  ///< Restricted MASTER KEY(WFS_PIN_USERESTRICTEDKEYENCKEY)
        ATTR_SM4(0x8000),  ///< China Secure Encryption Algorithm(SM4)

        ATTR_PINLOCAL(0x10000),  ///< PIN local offset(WFS_PIN_USEPINLOCAL)
        ATTR_RSAPUBLIC(0x20000),  ///< RSA public(WFS_PIN_USERSAPUBLIC)
        ATTR_RSAPRIVATE(0x40000),  ///< RSA private(WFS_PIN_USERSAPRIVATE)
        ATTR_CHIPINFO(0x100000),  ///< WFS_PIN_USECHIPINFO
        ATTR_CHIPPIN(0x200000),  ///< WFS_PIN_USECHIPPIN
        ATTR_CHIPPS(0x400000),  ///< WFS_PIN_USECHIPPS
        ATTR_CHIPMAC(0x800000),  ///< WFS_PIN_USECHIPMAC
        ATTR_CHIPLT(0x1000000),  ///< WFS_PIN_USECHIPLT
        ATTR_CHIPMACLZ(0x2000000),  ///< WFS_PIN_USECHIPMACLZ
        ATTR_CHIPMACAZ(0x4000000),  ///< WFS_PIN_USECHIPMACAZ
        ATTR_RSA_VERIFY(0x8000000),  ///< RSA public verify(WFS_PIN_USERSAPUBLICVERIFY)
        ATTR_RSA_SIGN(0x10000000),  ///< RSA private sign(WFS_PIN_USERSAPRIVATESIGN)
        ATTR_PINREMOTE(0x20000000),  ///< PIN remote(WFS_PIN_USEPINREMOTE)
        ATTR_SM2(0x80000000),  ///< China Secure Encryption Algorithm(SM2)

        ATTR_MPK(0x0020 | 0x0002),  ///< MASTER KEY only for PIN KEY
        ATTR_MDK(0x0020 | 0x0001),  ///< MASTER KEY only for DATA KEY
        ATTR_MAK(0x0020 | 0x0004),  ///< MASTER KEY only for MAC KEY
        ATTR_MIV(0x0020 | 0x0080),  ///< MASTER KEY only for IV KEY

        ATTR_WK(0x0002 | 0x0001 | 0x0004), ///< ATTR_DK | ATTR_AK | ATTR_PK
        ATTR_MWK(0x0020 | 0x0002 | 0x0001 | 0x0004); ///< ATTR_MK | ATTR_DK | ATTR_AK | ATTR_PK

        /**
         * enum value
         */
        private final int value;

        EKEYATTR(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 密钥设置模式
     */
    enum EKEYMODE implements EnumMapper.IntegerEnum {
        KEY_SET(0x30),   ///< It's equivalent to "combine" at some pinpad
        KEY_XOR(0x31),  ///< Key XOR
        KEY_XOR2(0x32),  ///< Key XOR 2
        KEY_XOR3(0x33); ///< Key XOR 3

        /**
         * enum value
         */
        private final int value;

        EKEYMODE(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 密钥校验值模式
     */
    enum EKCVMODE implements EnumMapper.IntegerEnum {
        KCVNONE(0x0),  ///< no KCV
        KCVSELF(0x1),  ///< key encrypt itself(first 8 char)
        KCVZERO(0x2);  ///< key encrypt 00000000

        /**
         * enum value
         */
        private final int value;

        EKCVMODE(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 键盘输入模式
     */
    enum ENTRYMODE implements EnumMapper.IntegerEnum {
        ENTRY_MODE_CLOSE(0x0), ///< Close entry
        ENTRY_MODE_TEXT(0x1), ///< Plain text entry
        ENTRY_MODE_PIN(0x2), ///< Pin mode entry
        ENTRY_MODE_KEY(0x3); ///< Key mode entry

        /**
         * enum value
         */
        private final int value;

        ENTRYMODE(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * 键盘按键声音
     */
    enum ESOUND implements EnumMapper.IntegerEnum {
        SOUND_CLOSE(0x0),  ///< Close
        SOUND_OPEN(0x1),  ///< Open
        SOUND_KEEP(0x2); ///< Keep

        /**
         * enum value
         */
        private final int value;

        ESOUND(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    /**
     * ECAPS
     */
    enum ECAPS implements EnumMapper.IntegerEnum {
        eCAP_RSA_HASHALG(0x0001),       ///< _pin_caps.dwRSAHashAlg, see the EHASH
        eCAP_KCVL(0x0002),              ///< _pin_caps.ucKCVL
        eCAP_UKO(0x0003),               ///< _pin_caps.wUserKeyOffset, don't change it unless you need to compatibility some project
        eCAP_SPLIT_CBS_MAC(0x0004),     ///< _pin_caps.bSplitBankSysMAC, default is TRUE
        eCAP_RSA_SIG_ALG(0x0005),       ///< _pin_caps.dwRSASignatureAlgorithm, see the ESIG_ALGORITHM
        eCAP_COMM_READ_TEXT(0x0006),  ///< _pin_caps.bCommandReadText, default is FASLE
        eCAP_SET_SN_SIGN_TAG(0x0007),  ///< _pin_caps.bSNSigTag, default define by epp
        eCAP_RSA_CRYPT_ALG(0x0008),  ///< _pin_caps.dwRSACryptAlgorithmg, default 1:RSA_PKCS1_PADDING see ras.h
        
        eCAP_EPP_VERSION_EX(0x0010), ///< ePIN_EPP Read version ex, default is TRUE
        eCAP_EPP_LOAD_IV(0x0011),  ///< ePIN_EPP Load IV, default is TRUE
        eCAP_EPP_CHECK_KEY_ID(0x0012),  ///< ePIN_EPP Check key ID or not, default is FALSE
        eCAP_EPP_OLD_VERSION(0x0013),  ///< ePIN_EPP SetKeyBoard version, default is TRUE

        eCAP_CHECK_KCV(0x0020),  ///< Check key check value(KCV) when load key, default is FALSE

        eCAP_DPASPA(0x0012),  ///< Control DPA/SPA, 0 is Disable, else is enable
        eCAP_CMDSEQ(0x2000),  ///< Command sequence add one every time, 0 is FALSE, else is TRUE, default is FALSE
        eCAP_MAPPINGPATH(0x3000);  ///< The path of enlarge key mapping table, maybe only path or include file name(*.dat) 

        /**
         * enum value
         */
        private final int value;

        ECAPS(int value) {
            this.value = value;
        }

        /**
         * Get this enum integer value.
         *
         * @return the integer value
         */

        @Override
        public int intValue() {
            return value;
        } // mapping function
    }

    int KEY_INVALID = 0xFFFF; // Plain master key
    int KEY_ID_OFFSET_PADK = 0x40; //pin mac data key offset
    int KEY_ID_OFFSET_SM4   = 1000; //sm4 offset

    /**
     * 打开设备.
     *
     * @param[in] ePort 端口类型， 具体请见 @see EPORT.
     * @param[in] eType 键盘类型， 具体请见 @see EPIN_TYPE.
     * @param[in] lpDescription 设备名称描述，具体请见 @see EPORT.
     * @param[in] eExtend 扩展参数，具体请见 @see EPIN_EXTEND.
     *
     *  @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long Open(EPORT ePort, EPIN_TYPE eType, String lpDescription, EPIN_EXTEND eExtend);

    /**
     * 关闭设备.
     *
     *  @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     */
    long Close();

    /**
     * 读取硬件版本信息.
     *
     * @param[out] pcVersion 版本信息, 信息长度不超过64字节.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long GetHardwareVersion(@Out byte[] pcVersion);

    /**
     * 读取硬件ID.
     *
     * @param[out] pcModelId 硬件ID, 信息长度不超过64字节.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long GetHardwareID(@Out byte[] pcModelId);

     /**
     * 读取序列号.
     *
     * @param[out] pcSn 序列Sn, 信息长度不超过64字节.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long GetHardwareSN(@Out byte[] pcSn);

    /**
     * 初始化.
     *
     * @param[in] mode 初始化模式, 默认0.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     */
    long Init(long mode);
    //long Init(@u_int8_t byte mode);

    /**
     * 复位.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     */
    long Reset();

    /**
     * 16进制字符串转换为2进制数组.
     *
     * @param[out] pBin 数组.
     * @param[in] dwBufLen "pBin" 数组长度.
     * @param[in] pHex 16进制字符串.
     * @param[in] dwLen "pHex"16进制字符串长度.
     *
     * @return 返回转换长度.
     *
     */
    long Soft_Hex2Bin(@Out byte[] pBin, long dwBufLen, String pHex, long dwLen);

    /**
     * 2进制数组换为16进制数组.
     *
     * @param[out] pHex 16进制数组.
     * @param[in] dwBufLen "pHex" 数组长度.
     * @param[in] pBin 2进制数组.
     * @param[in] dwLen "pBin" 数组长度.
     *
     * @return 返回转换长度.
     *
     */
    long Soft_Bin2Hex(@Out byte[] pHex, long dwBufLen, byte[] pBin, long dwLen);

    /**
     * 设置控制模式，取值由固件文档定义.
     *
     * @param[in] byControlMode 控制模式
     * @param[in] ControlCode 控制码
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long SetControlMode(byte byControlMode, short ControlCode);

    /**
     * 设置设备功能.
     *
     * @param[in] eCapsSwitch 功能选项, 具体请见 @see eCapsSwitch
     * @param[in] dwValue 功能值
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long SetCaps(ECAPS eCapsSwitch, long dwValue);

    /**
     * 设置设备功能.
     *
     * @param[in] eCapsSwitch 功能选项, 具体请见 @see eCapsSwitch
     * @param[in] dwValue 功能值
     * @param[in] lpReserve 保留其他
     * 
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long SetCapsEx(ECAPS eCapsSwitch, long dwValue, String lpReserve);

    /**
     * 开启键盘输入和控制键盘输入声音.
     *
     * @param[in] eSound 键盘输入声音，具体请见 @see ESOUND.
     * @param[in] eMode 键盘输入模式 具体请见 @see ENTRYMODE.
     * @param[in] dwDisableKey 控制禁止按键， 默认0.
     * @param[in] dwDisableFDK 控制进制功能键, 默认0.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long OpenKeyboardAndSound(ESOUND eSound, ENTRYMODE eMode, long dwDisableKey, long dwDisableFDK);

    /**
     * 读取输入键值.
     * FIXME: 此函数 "dwOutLen" 为引用类型, 可能会有问题，现在JNR不支持引用类型。
     *
     * @param[out] lpText 读取键值.
     * @param[in] dwOutLen 读取键盘长度.
     * @param[in] dwTimeOutMs 等待读取超时时间(ms).
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long ReadText(@Out byte[] lpText, @Out IntByReference dwOutLen, long dwTimeOutMs);

    /**
     * 下载密钥.
     *
     * @param[in] wKeyId 密钥ID.
     * @param[in] dwKeyAttr 密钥属性，可以组合, 具体请见 @see EKEYATTR.
     * @param[in] lpKey 密钥值.
     * @param[in] wKeyLen 密钥长度.
     * @param[in] wEnKey 加密密钥ID，值为0xFFFF表示明文下载.
     * @param[out] lpKCVRet 密钥校验值.
     * @param[in] eKCV 密钥校验值模式，具体请见 @see EKCVMODE.
     * @param[in] eMode 设置密钥模式， 具体请见 @see EKEYMODE.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    //long LoadKey(@u_int16_t int wKeyId, EKEYATTR dwKeyAttr, byte[] lpKey, @u_int16_t int wKeyLen, @u_int16_t int wEnKey,
    //             @Out byte[] lpKCVRet, EKCVMODE eKCV, EKEYMODE eMode);

    long LoadKey(int wKeyId, long dwKeyAttr, byte[] lpKey, int wKeyLen, int wEnKey,
                 @Out byte[] lpKCVRet, EKCVMODE eKCV, EKEYMODE eMode);

    /**
     * 开启PIN输入.
     *
     * @param[in] eSound 键盘输入声音，具体请见 @see ESOUND.
     * @param[in] MaxLen 最长输入PIN长度，默认6.
     * @param[in] MinLen 最短输入PIN长度，默认4.
     * @param[in] bAutoEnd 是否自动结束输入，默认 true.
     * @param[in] TimeOutS 等待按键输入超时(单位为S)，默认30s.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long StartPinInput(ESOUND eSound, byte MaxLen, byte MinLen, byte bAutoEnd, byte TimeOutS);

    /**
     * 获取 PinBlock.
     *
     * @param[in] wKeyId 密钥ID.
     * @param[in] PinLen PIN长度.
     * @param[in] ePinFormat PIN格式, 具体请见 @see EPINFORMAT.
     * @param[in] lpCardNo 卡号或者自定义数据.
     * @param[out] pPinBlock 获取的 PinBlock.
     * @param[out] wOutLen 获取的 PinBlock 长度.
     * @param[in] Padding 补充字符， 0x00-0x0F, 默认 0x0F.
     * @param[in] wEnKeyId 二次加密密钥ID；主密钥ID(一些键盘需要使用)， 0x00-0xFFFF.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    /*
    long GetPinBlock(int wKeyId, byte PinLen, EPINFORMAT ePinFormat, String lpCardNo, @Out byte[] pPinBlock,
                     @Out IntByReference wOutLen, byte Padding, int wEnKeyId);
    */
    long GetPinBlock(int wKeyId, byte PinLen, long nPinFormat, String lpCardNo, @Out byte[] pPinBlock,
                     @Out IntByReference wOutLen, byte Padding, int wEnKeyId);
    /**
     * 计算 MAC.
     *
     * @param[in] wKeyId 密钥ID.
     * @param[in] eMac MAC 算法，具体请见 @see EMAC.
     * @param[in] lpDataIn 输入需要计算 MAC 数据.
     * @param[in, out] dwInOutLen 输入: "lpDataIn" 输入长度.
     *                            输出: "lpOutData" 输出长度.
     * @param[out] lpOutData 获取的 MAC 值(DES/TDES: 8, SM4: 16).
     * @param[in] lpIVdata 一些算法加密使用的IV数据，若不需要 为 null.
     * @param[in] wIVid IV密钥ID；主密钥ID（一些键盘需要使用）, 0x00-0xFFFF.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    //long CalcMAC(int wKeyId, EMAC eMac, byte[] lpDataIn, @In @Out IntByReference dwInOutLen, @Out byte[] lpOutData, byte[] lpIVdata, int wIVid);
    long CalcMAC(int wKeyId, EMAC eMac, byte[] lpDataIn, @In @Out LongLongByReference dwInOutLen, @Out byte[] lpOutData, byte[] lpIVdata, int wIVid);

    /**
     * 加解密.
     *
     * @param[in] wKeyId 密钥ID.
     * @param[in] eMode  加解密算法，具体请见 @see ECRYPT.
     * @param[in] lpDataIn 输入需要加解密数据.
     * @param[in, out] dwInOutLen 输入: "lpDataIn" 输入长度.
     *                            输出: "lpOutData" 输出长度.
     * @param[out] lpOutData 返回的加解密数据.
     * @param[in] bEncrypt 1: 加密, 0: 解密.
     * @param[in] lpIVdata 一些算法加密使用的IV数据, 若不需要 为 null.
     * @param[in] wIVid IV密钥ID；主密钥ID（一些键盘需要使用）， 0x00-0xFFFF.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    //long Crypt(int wKeyId, ECRYPT eMode, byte[] lpDataIn, @In @Out IntByReference dwInOutLen, @Out byte[] lpOutData, byte bEncrypt, byte[] lpIVdata, int wIVid);
    long Crypt(int wKeyId, ECRYPT eMode, byte[] lpDataIn, @In @Out LongLongByReference dwInOutLen, 
            @Out byte[] lpOutData, byte bEncrypt, byte[] lpIVdata, int wIVid);

    /**
     * 软加解密.
     *
     * @param[in] eMode  加解密算法，具体请见 @see ECRYPT.
     * @param[in] lpDataIn 输入需要加解密数据.
     * @param[in, out] dwInOutLen 输入: "lpDataIn" 输入长度.
     *                            输出: "lpOut" 输出长度.
     * @param[in] lpKey 密钥.
     * @param[in] wKeyLen 密钥长度.
     * @param[in] lpEx 扩展数据. CBC 计算时候是 IV 数据
     * @param[in] wExLen 密钥长度. CBC 计算时候是 IV 数据长度
     * @param[out] lpOut 返回的加解密数据.
     * @param[in] bEncrypt 1: 加密, 0: 解密.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    long Soft_Crypt_Calc(ECRYPT eMode, byte[] lpDataIn, @In @Out LongLongByReference dwInOutLen, 
                byte[] lpKey, int wKeyLen, byte[] lpEx, int wExLen, @Out byte[] lpOut, byte bEncrypt);

    /**
     * 配置日志.
     *
     * @param[in] nlevel 日志等级(0: 开所有日志, 1: 开启Debug日志, 0x200: 关闭所有日志). @see LOG_LEVEL.
     * @param[in] szlogDir  日志目录.
     * @param[in] nSaveMode 日志保存模式，默认 -2.
     * @param[in] nExtend 扩展参数，默认 0.
     *
     * @return == 0 成功, != 0 失败, 具体原因请见 @see ERROR_CODE.
     *
     */
    void LogConfig(int nlevel, String szlogDir, int nSaveMode, int nExtend);
}
