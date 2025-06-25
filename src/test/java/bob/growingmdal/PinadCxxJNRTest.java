package bob.growingmdal;


import jnr.ffi.LibraryLoader;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.byref.LongLongByReference;

public class PinadCxxJNRTest {
    private volatile static boolean loaded = false;
    private static PinpadCJNR lib = null;
    private static long lastErrorCode; // Last error code
    private static PinpadCJNR.EPORT ePort;  // PINPAD Port type
    private static PinpadCJNR.EPIN_TYPE eType; // PINPAD Type
    private static String lpDescription; // PINPAD COM INFO
    private static PinpadCJNR.EPIN_EXTEND eExtend;  // PINPAD EXTEND FEATURE

    /**
     *  load library
     */
    public static boolean load() {
        synchronized (PinadCxxJNRTest.class) {
            if (loaded) return true;
            try {
                String libPath; // lib load path, must modify to you env. 加载库路径, 需要修改成自己的!
				// in linux os, every cpu architecture have its corresponding library, and related to JRE bits
                // 请注意, linux 系统下, 相应构架CPU有相应版本库, 并且和 JRE 位数也相关
                //libPath =  "/home/oyq/work/zt/source/epp/epp_lxl/gnu/x86_64";
                //libPath = "D:\\Work\\ZT\\lsh\\ZT\\2标准平台-20230613\\20211111\\bin\\";
                libPath = "D:\\Work\\ZT\\lsh\\ZT\\2标准平台-20230613\\20211111\\bin64\\";
                // 加载 libPinpadC.so 库, 注意相关依赖库 libZTPinpad.so 等... 可能需要放下系统加载目录 /lib 或者 /usr/lib 下
                // load library libPinpadC.so, maybe the dependent libraries like libZTPinpad.so should put to /lib or /usr/lib
                lib = LibraryLoader.create(PinpadCJNR.class).search(libPath).load("PinpadC");
                loaded = true;
            } catch (Exception e) {
                System.err.println("load error " + e);
            }
            return loaded;
        }
    }

    /**
     * init
     */
    public static void init() {
        lastErrorCode = 0;
        ePort = PinpadCJNR.EPORT.eCOM; // PINPAD Port type
        //ePort = PinpadCJNR.EPORT.eHID; // PINPAD Port type
        eType = PinpadCJNR.EPIN_TYPE.ePIN_EPP; // PINPAD Type
        lpDescription = "COM3:9600,N,8,1"; // PINPAD COM INFO
        //lpDescription = "/dev/ttyUSB0:9600,N,8,1"; // PINPAD COM INFO
        //lpDescription = "VID_23AB&PID_2005"; // PINPAD COM INFO
        eExtend = PinpadCJNR.EPIN_EXTEND.eEX_NONE; // PINPAD EXTEND FEATURE
        //eExtend =  PinpadCJNR.EPIN_EXTEND.eEx_2_10;
    }

    /**
     * open
     *
     * @return
     */
    public static boolean Open() {
        return (lastErrorCode = lib.Open(ePort, eType, lpDescription, eExtend)) == 0;
    }

    /**
     * close
     *
     * @return
     */
    public static boolean Close() {
        return (lastErrorCode = lib.Close()) == 0;
    }

    public static int StrBytesLen(byte [] data) {
        int len = 0;
        for (int i = 0; i < data.length; ++i) {
            if (data[i] == 0) { // 0
                break;
            } else {
                ++len;
            }
        }
        return len;
    }

    /**
     * get hardware version
     *
     * @return
     */
    public static String GetHardwareVersion() {
        byte [] sn = new byte[64];
        if ((lastErrorCode = lib.GetHardwareVersion(sn)) == 0)  {
            int len = StrBytesLen(sn);
            return new String(sn, 0, len);
        } else {
            return "";
        }
    }

    /**
     * set log
     *
     * @param nlevel
     */
    public static void LogConfig(int nlevel, String szlogDir) {
        lib.LogConfig(nlevel, szlogDir, -2, 0);
    }

    public static int Soft_Hex2Bin(byte[] pBin, int dwBufLen, String pHex, int dwLen) {
        return (int)lib.Soft_Hex2Bin(pBin, dwBufLen, pHex, dwLen);
    }

    public static int Soft_Bin2Hex(byte[] pHex, long dwBufLen, byte[] pBin, long dwLen) {
        return (int)lib.Soft_Bin2Hex(pHex, dwBufLen, pBin, dwLen);
    }

    public static boolean Init() {
        return ((lastErrorCode = lib.Init(0)) == 0);
    }

    public static boolean SetControlMode(int byControlMode, int ControlCode) {
        return ((lastErrorCode = lib.SetControlMode((byte)byControlMode, (short)ControlCode)) == 0);
    }

    public static boolean SetCaps(PinpadCJNR.ECAPS eCapsSwitch, long dwValue) {
        return ((lastErrorCode = lib.SetCaps(eCapsSwitch, dwValue)) == 0);
    }

    public static boolean SetCaps(PinpadCJNR.ECAPS eCapsSwitch, long dwValue, String lpReserve) {
        return ((lastErrorCode = lib.SetCapsEx(eCapsSwitch, dwValue, lpReserve)) == 0);
    }

    public static boolean OpenKeyboardAndSound(PinpadCJNR.ESOUND eSound, PinpadCJNR.ENTRYMODE eMode) {
        return ((lastErrorCode = lib.OpenKeyboardAndSound(eSound, eMode, 0, 0)) == 0);
    }

    public static boolean ReadText(byte[] lpText, IntByReference dwOutLen, long dwTimeOutMs) {
        return ((lastErrorCode = lib.ReadText(lpText, dwOutLen, dwTimeOutMs)) == 0);
    }

    /*
    public static boolean LoadKey(int wKeyId, PinpadCJNR.EKEYATTR dwKeyAttr, byte[] lpKey, int wKeyLen, int wEnKey, byte[] lpKCVRet, PinpadCJNR.EKCVMODE eKCV) {
        return ((lastErrorCode = lib.LoadKey(wKeyId, dwKeyAttr, lpKey, wKeyLen, wEnKey, lpKCVRet,
                eKCV, PinpadCJNR.EKEYMODE.KEY_SET)) == 0);
    }
    */

    public static boolean LoadKey(int wKeyId, long dwKeyAttr, byte[] lpKey, int wKeyLen, int wEnKey, byte[] lpKCVRet, PinpadCJNR.EKCVMODE eKCV) {
        return ((lastErrorCode = lib.LoadKey(wKeyId, dwKeyAttr, lpKey, wKeyLen, wEnKey, lpKCVRet,
                eKCV, PinpadCJNR.EKEYMODE.KEY_SET)) == 0);
    }

    public static boolean StartPinInput(PinpadCJNR.ESOUND eSound, int MaxLen, int MinLen, boolean bAutoEnd) {
        return ((lastErrorCode = lib.StartPinInput(eSound, (byte)MaxLen, (byte)MinLen, (bAutoEnd ? (byte)1 : (byte)0), (byte)30)) == 0);
    }

    /*
    public static boolean GetPinBlock(int wKeyId, byte PinLen, PinpadCJNR.EPINFORMAT ePinFormat, String lpCardNo, byte[] pPinBlock,
                     IntByReference wOutLen, int Padding, int wEnKeyId) {
        return ((lastErrorCode = lib.GetPinBlock(wKeyId, PinLen, ePinFormat, lpCardNo, pPinBlock, wOutLen,
                (byte)Padding, wEnKeyId)) == 0);
    }
    */

    public static boolean GetPinBlock(int wKeyId, byte PinLen, long nPinFormat, String lpCardNo, byte[] pPinBlock,
                     IntByReference wOutLen, int Padding, int wEnKeyId) {
        return ((lastErrorCode = lib.GetPinBlock(wKeyId, PinLen, nPinFormat, lpCardNo, pPinBlock, wOutLen,
                (byte)Padding, wEnKeyId)) == 0);
    }

    /*
    public static boolean CalcMAC(int wKeyId, PinpadCJNR.EMAC eMac, byte[] lpDataIn, IntByReference dwInOutLen, byte[] lpOutData, byte[] lpIVdata, int wIVid) {
        return ((lastErrorCode = lib.CalcMAC(wKeyId, eMac, lpDataIn, dwInOutLen, lpOutData, lpIVdata, wIVid)) == 0);
    }*/

    public static boolean CalcMAC(int wKeyId, PinpadCJNR.EMAC eMac, byte[] lpDataIn, LongLongByReference dwInOutLen, 
        byte[] lpOutData, byte[] lpIVdata, int wIVid) {
        return ((lastErrorCode = lib.CalcMAC(wKeyId, eMac, lpDataIn, dwInOutLen, lpOutData, lpIVdata, wIVid)) == 0);
    }

    /*
    public static boolean Crypt(int wKeyId, PinpadCJNR.ECRYPT eMode, byte[] lpDataIn, IntByReference dwInOutLen, byte[] lpOutData, boolean bEncrypt, byte[] lpIVdata, int wIVid) {
        return ((lastErrorCode = lib.Crypt(wKeyId, eMode, lpDataIn, dwInOutLen, lpOutData, (bEncrypt ? (byte)1 : (byte)0), lpIVdata, wIVid)) == 0);
    }*/

    public static boolean Crypt(int wKeyId, PinpadCJNR.ECRYPT eMode, byte[] lpDataIn, LongLongByReference dwInOutLen, 
            byte[] lpOutData, boolean bEncrypt, byte[] lpIVdata, int wIVid) {
        return ((lastErrorCode = lib.Crypt(wKeyId, eMode, lpDataIn, dwInOutLen, lpOutData, (bEncrypt ? (byte)1 : (byte)0), lpIVdata, wIVid)) == 0);
    }

    public static boolean Soft_Crypt_Calc(PinpadCJNR.ECRYPT eMode, byte[] lpDataIn, LongLongByReference dwInOutLen, 
            byte[] lpKey, int wKeyLen, byte[] lpEx, int wExLen, byte[] lpOut, boolean bEncrypt) {
        return ((lastErrorCode = lib.Soft_Crypt_Calc(eMode, lpDataIn, dwInOutLen, lpKey, wKeyLen, lpEx, wExLen, lpOut, (bEncrypt ? (byte)1 : (byte)0))) == 0);
    }

    public static long GetLastError() {
        return lastErrorCode;
    }
    public static void ShowErrorCode(String str) {
        System.err.println(str + " failed error code: " + lastErrorCode);
    }

    /*****************************************************************************
     * Function Description:  start text input and output the pinpad input
     * Parameter:
     * Return Value
     *****************************************************************************/
    public static void nTextInput() {
        byte[] bypText = new byte[64];
        int nInputLen = 18;//input length
        IntByReference byLen = new IntByReference();

        if (!OpenKeyboardAndSound(PinpadCJNR.ESOUND.SOUND_OPEN, PinpadCJNR.ENTRYMODE.ENTRY_MODE_TEXT)) {
            ShowErrorCode("OpenKeyboardAndSound");
            return;
        }

        System.out.println("Open Keyboard And Sound");
        System.out.println("Test the pinpad button, please check your KeyCodes.\nPlease input 18 char by pinpad!<<");

        while (0 < nInputLen)
        {
            ReadText(bypText, byLen, 500);

            for (int i = 0; i < byLen.getValue() && 0 < nInputLen; i++)
            {
                nInputLen--;
                switch (bypText[i])
                {
                    case 0x1B:
                        System.out.println("You pressed the [CANCEL] button.");
                        break;
                    case 0x08:
                        System.out.println("You pressed the [CLEAR] button.");
                        break;
                    case 0x0D:
                        System.out.println("You pressed the [ENTER] button.");
                        break;
                    case 0x2F:
                        System.out.println("You pressed the [BLACK] button.");
                        break;
                    case 0x2E:
                        System.out.println("You pressed the [.] button.");
                        break;
                    case 0x7F:
                        System.out.println("You pressed the [00] button.");
                        break;
                    default:
                        if (0x80 == (bypText[i] & 0xFF) || 0x81 == (bypText[i] & 0xFF) || 0x82 == (bypText[i] & 0xFF))
                        {
                            System.out.println("Input error, maybe the input "+  (bypText[i] & 0xFF) 
                                    + " [0x" + Integer.toHexString((bypText[i] & 0xFF)) + "]"
                                    + " is TIMEOUT or the button is stuck.\n");
                            System.out.println("The input is going to close.\n");
                            nInputLen = 0;
                        }
                        else if (0 == bypText[i])//no input, for some pinpad
                        {
                            nInputLen++;
                        }
                        else
                        {
                            System.out.println("You pressed the " + (char)bypText[i]
                                + " [0x" + Integer.toHexString((bypText[i] & 0xFF)) + "]"
                                + " button.");
                        }
                        break;
                }
            }
        }

        //close the input
        if (!OpenKeyboardAndSound(PinpadCJNR.ESOUND.SOUND_CLOSE, PinpadCJNR.ENTRYMODE.ENTRY_MODE_CLOSE)) {
            ShowErrorCode("OpenKeyboardAndSound");
            return;
        }

        System.out.println("Open Keyboard And Sound SOUND_CLOSE  ENTRY_MODE_CLOSE\n");
    }

    /*****************************************************************************
     * Function Description: TDES, load master key, pin key, mac key and data key into pinpad
     * Parameter:
     * iMKID:   master key id(0-0x1F)
     * iWKDataID: work data key id(0-0x04)
     * iWKPinID:  work pin key id(0-0x04)
     * iWKPinID:  work mac key id(0-0x04)
     * bExtend:   whether or not enable extend function
     * bWorkKeyIDOffset:  whether or not work key use offset which could not need master key when use this key
     * Return Value:
     *****************************************************************************/
    public static void nLoadKey(int iMKID, int iWKDataID, int iWKPinID, int iWKMacID, 
            boolean bExtend, boolean bWorkKeyIDOffset) {
        int nKeyLen      = 0;
        byte byKcvLen    = 4;
        String strKeyHex = "";              //key Hex string
        byte[] bypKeyBin = new byte[48 + 1];//key hex string , for  LoadKey , the '1' is for the string end '\0'
        byte[] bypKcvBin = new byte[8 + 1];//kcv hex string , from LoadKey
        byte[] bypKcvHex = new byte[16 + 1];//kcv Hex string , for show

        // Set KCV length
        SetCaps(PinpadCJNR.ECAPS.eCAP_KCVL, byKcvLen);

        // set KCV zero
        SetControlMode(0x0B, 0x00); 
        // set KCV self
        //SetControlMode(0x0B, 0x01); 

        if (bExtend)
	    {
		    bWorkKeyIDOffset = false;
	    }

        strKeyHex = "CE31B0C2D38034706861B0AE86CE91D0";
        //transform key value from ASCII TO HEX
        nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());

        //load master key 1
        System.out.println("LoadKey MASTER KEY " + iMKID + " ...");
        
        long nKeyAttr = PinpadCJNR.EKEYATTR.ATTR_MK.intValue();
        //load master key 1 , EnKeyId is 0xFFFF(load clear key) ,set key , KCV calculate encrypt zero
        if (!LoadKey(iMKID, nKeyAttr, bypKeyBin, nKeyLen, 0xFFFF, bypKcvBin, PinpadCJNR.EKCVMODE.KCVZERO)) {
            ShowErrorCode("LoadKey");
            return;
        }
         
        //transform KCV value from HEX TO ASCII
        nKeyLen = Soft_Bin2Hex(bypKcvHex, 16, bypKcvBin, byKcvLen);

        //show KCV
        System.out.println("LoadKey MASTER KEY " + iMKID + " Return is " + GetLastError() + " KCV is " + new String(bypKcvHex, 0, nKeyLen) + "\n");

        if (bWorkKeyIDOffset) 
	    {
		    iWKPinID += PinpadCJNR.KEY_ID_OFFSET_PADK;
	    }

        //load pin key 2 , master key 1
        System.out.println("LoadKey PIN KEY 2 MASTER KEY 1 ...");
        strKeyHex = "358DA0A6507D8464E34A4EEDA6CD7740";
        nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());

        nKeyAttr = PinpadCJNR.EKEYATTR.ATTR_PK.intValue();
        //load PIN key 2 , EnKeyId is 1 ,set key , KCV calculate encrypt zero
        if (!LoadKey(iWKPinID, nKeyAttr, bypKeyBin, nKeyLen, iMKID, bypKcvBin, PinpadCJNR.EKCVMODE.KCVZERO)) {
            ShowErrorCode("LoadKey");
            return;
        }

        nKeyLen = Soft_Bin2Hex(bypKcvHex, 16, bypKcvBin, byKcvLen);
        System.out.println("LoadKey PIN KEY " + iWKPinID + " Return is " + GetLastError() + " KCV is " + new String(bypKcvHex, 0, nKeyLen) + "\n");

        if (bWorkKeyIDOffset) 
	    {
		    iWKMacID += PinpadCJNR.KEY_ID_OFFSET_PADK;
	    }

        //load mac key 3 , master key 1
        System.out.println("LoadKey MAC KEY " + iWKMacID + " MASTER KEY " + iMKID + " ...");
        strKeyHex = "863A1602089DFB5B";
        nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());

        nKeyAttr = PinpadCJNR.EKEYATTR.ATTR_AK.intValue();
        if (!LoadKey(iWKMacID, nKeyAttr, bypKeyBin, nKeyLen, iMKID, bypKcvBin, PinpadCJNR.EKCVMODE.KCVZERO)) {
            ShowErrorCode("LoadKey");
            return;
        }
        nKeyLen = Soft_Bin2Hex(bypKcvHex, 16, bypKcvBin, byKcvLen);
        System.out.println("LoadKey MAC KEY " + iWKMacID + " Return is " + GetLastError() + "  KCV is " + new String(bypKcvHex, 0, nKeyLen) + "\n");

        if (bWorkKeyIDOffset) 
	    {
		    iWKDataID += PinpadCJNR.KEY_ID_OFFSET_PADK;
	    }

        //load data key 0, master key 1
        System.out.println("LoadKey DATA KEY " + iWKDataID + " MASTER KEY " + iMKID + " ...");
        strKeyHex = "987DA0A6507D8464E34A4EEDA6CD7740";
        nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());

        nKeyAttr = PinpadCJNR.EKEYATTR.ATTR_DK.intValue();

        if (!LoadKey(iWKDataID, nKeyAttr, bypKeyBin, nKeyLen, iMKID, bypKcvBin, PinpadCJNR.EKCVMODE.KCVZERO)) {
            ShowErrorCode("LoadKey");
            return;
        }

        nKeyLen = Soft_Bin2Hex(bypKcvHex, 16, bypKcvBin, byKcvLen);
        System.out.println("LoadKey DATA KEY " + iWKDataID + " Return is " + GetLastError() + "  KCV is "+ new String(bypKcvHex, 0, nKeyLen) +"\n");
    }

    /*****************************************************************************
     * Function Description:  start pin input , get the input from pinpad (digit as "*" or "0") and cul the pinblock
     * Parameter: 
     * iWKID:  work key id(0-0x04)
     * iMKID:   master key(enkey)
     * bExtend:  whether or not enable extend function
     * bWorkKeyIDOffset:  whether or not work key use offset which could not need master key when use this key
     * Return Value: 
     *****************************************************************************/
    public static void nPinBlock(int iWKID, int iMKID, boolean bExtend, boolean bWorkKeyIDOffset)
    {
        int byPinLen = 0; // input pin length , for GetPinBlock
        IntByReference byLen = new IntByReference();
        String strCardNumber  = "";//card number , for GetPinBlock
        byte[] bypPinInput    = new byte[16 + 1];
        byte[] bypPinBlockBin = new byte[8 + 1];//pinblock hex string , from GetPinBlock
        byte[] bypPinBlockHex = new byte[16 + 1];//pinblock ascii string , for show
        int i = 0;

        //SetControlMode(0xFF, 0x20); // backspace 0x08 key as clear key function, delete all pin, if enter 5 pin, will recv 5 size 0f 0x08
        //SetControlMode(0xFF, 0x21); // backspace 0x08 key as backsapce, delete one pin, if not enter pin, also recv an 0x08.
        //SetControlMode(0xFF, 0x22); // backspace 0x08 key as clear key function, delete all pin, recv an 0x08, some device not support this

        //open pin input , pen sound , max len is 6 , min len is 4 , auto ended if the input len is equal the max len 
        if (!StartPinInput(PinpadCJNR.ESOUND.SOUND_OPEN, 6, 4, true)) {
            ShowErrorCode("StartPinInput");
            return;
        }

        System.out.println("StartPinInput Return is "+ GetLastError());

        System.out.println("Please input PIN.<<");

        int nInputRet = 0;//for end the pin inpt , press the CANCEL button or ENTER button with len bewtten min len and max len

        while (0 <= nInputRet)//the pin input will end when input ENTER or the StartPinInput auto end it
        {
            ReadText(bypPinInput, byLen, 500);

            for (i = 0; i < byLen.getValue(); i++)
            {
                if (0x0D == bypPinInput[i])//the pin input will end when input ENTER or the StartPinInput auto end it
                {
                    System.out.println("You pressed the [ENTER] button.");
                    if (byPinLen >= 4)
                    {
                        System.out.println("The pin input is end.");
                        nInputRet = -1;//break the input,end the pin input and calculate pinblock
                        break;
                    }
                    System.out.println("The input pin is insufficient please continue the pin input.");
                }
                else if (0x1B == bypPinInput[i])
                {
                    System.out.println("You pressed the [CANCEL] button.");
                    System.out.println("The input is going to close.");
                    nInputRet = -2;//break the input,end the pin input with no pinblock
                    break;
                }
                else if (0x80 == (bypPinInput[i] & 0xFF) || 0x81 == (bypPinInput[i] & 0xFF) || 0x82 == (bypPinInput[i] & 0xFF))
                {
                    System.out.println("Input error, maybe the input " + (bypPinInput[i] & 0xFF) 
                        + " [0x" + Integer.toHexString((bypPinInput[i] & 0xFF)) + "]"
                        + " is TIMEOUT or the button is stuck.");
                    System.out.println("The input is going to close.\n");
                    nInputRet = -3;//end the pin input with no pinblock
                    break;
                }
                else if (0x08 == bypPinInput[i])
                {
                    System.out.println("You pressed the [CLEAR] or [BACKSPACE] button.");
                    //close the input and reopen the pin input
                    OpenKeyboardAndSound(PinpadCJNR.ESOUND.SOUND_CLOSE, PinpadCJNR.ENTRYMODE.ENTRY_MODE_CLOSE);
                    System.out.println("Open Keyboard And Sound SOUND_CLOSE  ENTRY_MODE_CLOSE Return is " + GetLastError());

                    StartPinInput(PinpadCJNR.ESOUND.SOUND_OPEN, 6, 4, true);
                    System.out.println("StartPinInput Return is " + GetLastError() + "\n");

                    byPinLen = 0;
                }
                else if (0x20 == bypPinInput[i])
                {
                    System.out.println("You pressed the [BLACK] button.");
                }
                else if (0x2E == bypPinInput[i])
                {
                    System.out.println("You pressed the [.] button.");
                }
                else if (0x7F == bypPinInput[i])
                {
                    System.out.println("You pressed the [00] button.");
                }
                else//count the PIN input, some pinpad's PIN return code can change
                {
                    System.out.println("You pressed the " + (char)bypPinInput[i] 
                        + " [0x" + Integer.toHexString((bypPinInput[i] & 0xFF)) + "]"
                        + " button.");
                    byPinLen++;
                }
            }
        }

        //close the input
        if (!OpenKeyboardAndSound(PinpadCJNR.ESOUND.SOUND_CLOSE, PinpadCJNR.ENTRYMODE.ENTRY_MODE_CLOSE)) {
            ShowErrorCode("OpenKeyboardAndSound");
            return;
        }

        System.out.println("Open Keyboard And Sound SOUND_CLOSE ENTRY_MODE_CLOSE Return is " + GetLastError());

        if (-1 == nInputRet)//the pin input was ended by ENTER , get the pinblock
        {
            IntByReference byPinblockLen = new IntByReference();
            strCardNumber = "6217000130000004332";
            
            if (bExtend)
		    {
			    bWorkKeyIDOffset = false;
                iMKID = PinpadCJNR.KEY_INVALID;
		    }

		    if (bWorkKeyIDOffset)
		    {
			    iWKID += PinpadCJNR.KEY_ID_OFFSET_PADK;
			    iMKID = 0;
		    }

             System.out.println("GetPinBlock Key " + iWKID + " EnKey " + iMKID);

            //calculate pinblock , use key 2 , pin len is byPinLen , pinblock format is FORMAT_ISO0 , the key's enkey is 1
            long nFormat = PinpadCJNR.EPINFORMAT.FORMAT_ISO0.intValue();
           
            if (!GetPinBlock(iWKID, (byte)byPinLen, nFormat, strCardNumber, bypPinBlockBin, byPinblockLen, 0xF, iMKID)) {
                ShowErrorCode("GetPinBlock");
                return;
            }
            //transform pinblock from HEX TO ASCII
            byPinLen = Soft_Bin2Hex(bypPinBlockHex, 16, bypPinBlockBin, byPinblockLen.getValue());
            System.out.println("GetPinBlock Return is " + GetLastError() + " PINBLOCK is "+ new String(bypPinBlockHex, 0, byPinLen)+ "\n");
        }
    }

    /*****************************************************************************
     * Function Description:  calculate MAC by pinpad
     * Parameter: 
     * iWKID:  work key id(0-0x04)
     * iMKID:   master key(enkey)
     * bExtend:  whether or not enable extend function
     * bWorkKeyIDOffset:  whether or not work key use offset which could not need master key when use this key
     * Return Value: 
     *****************************************************************************/
    public static void nMAC(int iWKID, int iMKID, boolean bExtend, boolean bWorkKeyIDOffset)
    {
        byte[] bypDataBin = new byte[1000];  //data hex string , for CalcMAC
        byte[] bypMacBin  = new byte[8 + 1]; //MAC hex string , form CalcMAC
        byte[] cpMacAsc = new byte[16 + 1];//MAC Hex string , for show
        byte[] bypIVHex = new byte[8 + 1]; //IV Hex data

        String strDataHex = "0200702006C020C098111962170001300000043320000000000000000001000337051000010012376217000130000004332D220262023310200000303030303232363431303531333031353436323030303131353642EA1CEBCF168FE32600000000000000001322000001000500";
        //transform data from ASCII TO HEX
        int iDataLen = Soft_Hex2Bin(bypDataBin, 1000, strDataHex, strDataHex.length());
        //IntByReference byDataLen = new IntByReference(iDataLen);//length of data for calculate MAC
        LongLongByReference byDataLen = new LongLongByReference(iDataLen);

        // IV string for CBC
        String strIV = "0000000000000000";
        iDataLen = Soft_Hex2Bin(bypIVHex, 8, strIV, strIV.length());

        //
        if (bExtend)
        {
            bWorkKeyIDOffset = false;
            iMKID = PinpadCJNR.KEY_INVALID;
        }
	
        if (bWorkKeyIDOffset)
        {
            iWKID += PinpadCJNR.KEY_ID_OFFSET_PADK;
            iMKID = 0;
        }

        //calculate ndataLen length cpDataHex use MAC_BANKSYS , with key 3 and no IV
        if (!CalcMAC(iWKID, PinpadCJNR.EMAC.MAC_BANKSYS, bypDataBin, byDataLen, bypMacBin, null, iMKID)) {
            ShowErrorCode("CalcMAC");
            return;
        }
        //transform MAC from HEX TO ASCII
        iDataLen = Soft_Bin2Hex(cpMacAsc, 16, bypMacBin, byDataLen.getValue());
        System.out.println("MAC Return is " + GetLastError() + " MAC is " + new String(cpMacAsc, 0, iDataLen) + "\n");
    }

    /*****************************************************************************
     * Function Description:  encrypt and decrypt by pinpad
     * Parameter: 
     * iWKID:  work key id(0-0x04)
     * iMKID:   master key(enkey)
     * bExtend:  whether or not enable extend function
     * bWorkKeyIDOffset:  whether or not work key use offset which could not need master key when use this key
     * Return Value: 
     *****************************************************************************/
    public static void nCrypt(int iWKID, int iMKID, boolean bExtend, boolean bWorkKeyIDOffset)
    {
        byte[] bypDataBin   = new byte[16 + 1];//data hex string, for crypt
        byte[] bypResultBin = new byte[16 + 1]; //crypt result hex string , from crypt , the len should increase with the data len
        byte[] bypResultHex = new byte[32 + 1];//crypt result Hex string , for show , the len should increase with the data len
        String strDataHex = "";

        byte[] bypIVHex = new byte[8 + 1]; //IV Hex data
        // IV string for CBC
        String strIV = "0000000000000000";
        int iDataLen = Soft_Hex2Bin(bypIVHex, 8, strIV, strIV.length());

        if (bExtend)
        {
            bWorkKeyIDOffset = false;
            iMKID = PinpadCJNR.KEY_INVALID;
        }

        if (bWorkKeyIDOffset)
        {
            iWKID += PinpadCJNR.KEY_ID_OFFSET_PADK;
            iMKID = 0;
        }
        
        //encypt the data
        strDataHex = "EF34A0A6507D8464E34A4EEDA6CD7740";
        System.out.println("ENCRYPT " + strDataHex);

        iDataLen = Soft_Hex2Bin(bypDataBin, 16, strDataHex, strDataHex.length());

        //IntByReference byDataLen = new IntByReference(iDataLen);
        LongLongByReference byDataLen = new LongLongByReference(iDataLen);

        //Encrypt cpDataHex with key 0 and no IV by CRYPT_TRIDESECB
        if (!Crypt(iWKID, PinpadCJNR.ECRYPT.CRYPT_TRIDESECB, bypDataBin, byDataLen, bypResultBin, true, null, iMKID)) {
            ShowErrorCode("Crypt");
            return;
        }
        iDataLen = Soft_Bin2Hex(bypResultHex, 32, bypResultBin, byDataLen.getValue());
        System.out.println("Return is " + GetLastError() + " ciphertext is "+ new String(bypResultHex, 0, iDataLen) + "\n");
        //decrypt the data
        System.out.println("DECRYPT " + new String(bypResultHex, 0, iDataLen));

        System.arraycopy(bypResultBin, 0, bypDataBin, 0, bypResultBin.length);

        //iDataLen = byDataLen.getValue();
        long nDataLen = byDataLen.getValue();//(bypDataBin.length - 1);

        byDataLen = null;
        //byDataLen = new IntByReference(iDataLen);
        byDataLen = new LongLongByReference(nDataLen);

        //decrypt cpDataHex with key 0 and no IV by CRYPT_TRIDESECB
        if (!Crypt(iWKID, PinpadCJNR.ECRYPT.CRYPT_TRIDESECB, bypDataBin, byDataLen, bypResultBin, false, null, iMKID)) {
            ShowErrorCode("Crypt");
            return;
        }
        iDataLen = Soft_Bin2Hex(bypResultHex, 32, bypResultBin, byDataLen.getValue());
        System.out.println("Return is "+ GetLastError() +" cleartext is " + new String(bypResultHex, 0, iDataLen) + "\n");
    }
    
    /*****************************************************************************
     * Function Description: soft encrypt and decrypt by pinpad
     * Parameter: 
     * Return Value: 
     *****************************************************************************/
    public static void nSoft_Crypt()
    {
        byte[] bypKeyBin = new byte [48 + 1]; // key
        byte[] bypDataBin   = new byte[16 + 1];//data hex string, for crypt
        byte[] bypResultBin = new byte[16 + 1]; //crypt result hex string , from crypt , the len should increase with the data len
        byte[] bypResultHex = new byte[32 + 1];//crypt result Hex string , for show , the len should increase with the data len
        String strDataHex = "";
        
        String strKeyHex = "CE31B0C2D38034706861B0AE86CE91D0";
        //transform key value from ASCII TO HEX
        int nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());

        //encypt the data
        strDataHex = "EF34A0A6507D8464E34A4EEDA6CD7740";
        System.out.println("Soft ENCRYPT " + strDataHex);

        int iDataLen = Soft_Hex2Bin(bypDataBin, 16, strDataHex, strDataHex.length());

        //IntByReference byDataLen = new IntByReference(iDataLen);
        LongLongByReference byDataLen = new LongLongByReference(iDataLen);

        //Encrypt cpDataHex with key 0 and no IV by CRYPT_DESECB
        Soft_Crypt_Calc(PinpadCJNR.ECRYPT.CRYPT_DESECB, bypDataBin, byDataLen, bypKeyBin, nKeyLen, null, 0, bypResultBin, true);
        iDataLen = Soft_Bin2Hex(bypResultHex, 32, bypResultBin, byDataLen.getValue());
        System.out.println("Return is " + GetLastError() + " ciphertext is "+ new String(bypResultHex, 0, iDataLen) + "\n");
        //decrypt the data
        System.out.println("Soft DECRYPT " + new String(bypResultHex, 0, iDataLen));

        System.arraycopy(bypResultBin, 0, bypDataBin, 0, bypResultBin.length);

        iDataLen = (bypDataBin.length - 1);

        byDataLen = null;
        //byDataLen = new IntByReference(iDataLen);
        byDataLen = new LongLongByReference(iDataLen);

        //decrypt cpDataHex with key 0 and no IV by CRYPT_DESECB
        Soft_Crypt_Calc(PinpadCJNR.ECRYPT.CRYPT_DESECB, bypDataBin, byDataLen, bypKeyBin, nKeyLen, null, 0, bypResultBin, false);
        iDataLen = Soft_Bin2Hex(bypResultHex, 32, bypResultBin, byDataLen.getValue());
        System.out.println("Return is "+ GetLastError() +" cleartext is " + new String(bypResultHex, 0, iDataLen) + "\n");
    }

     /*****************************************************************************
     * Function Description: SM4, load master key, pin key, mac key and data key into pinpad
     * iMKID:   master key id(0-0x1F)
     * iWKDataID: work data key id(0-0x04)
     * iWKPinID:  work pin key id(0-0x04)
     * iWKPinID:  work mac key id(0-0x04)
     * bExtend:   whether or not enable extend function
     * bWorkKeyIDOffset:  whether or not work key use offset which could not need master key when use this key
     * bSM4KeyIDOffset:  whether or not work key use SM4 offset
     * Return Value:
     *****************************************************************************/
    public static void nSM4LoadKey(int iMKID, int iWKDataID, int iWKPinID, int iWKMacID, 
                boolean bExtend, boolean bWorkKeyIDOffset, boolean bSM4KeyIDOffset) {
        int nKeyLen      = 0;
        byte byKcvLen    = 4; // kcv length
        String strKeyHex = "";              //key Hex string
        byte[] bypKeyBin = new byte[48 + 1];//key hex string , for  LoadKey , the '1' is for the string end '\0'
        byte[] bypKcvBin = new byte[16 + 1];//kcv hex string , from LoadKey
        byte[] bypKcvHex = new byte[16 + 1];//kcv Hex string , for show

        if (bExtend)
        {
            bWorkKeyIDOffset = false;
            bSM4KeyIDOffset = false;
        }

        if (bSM4KeyIDOffset)
        {
            iMKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
        }
        
        // Set KCV length
        SetCaps(PinpadCJNR.ECAPS.eCAP_KCVL, byKcvLen);

        // set KCV zero
        SetControlMode(0x0B, 0x00); 

        strKeyHex = "EEF3602DDE149C6CF31EDBA5D24E788B";
        //transform key value from ASCII TO HEX
        nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());

        //load master key 1
        System.out.println("SM4 LoadKey MASTER KEY " + iMKID + " ...");
       
        //load master key 1 , EnKeyId is 0xFFFF(load clear key) ,set key , KCV calculate encrypt zero
        long nKeyAttr = (PinpadCJNR.EKEYATTR.ATTR_MK.intValue() | PinpadCJNR.EKEYATTR.ATTR_SM4.intValue());
        if (!LoadKey(iMKID, nKeyAttr, bypKeyBin, nKeyLen, 0xFFFF, bypKcvBin, PinpadCJNR.EKCVMODE.KCVZERO)) {
            ShowErrorCode("SM4 LoadKey");
            return;
        }
         
        //transform KCV value from HEX TO ASCII
        nKeyLen = Soft_Bin2Hex(bypKcvHex, 16, bypKcvBin, byKcvLen);

        //show KCV
        System.out.println("SM4 LoadKey MASTER KEY " + iMKID + " Return is " + GetLastError() + " KCV is " + new String(bypKcvHex, 0, nKeyLen) + "\n");

        if (bWorkKeyIDOffset) 
        {
            iWKPinID += PinpadCJNR.KEY_ID_OFFSET_PADK;
        }

        if (bSM4KeyIDOffset)
        {
            iWKPinID += PinpadCJNR.KEY_ID_OFFSET_SM4;
            //iMKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
        }

        //load pin key 2 , master key 1
        System.out.println("SM4 LoadKey PIN KEY " + iWKPinID + " MASTER KEY " + iMKID + " ...");
        strKeyHex = "EEF3602DDE149C6CF31EDBA5D24E788B";
        nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());
        //load PIN key 2 , EnKeyId is 1 ,set key , KCV calculate encrypt zero
        nKeyAttr = (PinpadCJNR.EKEYATTR.ATTR_PK.intValue() | PinpadCJNR.EKEYATTR.ATTR_SM4.intValue());
        if (!LoadKey(iWKPinID, nKeyAttr, bypKeyBin, nKeyLen, iMKID, bypKcvBin, PinpadCJNR.EKCVMODE.KCVZERO)) {
            ShowErrorCode("SM4 LoadKey");
            return;
        }

        nKeyLen = Soft_Bin2Hex(bypKcvHex, 16, bypKcvBin, byKcvLen);
        System.out.println("SM4 LoadKey PIN KEY " + iWKPinID + " Return is " + GetLastError() + " KCV is " + new String(bypKcvHex, 0, nKeyLen) + "\n");

        if (bWorkKeyIDOffset) 
        {
            iWKMacID += PinpadCJNR.KEY_ID_OFFSET_PADK;
        }

        if (bSM4KeyIDOffset)
        {
            iWKMacID += PinpadCJNR.KEY_ID_OFFSET_SM4;
            //iMKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
        }

        //load mac key 3 , master key 1
        System.out.println("SM4 LoadKey MAC KEY " + iWKMacID + " MASTER KEY " + iMKID + " ...");
        strKeyHex = "CF6CA84D069FD1EDC118D69BF9BA7D91";
        nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());
        nKeyAttr = (PinpadCJNR.EKEYATTR.ATTR_AK.intValue() | PinpadCJNR.EKEYATTR.ATTR_SM4.intValue());
        if (!LoadKey(iWKMacID, nKeyAttr, bypKeyBin, nKeyLen, iMKID, bypKcvBin, PinpadCJNR.EKCVMODE.KCVZERO)) {
            ShowErrorCode("SM4 LoadKey");
            return;
        }
        nKeyLen = Soft_Bin2Hex(bypKcvHex, 16, bypKcvBin, byKcvLen);
        System.out.println("SM4 LoadKey MAC KEY " + iWKMacID + " Return is " + GetLastError() + "  KCV is " + new String(bypKcvHex, 0, nKeyLen) + "\n");

        if (bWorkKeyIDOffset) 
        {
            iWKDataID += PinpadCJNR.KEY_ID_OFFSET_PADK;
        }

        if (bSM4KeyIDOffset)
        {
            iWKDataID += PinpadCJNR.KEY_ID_OFFSET_SM4;
            //iMKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
        }

        //load data key 0, master key 1
        System.out.println("SM4 LoadKey DATA KEY " + iWKDataID + " MASTER KEY " + iMKID + " ...");
        strKeyHex = "987DA0A6507D8464E34A4EEDA6CD7740";
        nKeyLen = Soft_Hex2Bin(bypKeyBin, 48, strKeyHex, strKeyHex.length());
        nKeyAttr = (PinpadCJNR.EKEYATTR.ATTR_DK.intValue() | PinpadCJNR.EKEYATTR.ATTR_SM4.intValue());
        if (!LoadKey(iWKDataID, nKeyAttr, bypKeyBin, nKeyLen, iMKID, bypKcvBin, PinpadCJNR.EKCVMODE.KCVZERO)) {
            ShowErrorCode("SM4 LoadKey");
            return;
        }

        nKeyLen = Soft_Bin2Hex(bypKcvHex, 16, bypKcvBin, byKcvLen);
        System.out.println("SM4 LoadKey DATA KEY " + iWKDataID + " Return is " + GetLastError() + "  KCV is "+ new String(bypKcvHex, 0, nKeyLen) +"\n");
    }

    /*****************************************************************************
     * Function Description: SM4, start pin input , get the input from pinpad (digit as "*" or "0") and cul the pinblock
     * Parameter: 
     * iWKID: work  key id(0-0x04)
     * iMKID:  master key id(enkey)
     * bExtend:   whether or not enable extend function
     * bWorkKeyIDOffset:  whether or not work key use offset which could not need master key when use this key
     * bSM4KeyIDOffset:  whether or not work key use SM4 offset
     * Return Value: 
     *****************************************************************************/
    public static void nSM4PinBlock(int iWKID, int iMKID, boolean bExtend, boolean bWorkKeyIDOffset, boolean bSM4KeyIDOffset)
    {
        int byPinLen = 0; // input pin length , for GetPinBlock
        IntByReference byLen = new IntByReference();
        String strCardNumber  = "";//card number , for GetPinBlock
        byte[] bypPinInput    = new byte[16 + 1];
        byte[] bypPinBlockBin = new byte[16 + 1];//pinblock hex string , from GetPinBlock
        byte[] bypPinBlockHex = new byte[32 + 1];//pinblock ascii string , for show
        int i = 0;

        //SetControlMode(0xFF, 0x20); // backspace 0x08 key as clear key function, delete all pin, if enter 5 pin, will recv 5 size 0f 0x08
        //SetControlMode(0xFF, 0x21); // backspace 0x08 key as backsapce, delete one pin, if not enter pin, also recv an 0x08.
        //SetControlMode(0xFF, 0x22); // backspace 0x08 key as clear key function, delete all pin, recv an 0x08, some device not support this

        //open pin input , pen sound , max len is 6 , min len is 4 , auto ended if the input len is equal the max len 
        if (!StartPinInput(PinpadCJNR.ESOUND.SOUND_OPEN, 6, 4, true)) {
            ShowErrorCode("StartPinInput");
            return;
        }

        System.out.println("StartPinInput Return is "+ GetLastError());

        System.out.println("Please input PIN.<<");

        int nInputRet = 0;//for end the pin inpt , press the CANCEL button or ENTER button with len bewtten min len and max len

        while (0 <= nInputRet)//the pin input will end when input ENTER or the StartPinInput auto end it
        {
            ReadText(bypPinInput, byLen, 500);

            for (i = 0; i < byLen.getValue(); i++)
            {
                if (0x0D == bypPinInput[i])//the pin input will end when input ENTER or the StartPinInput auto end it
                {
                    System.out.println("You pressed the [ENTER] button.");
                    if (byPinLen >= 4)
                    {
                        System.out.println("The pin input is end.");
                        nInputRet = -1;//break the input,end the pin input and calculate pinblock
                        break;
                    }
                    System.out.println("The input pin is insufficient please continue the pin input.");
                }
                else if (0x1B == bypPinInput[i])
                {
                    System.out.println("You pressed the [CANCEL] button.");
                    System.out.println("The input is going to close.");
                    nInputRet = -2;//break the input,end the pin input with no pinblock
                    break;
                }
                else if (0x80 == (bypPinInput[i] & 0xFF) || 0x81 == (bypPinInput[i] & 0xFF) || 0x82 == (bypPinInput[i] & 0xFF))
                {
                    System.out.println("Input error, maybe the input " + (bypPinInput[i] & 0xFF) 
                        + " [0x" + Integer.toHexString((bypPinInput[i] & 0xFF)) + "]"
                        + " is TIMEOUT or the button is stuck.");
                    System.out.println("The input is going to close.\n");
                    nInputRet = -3;//end the pin input with no pinblock
                    break;
                }
                else if (0x08 == bypPinInput[i])
                {
                    System.out.println("You pressed the [CLEAR] or [BACKSPACE] button.");
                    //close the input and reopen the pin input
                    OpenKeyboardAndSound(PinpadCJNR.ESOUND.SOUND_CLOSE, PinpadCJNR.ENTRYMODE.ENTRY_MODE_CLOSE);
                    System.out.println("Open Keyboard And Sound SOUND_CLOSE  ENTRY_MODE_CLOSE Return is " + GetLastError());

                    StartPinInput(PinpadCJNR.ESOUND.SOUND_OPEN, 6, 4, true);
                    System.out.println("StartPinInput Return is " + GetLastError() + "\n");

                    byPinLen = 0;
                }
                else if (0x20 == bypPinInput[i])
                {
                    System.out.println("You pressed the [BLACK] button.");
                }
                else if (0x2E == bypPinInput[i])
                {
                    System.out.println("You pressed the [.] button.");
                }
                else if (0x7F == bypPinInput[i])
                {
                    System.out.println("You pressed the [00] button.");
                }
                else//count the PIN input, some pinpad's PIN return code can change
                {
                    System.out.println("You pressed the " + (char)bypPinInput[i] 
                        + " [0x" + Integer.toHexString((bypPinInput[i] & 0xFF)) + "]"
                        + " button.");
                    byPinLen++;
                }
            }
        }

        //close the input
        if (!OpenKeyboardAndSound(PinpadCJNR.ESOUND.SOUND_CLOSE, PinpadCJNR.ENTRYMODE.ENTRY_MODE_CLOSE)) {
            ShowErrorCode("OpenKeyboardAndSound");
            return;
        }

        System.out.println("Open Keyboard And Sound SOUND_CLOSE  ENTRY_MODE_CLOSE Return is " + GetLastError());

        if (-1 == nInputRet)//the pin input was ended by ENTER , get the pinblock
        {
            IntByReference byPinblockLen = new IntByReference();
            strCardNumber = "6216591242342141233";

            if (bExtend)
            {
                bSM4KeyIDOffset = false;
                bWorkKeyIDOffset = false;
                iMKID = PinpadCJNR.KEY_INVALID;
            }
            else 
            {
                //bSM4KeyIDOffset = true;
            }

            if (bWorkKeyIDOffset)
            {
                iWKID += PinpadCJNR.KEY_ID_OFFSET_PADK;
                iMKID = 0;
            }

            if (bSM4KeyIDOffset) // must add KEY_ID_OFFSET_SM4 when not use extend function and pin format not or FORMAT_SM4
            {
                iWKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
                //iMKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
            }

            System.out.println("SM4 GetPinBlock Key " + iWKID + " EnKey " + iMKID);

            //calculate pinblock , use key 2 , pin len is byPinLen , pinblock format is FORMAT_BANKSYS , the key's enkey is 1
            long nFormat = PinpadCJNR.EPINFORMAT.FORMAT_BANKSYS.intValue() 
                | PinpadCJNR.EPINFORMAT.FORMAT_SM4.intValue();
            if (!GetPinBlock(iWKID, (byte)byPinLen, nFormat, strCardNumber, bypPinBlockBin, byPinblockLen, 0xF, iMKID)) {
                ShowErrorCode("SM4 GetPinBlock");
                return;
            }
            
            //transform pinblock from HEX TO ASCII
            byPinLen = Soft_Bin2Hex(bypPinBlockHex, 32, bypPinBlockBin, byPinblockLen.getValue());
            System.out.println("SM4 GetPinBlock Return is " + GetLastError() + " PINBLOCK is "+ new String(bypPinBlockHex, 0, byPinLen)+ "\n");
        }
    }

     /*****************************************************************************
     * Function Description: SM4, calculate MAC by pinpad
     * Parameter: 
     * iWKID: work  key id(0-0x04)
     * iMKID:  master key id(enkey)
     * bExtend:   whether or not enable extend function
     * bWorkKeyIDOffset:  whether or not work key use offset which could not need master key when use this key
     * bSM4KeyIDOffset:  whether or not work key use SM4 offset
     * Return Value: 
     *****************************************************************************/
    public static void nSM4MAC(int iWKID, int iMKID, boolean bExtend, boolean bWorkKeyIDOffset, boolean bSM4KeyIDOffset)
    {
        byte[] bypDataBin = new byte[1000];  //data hex string , for CalcMAC
        byte[] bypMacBin  = new byte[16 + 1]; //MAC hex string , form CalcMAC
        byte[] cpMacAsc = new byte[32 + 1]; //MAC Hex string , for show
        byte[] bypIVHex = new byte[16 + 1]; // IV data hex

        if (bExtend)
        {
            bWorkKeyIDOffset = false;
            bSM4KeyIDOffset = false;
            iMKID = PinpadCJNR.KEY_INVALID;
        }

        if (bWorkKeyIDOffset)
        {
            iWKID += PinpadCJNR.KEY_ID_OFFSET_PADK;
            iMKID = 0;
        }

        if (bSM4KeyIDOffset)
        {
            iWKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
            //iMKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
        }

        String strDataHex = "303230302031393632313635393132343233343231343132333320333030303030203036303231373334323920303030363838203230303030303037";
        //transform data from ASCII TO HEX
        int iDataLen = Soft_Hex2Bin(bypDataBin, 1000, strDataHex, strDataHex.length());
        //IntByReference byDataLen = new IntByReference(iDataLen);//length of data for calculate MAC
        LongLongByReference byDataLen = new LongLongByReference(iDataLen);

        // IV string for CBC
        String strIV = "00000000000000000000000000000000";
        iDataLen = Soft_Hex2Bin(bypIVHex, 16, strIV, strIV.length());

        //calculate ndataLen length cpDataHex use SM4MAC_BANKSYS, with key 3 and no IV
        if (!CalcMAC(iWKID, PinpadCJNR.EMAC.SM4MAC_BANKSYS, bypDataBin, byDataLen, bypMacBin, null, iMKID)) {
            ShowErrorCode("SM4 CalcMAC");
            return;
        }
        //transform MAC from HEX TO ASCII
        iDataLen = Soft_Bin2Hex(cpMacAsc, 32, bypMacBin, byDataLen.getValue());
        System.out.println("SM4 MAC Return is " + GetLastError() + " MAC is " + new String(cpMacAsc, 0, iDataLen) + "\n");
    }

    /*****************************************************************************
     * Function Description: SM4, encrypt and decrypt by pinpad
     * Parameter: 
     * iWKID: work  key id(0-0x04)
     * iMKID:  master key id(enkey)
     * bExtend:   whether or not enable extend function
     * bWorkKeyIDOffset:  whether or not work key use offset which could not need master key when use this key
     * bSM4KeyIDOffset:  whether or not work key use SM4 offset
     * Return Value: 
     *****************************************************************************/
    public static void nSM4Crypt(int iWKID, int iMKID, boolean bExtend, boolean bWorkKeyIDOffset, boolean bSM4KeyIDOffset)
    {
        byte[] bypDataBin   = new byte[16 + 1];//data hex string, for crypt
        byte[] bypResultBin = new byte[16 + 1]; //crypt result hex string , from crypt , the len should increase with the data len
        byte[] bypResultHex = new byte[32 + 1];//crypt result Hex string , for show , the len should increase with the data len
        String strDataHex = "";

        byte[] bypIVHex = new byte[16 + 1]; //IV Hex data
        // IV string for CBC
        String strIV = "00000000000000000000000000000000";
        int iDataLen = Soft_Hex2Bin(bypIVHex, 16, strIV, strIV.length());

        if (bExtend)
        {
            bWorkKeyIDOffset = false;
            bSM4KeyIDOffset = false;
            iMKID = PinpadCJNR.KEY_INVALID;
        }
    
        if (bWorkKeyIDOffset)
        {
            iWKID += PinpadCJNR.KEY_ID_OFFSET_PADK;
            iMKID = 0;
        }

        if (bSM4KeyIDOffset)
        {
            iWKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
            //iMKID += PinpadCJNR.KEY_ID_OFFSET_SM4;
        }

        //encypt the data
        strDataHex = "EF34A0A6507D8464E34A4EEDA6CD7740";
        System.out.println("SM4 ENCRYPT " + strDataHex);

        iDataLen = Soft_Hex2Bin(bypDataBin, 16, strDataHex, strDataHex.length());

        //IntByReference byDataLen = new IntByReference(iDataLen);
        LongLongByReference byDataLen = new LongLongByReference(iDataLen);

        //Encrypt cpDataHex with key 0 and no IV by CRYPT_SM4ECB
        if (!Crypt(iWKID, PinpadCJNR.ECRYPT.CRYPT_SM4ECB, bypDataBin, byDataLen, bypResultBin, true, null, iMKID)) {
            ShowErrorCode("SM4 Crypt");
            return;
        }
        iDataLen = Soft_Bin2Hex(bypResultHex, 32, bypResultBin, byDataLen.getValue());
        System.out.println("Return is " + GetLastError() + " ciphertext is "+ new String(bypResultHex, 0, iDataLen) + "\n");
        
        //decrypt the data
        System.out.println("SM4 DECRYPT " + new String(bypResultHex, 0, iDataLen));

        System.arraycopy(bypResultBin, 0, bypDataBin, 0, bypResultBin.length);

        //iDataLen = byDataLen.getValue();
        long nDataLen = byDataLen.getValue();//(bypDataBin.length - 1);

        byDataLen = null;
        //byDataLen = new IntByReference(iDataLen);
        byDataLen = new LongLongByReference(nDataLen);

        //decrypt cpDataHex with key 0 and no IV by CRYPT_SM4ECB
        if (!Crypt(iWKID, PinpadCJNR.ECRYPT.CRYPT_SM4ECB, bypDataBin, byDataLen, bypResultBin, false, null, iMKID)) {
            ShowErrorCode("SM4 Crypt");
            return;
        }
        iDataLen = Soft_Bin2Hex(bypResultHex, 32, bypResultBin, byDataLen.getValue());
        System.out.println("Return is " + GetLastError() + " cleartext is " + new String(bypResultHex, 0, iDataLen) + "\n");
    }

    /**
     * 
     */
    public static void main(String[] args) {
        boolean bTest3DES = true; // whether or not bob.growingmdal.test 3DES key function
	    boolean bTestSM4 = true; // whether or not bob.growingmdal.test SM4 key function

        // should change as you env for linux, should modify when in windows

        // linux system
        //String logDir = "/home/oyq/work/zt/source/epp/epp_lxl/gnu/x86_64/log"; // log save dir, please change it in you env
        //String logExtendDb = "/home/oyq/work/zt/source/epp/epp_lxl/gnu/x86_64/EnlargeKey_c.db"; // must make sure you app have right to access this db file

        // windows system
        String logDir = "C:\\SZZT\\Log\\"; // log save dir, please change it in you env
        String logExtendDb = "C:\\SZZT\\EnlargeKey.dat"; // must make sure you app have right to access this enlarge file

        boolean bExtend; // library extend function, will use db or file to save key map info, for key have multiple usage and unified interface parameters 
        boolean bWorkKeyIDOffset = false; // whether or not work key use offset which could not need master key when use this key, some device support this
        boolean bSM4KeyIDOffset = false; // whether or not work key use SM4 offset which stand for SM4 key usage
        
        int iMKID = 1; // master key id
        int iWKPinID = 2; // work key for pin crypt
        int iWKMacID = 3; // work key for mac crypt
        int iWKDataID = 0; // work key for data crypt

        try {
            PinadCxxJNRTest.load(); // load library
            PinadCxxJNRTest.init();// init 

            bExtend = (eExtend != PinpadCJNR.EPIN_EXTEND.eEX_NONE);

            System.out.println("bExtend: " + bExtend);
            System.out.println("bWorkKeyIDOffset: " + bWorkKeyIDOffset);
            System.out.println("bSM4KeyIDOffset: " + bSM4KeyIDOffset);

            // open device
            if (!PinadCxxJNRTest.Open()) {
                ShowErrorCode("Open");
                return;
            }

            if (bExtend) {
                SetCaps(PinpadCJNR.ECAPS.eCAP_MAPPINGPATH, 0, logExtendDb);
            }

            LogConfig(1, logDir); // enable log
            //LogConfig(0x200, logDir); // disable log

            //Get Hardware Version
            System.out.println("Hardware info : " + PinadCxxJNRTest.GetHardwareVersion());

            //**CAUTION : Init() will reset the pinpad and delete keys
            //**if you want to reset the pinpad and keep the key , call the Reset()

            if (!Init()) {
                ShowErrorCode("Init");
            } else {
                System.out.println("Init ...");
            }
            
            //bob.growingmdal.test text input by pinpad
            nTextInput();

            if (bTest3DES) {
                //load key to pinpad
                nLoadKey(iMKID, iWKDataID, iWKPinID, iWKMacID, bExtend, bWorkKeyIDOffset);

                //input pin and get pinblock from pinpad
                nPinBlock(iWKPinID, iMKID, bExtend, bWorkKeyIDOffset);

                //calculate MAC by pinpad
                nMAC(iWKMacID, iMKID, bExtend, bWorkKeyIDOffset);

                //encrypt and decrypt by pinpad
                nCrypt(iWKDataID, iMKID, bExtend, bSM4KeyIDOffset);
            }
           
            if (bTestSM4) {
                iMKID = 2;
                // SM4 load key to pinpad
                nSM4LoadKey(iMKID, iWKDataID, iWKPinID, iWKMacID, bExtend, bWorkKeyIDOffset, bSM4KeyIDOffset);

                // SM4 input pin and get pinblock from pinpad
                nSM4PinBlock(iWKPinID, iMKID, bExtend, bWorkKeyIDOffset, bSM4KeyIDOffset);

                //SM4 calculate MAC by pinpad
                nSM4MAC(iWKMacID, iMKID, bExtend, bWorkKeyIDOffset, bSM4KeyIDOffset);

                //SM4 encrypt and decrypt by pinpad
                nSM4Crypt(iWKDataID, iMKID, bExtend, bWorkKeyIDOffset, bSM4KeyIDOffset);
            }
           
            //close pinpad
            //Close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //close pinpad
            PinadCxxJNRTest.Close();
        }
    }
}
