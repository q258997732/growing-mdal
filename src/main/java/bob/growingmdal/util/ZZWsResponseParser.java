package bob.growingmdal.util;

import java.util.HashMap;
import java.util.Map;

public class ZZWsResponseParser {

    // 状态码描述映射
    private static final Map<Integer, String> STATUS_DESCRIPTION = new HashMap<>();
    private static final Map<Integer, String> CAMERA_TYPE_DESCRIPTION = new HashMap<>();
    private static final Map<Integer, String> IMAGE_FORMAT_DESCRIPTION = new HashMap<>();
    private static final Map<Integer, String> DATE_POSITION_DESCRIPTION = new HashMap<>();
    private static final Map<Integer, String> FACE_DETECT_RESULT_DESCRIPTION = new HashMap<>();

    static {
        // 初始化状态码描述
        STATUS_DESCRIPTION.put(-1, "不存在");
        STATUS_DESCRIPTION.put(0, "未打开/失败");
        STATUS_DESCRIPTION.put(1, "已打开/成功");

        // 摄像头类型描述
        CAMERA_TYPE_DESCRIPTION.put(1, "文件摄像头");
        CAMERA_TYPE_DESCRIPTION.put(2, "人脸摄像头");
        CAMERA_TYPE_DESCRIPTION.put(3, "环境摄像头");
        CAMERA_TYPE_DESCRIPTION.put(4, "人脸红外摄像头");

        // 图像格式描述
        IMAGE_FORMAT_DESCRIPTION.put(1, "BMP");
        IMAGE_FORMAT_DESCRIPTION.put(2, "JPG");
        IMAGE_FORMAT_DESCRIPTION.put(3, "PNG");
        IMAGE_FORMAT_DESCRIPTION.put(4, "TIFF");
        IMAGE_FORMAT_DESCRIPTION.put(5, "GIF");

        // 日期位置描述
        DATE_POSITION_DESCRIPTION.put(1, "左上");
        DATE_POSITION_DESCRIPTION.put(2, "右上");
        DATE_POSITION_DESCRIPTION.put(3, "左下");
        DATE_POSITION_DESCRIPTION.put(4, "右下");

        // 人脸检测结果描述
        FACE_DETECT_RESULT_DESCRIPTION.put(0, "成功");
        FACE_DETECT_RESULT_DESCRIPTION.put(-1, "匹配特征失败");
        FACE_DETECT_RESULT_DESCRIPTION.put(-2, "未获取到人脸");
        FACE_DETECT_RESULT_DESCRIPTION.put(-3, "不是活体");
    }

    /**
     * 解析WebService返回结果并生成可读描述
     *
     * @param response 原始响应字符串（格式：命令名#参数1#参数2...）
     * @return 可读的描述信息
     */
    public static String parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "无效响应";
        }

        String[] parts = response.split("#");
        if (parts.length < 1) {
            return "无效响应格式";
        }

        String command = parts[0];
        String params = parts.length > 1 ? parts[1] : "";
        String data = parts.length > 2 ? parts[2] : "";

        switch (command) {
            // 设备信息命令
            case "GetDevCount":
                return parseGetDevCount(params);
            case "GetDeviceStatus":
            case "GetDeviceStatusEx":
                return parseDeviceStatus(command, params);
            case "GetDeviceName":
            case "GetChildDeviceName":
            case "GetHideDeviceName":
                return parseGetDeviceName(command, params);
            case "GetVidPid":
            case "GetChildVidPid":
            case "GetHideVidPid":
                return parseGetVidPid(command, params);

            // 设备操作命令
            case "OpenDevice":
            case "OpenChildDevice":
            case "OpenHideDevice":
            case "OpenDeviceEx":
            case "OpenChildDeviceEx":
            case "OpenHideDeviceEx":
            case "CloseDevice":
            case "CloseChildDevice":
            case "CloseHideDevice":
            case "OpenVideo":
            case "OpenChildVideo":
            case "OpenHideVideo":
            case "CloseVideo":
            case "CloseChildVideo":
            case "CloseHideVideo":
            case "ShowPin":
            case "ShowChildPin":
            case "ShowHidePin":
            case "ShowFilter":
            case "ShowChildFilter":
            case "ShowHideFilter":
            case "RotateLeft":
            case "RotateRight":
            case "ZoomIn":
            case "ZoomOut":
            case "Deskew":
            case "MultiDeskew":
            case "UnDeskew":
            case "MoveDetect":
            case "UnMoveDetect":
            case "StopRecord":
            case "DisableDate":
            case "DisableWatermark":
            case "OpenIdCard":
            case "OpenIdCardEx":
            case "CloseIdCard":
            case "ReadIdCard":
            case "SetJpegQuality":
            case "InitFaceMgr":
            case "DeinitFaceMgr":
            case "FaceDetect":
            case "FaceDetectEx":
            case "UnFaceDetect":
            case "GetFaceTemp1":
            case "GetFaceFeature":
            case "StopGetFace":
            case "CompareFace":
            case "MakeImageListFile":
                return parseOperationResult(command, params);

            // 分辨率相关命令
            case "SetResolution":
            case "SetChildResolution":
            case "SetHideResolution":
                return parseSetResolution(command, params);
            case "GetResolution":
            case "GetChildResolution":
            case "GetHideResolution":
                return parseGetResolution(command, params);

            // 拍照相关命令
            case "Capture":
                return parseCapture(params);
            case "CaptureToFile":
            case "MultiCaptureToFile":
                return parseCaptureToFile(command, params);
            case "MultiCapture":
                return parseMultiCapture(params);

            // 其他功能命令
            case "GetBarcode":
                return parseGetBarcode(params);
            case "GetIdCardResult":
                return parseGetIdCardResult(params);
            case "GetIdCardImage":
                return parseGetIdCardImage(params);
            case "GetIdCardFingerFeature":
                return parseGetIdCardFingerFeature(params);
            case "StartRecord":
                return parseStartRecord(params);
            case "EnableDate":
                return parseEnableDate(params);
            case "EnableWatermark":
                return parseEnableWatermark(params);
            case "GetBase64":
                return parseGetBase64(params);

            // 人脸识别相关
            case "FaceDetectEvent":
                return parseFaceDetectEvent(params);
            case "FaceDetectExEvent":
                return parseFaceDetectExEvent(params);
            case "FaceResultEvent":
                return parseFaceResultEvent(params, data);
            case "GetFaceTemp1FromBase64":
                return parseGetFaceTemp1FromBase64(params);
            case "CompareFaceEx":
                return parseCompareFaceEx(params);

            case "GetFaceTemplFromBase64":
                return parseGetFaceTemplFromBase64(params);
            // 默认处理
            default:
                return parseGenericResponse(command, params);
        }
    }

    public static String getCaptureBase64(String response) {
        if (response.split("#").length > 1) {
            return response.split("#")[1];
        }
        return null;
    }

    // ==== 解析方法实现 ====

    private static String parseGetDevCount(String params) {
        return "摄像头数量: " + params;
    }

    private static String parseDeviceStatus(String command, String params) {
        try {
            int status = Integer.parseInt(params);
            String statusDesc = STATUS_DESCRIPTION.getOrDefault(status, "未知状态(" + status + ")");
            return command + "状态: " + statusDesc;
        } catch (NumberFormatException e) {
            return command + "状态解析失败";
        }
    }

    private static String parseGetDeviceName(String command, String params) {
        String deviceType = command.equals("GetDeviceName") ? "主摄像头" :
                command.equals("GetChildDeviceName") ? "副摄像头" : "隐藏摄像头";
        return deviceType + "名称: " + params;
    }

    private static String parseGetVidPid(String command, String params) {
        String deviceType = command.equals("GetVidPid") ? "主摄像头" :
                command.equals("GetChildVidPid") ? "副摄像头" : "隐藏摄像头";
        return deviceType + " VID/PID: " + params;
    }

    private static String parseOperationResult(String command, String params) {
        if (params.isEmpty()) {
            return command + "操作结果: 无返回参数";
        }

        try {
            int result = Integer.parseInt(params);
            String resultDesc = (result != 0) ? "成功" : "失败";
            return command + "操作结果: " + resultDesc + " (返回码: " + result + ")";
        } catch (NumberFormatException e) {
            return command + "操作结果: 非数字返回值 - " + params;
        }
    }

    private static String parseSetResolution(String command, String params) {
        String[] parts = params.split("#");
        if (parts.length < 1) {
            return command + "设置结果: 无参数";
        }

        try {
            int result = Integer.parseInt(parts[0]);
            String resultDesc = (result != 0) ? "成功" : "失败";
            return command + "设置结果: " + resultDesc;
        } catch (NumberFormatException e) {
            return command + "设置结果: 无效返回码 - " + parts[0];
        }
    }

    private static String parseGetResolution(String command, String params) {
        String[] parts = params.split("#");
        if (parts.length < 3) {
            return command + "获取结果: 参数不足";
        }

        try {
            int format = Integer.parseInt(parts[0]);
            int width = Integer.parseInt(parts[1]);
            int height = Integer.parseInt(parts[2]);

            String formatDesc = IMAGE_FORMAT_DESCRIPTION.getOrDefault(format, "未知格式(" + format + ")");
            return command + "结果: 格式=" + formatDesc +
                    ", 分辨率=" + width + "x" + height;
        } catch (NumberFormatException e) {
            return command + "获取结果: 参数解析失败";
        }
    }

    private static String parseCapture(String params) {
        if (params.isEmpty()) {
            return "拍照失败: 无返回数据";
        }
        return "拍照成功: 获取到Base64图像数据 (长度=" + params.length() + ")";
    }

    private static String parseMultiCapture(String params) {
        if (params.isEmpty()) {
            return "多张拍照失败: 无返回数据";
        }

        String[] images = params.split("#");
        return "多张拍照成功: 获取到" + images.length + "张图像";
    }

    private static String parseCaptureToFile(String command, String params) {
        try {
            int result = Integer.parseInt(params);
            String resultDesc = (result != 0) ? "成功" : "失败";
            return command + "操作结果: " + resultDesc;
        } catch (NumberFormatException e) {
            return command + "操作结果: 无效返回码 - " + params;
        }
    }

    private static String parseGetBarcode(String params) {
        if (params.isEmpty()) {
            return "未检测到条码";
        }

        String[] barcodes = params.split("#");
        if (barcodes.length == 1) {
            return "检测到条码: " + barcodes[0];
        }
        return "检测到" + barcodes.length + "个条码: " + String.join(", ", barcodes);
    }

    private static String parseGetIdCardResult(String params) {
        String[] fields = params.split("#");
        if (fields.length < 15) {
            return "身份证信息不完整";
        }

        StringBuilder sb = new StringBuilder("身份证信息:\n");
        // 国内身份证信息 (1-15)
        sb.append("姓名: ").append(fields[0]).append("\n");
        sb.append("性别: ").append(fields[1]).append("\n");
        sb.append("民族: ").append(fields[2]).append("\n");
        sb.append("出生日期: ").append(fields[3]).append("-").append(fields[4]).append("-").append(fields[5]).append("\n");
        sb.append("住址: ").append(fields[6]).append("\n");
        sb.append("身份证号: ").append(fields[7]).append("\n");
        sb.append("发证机关: ").append(fields[8]).append("\n");
        sb.append("有效期起: ").append(fields[9]).append("-").append(fields[10]).append("-").append(fields[11]).append("\n");
        sb.append("有效期止: ").append(fields[12]).append("-").append(fields[13]).append("-").append(fields[14]);

        // 外国人永久居留证信息 (16-31)
        if (fields.length > 15 && !fields[15].isEmpty()) {
            sb.append("\n\n外国人永久居留证信息:\n");
            sb.append("英文姓名: ").append(fields[15]).append("\n");
            sb.append("性别: ").append(fields[16]).append("\n");
            sb.append("永久居留证号码: ").append(fields[17]).append("\n");
            sb.append("国籍: ").append(fields[18]).append("\n");
            sb.append("中文姓名: ").append(fields[19]).append("\n");
            sb.append("有效期起: ").append(fields[20]).append("-").append(fields[21]).append("-").append(fields[22]).append("\n");
            sb.append("有效期止: ").append(fields[23]).append("-").append(fields[24]).append("-").append(fields[25]).append("\n");
            sb.append("出生日期: ").append(fields[26]).append("-").append(fields[27]).append("-").append(fields[28]).append("\n");
            sb.append("证件版本号: ").append(fields[29]).append("\n");
            sb.append("申请受理机关: ").append(fields[30]);
        }

        // 港澳台居住证信息 (61-76)
        if (fields.length > 60 && !fields[60].isEmpty()) {
            sb.append("\n\n港澳台居住证信息:\n");
            sb.append("姓名: ").append(fields[60]).append("\n");
            sb.append("性别: ").append(fields[61]).append("\n");
            sb.append("出生日期: ").append(fields[62]).append("-").append(fields[63]).append("-").append(fields[64]).append("\n");
            sb.append("住址: ").append(fields[65]).append("\n");
            sb.append("身份证号: ").append(fields[66]).append("\n");
            sb.append("发证机关: ").append(fields[67]).append("\n");
            sb.append("有效期起: ").append(fields[68]).append("-").append(fields[69]).append("-").append(fields[70]).append("\n");
            sb.append("有效期止: ").append(fields[71]).append("-").append(fields[72]).append("-").append(fields[73]).append("\n");
            sb.append("通行证号码: ").append(fields[74]).append("\n");
            sb.append("签发次数: ").append(fields[75]);
        }

        return sb.toString();
    }

    private static String parseGetIdCardImage(String params) {
        String[] images = params.split("#");
        if (images.length < 3) {
            return "身份证图像数据不完整";
        }

        return "身份证图像数据:\n" +
                "正面图像长度: " + images[0].length() + "\n" +
                "背面图像长度: " + images[1].length() + "\n" +
                "人脸图像长度: " + images[2].length();
    }

    private static String parseGetIdCardFingerFeature(String params) {
        String[] features = params.split("#");
        if (features.length < 2) {
            return "指纹特征数据不完整";
        }

        return "指纹特征值:\n" +
                "第一个特征值长度: " + features[0].length() + "\n" +
                "第二个特征值长度: " + features[1].length();
    }

    private static String parseStartRecord(String params) {
        try {
            int result = Integer.parseInt(params);
            String resultDesc = (result != 0) ? "成功" : "失败";
            return "启动录像结果: " + resultDesc;
        } catch (NumberFormatException e) {
            return "启动录像结果: 无效返回码 - " + params;
        }
    }

    private static String parseEnableDate(String params) {
        String[] parts = params.split("#");
        if (parts.length < 1) {
            return "启用日期设置失败: 无返回参数";
        }

        try {
            int result = Integer.parseInt(parts[0]);
            String resultDesc = (result != 0) ? "成功" : "失败";
            return "启用日期设置: " + resultDesc;
        } catch (NumberFormatException e) {
            return "启用日期设置: 无效返回码 - " + parts[0];
        }
    }

    private static String parseEnableWatermark(String params) {
        String[] parts = params.split("#");
        if (parts.length < 1) {
            return "启用水印设置失败: 无返回参数";
        }

        try {
            int result = Integer.parseInt(parts[0]);
            String resultDesc = (result != 0) ? "成功" : "失败";
            return "启用水印设置: " + resultDesc;
        } catch (NumberFormatException e) {
            return "启用水印设置: 无效返回码 - " + parts[0];
        }
    }

    private static String parseGetBase64(String params) {
        if (params.isEmpty()) {
            return "获取Base64数据失败: 无返回数据";
        }
        return "获取Base64数据成功: 数据长度=" + params.length();
    }

    private static String parseFaceDetectExEvent(String params) {
        String[] parts = params.split("#");
        if (parts.length < 1) {
            return "人脸检测事件: 参数不足";
        }

        int resultCode;
        try {
            resultCode = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return "人脸检测事件: 无效结果码";
        }

        String resultDesc;
        switch (resultCode) {
            case 0:
                resultDesc = "不是活体";
                break;
            case 1:
                resultDesc = "未检测到图像";
                break;
            case 2:
                resultDesc = "未检测到人脸";
                break;
            case 3:
                resultDesc = "检测成功";
                break;
            default:
                resultDesc = "未知状态(" + resultCode + ")";
        }

        if (resultCode == 3 && parts.length >= 2) {
            try {
                int score = Integer.parseInt(parts[1]);
                return "人脸检测结果: " + resultDesc + ", 相似度分数: " + score;
            } catch (NumberFormatException e) {
                return "人脸检测结果: " + resultDesc + ", 无效分数值";
            }
        }
        return "人脸检测结果: " + resultDesc;
    }

    private static String parseFaceDetectEvent(String params) {
        String[] parts = params.split("#");
        if (parts.length < 1) {
            return "参数不足";
        }
        int resultCode;
        try {
            resultCode = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return "无效结果码";
        }
        String resultText;
        switch (resultCode) {
            case 0:
                resultText = "成功";
                break;
            case -1:
                resultText = "匹配特征失败";
                break;
            case -2:
                resultText = "未获取到人脸";
                break;
            case -3:
                resultText = "不是活体";
                break;
            default:
                resultText = "未知错误";
        }
        return resultText;

    }

    private static String parseFaceResultEvent(String params, String data) {
        String[] parts = params.split("#");
        if (parts.length < 1) {
            return "识别失败";
        }
        int resultCode = -99;
        try {
            resultCode = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return "无效结果码";
        }
        return FACE_DETECT_RESULT_DESCRIPTION.getOrDefault(resultCode, "未知结果(" + resultCode + ")") + " ,特征码 ：" + data;
    }

    private static String parseGetFaceTemp1FromBase64(String params) {
        if (params.isEmpty()) {
            return "获取人脸特征值失败: 无返回数据";
        }
        return "获取人脸特征值成功: 特征值长度=" + params.length();
    }

    private static String parseCompareFaceEx(String params) {
        try {
            int score = Integer.parseInt(params);
            return "人脸对比分数: " + score + "/100 (分数越高越相似)";
        } catch (NumberFormatException e) {
            return "人脸对比结果: 无效分数值 - " + params;
        }
    }

    private static String parseGenericResponse(String command, String params) {
        if (params.isEmpty()) {
            return command + ": 无返回参数";
        }

        // 尝试解析为数字结果
        try {
            int result = Integer.parseInt(params);
            if (STATUS_DESCRIPTION.containsKey(result)) {
                return command + ": " + STATUS_DESCRIPTION.get(result);
            }
            return command + ": 返回码=" + result;
        } catch (NumberFormatException ignored) {
        }

        // 包含多个参数的情况
        if (params.contains("#")) {
            String[] subParams = params.split("#");
            return command + ": 返回" + subParams.length + "个参数";
        }

        return command + ": " + params;
    }

    private static String parseGetFaceTemplFromBase64(String params){
        String[] parts = params.split("#");
        if (parts.length < 1) {
            return "获取失败,无返回特征";
        }
        return "成功";
    }

    public static String getFaceEigenvalueData(String params){
        String[] parts = params. split("#");
        if (parts.length < 2) {
            return "获取失败";
        }
        return parts[1];
    }

    // 单元测试示例
    public static void main(String[] args) {
        System.out.println("=== 设备信息测试 ===");
        System.out.println(parseResponse("GetDevCount#3"));
        System.out.println(parseResponse("GetDeviceStatus#1"));
        System.out.println(parseResponse("GetDeviceName#Main_Camera"));

        System.out.println("\n=== 设备操作测试 ===");
        System.out.println(parseResponse("OpenDevice#1"));
        System.out.println(parseResponse("CloseVideo#0"));
        System.out.println(parseResponse("RotateLeft#1"));

        System.out.println("\n=== 拍照测试 ===");
        System.out.println(parseResponse("Capture#base64data..."));
        System.out.println(parseResponse("MultiCapture#data1#data2#data3"));

        System.out.println("\n=== 身份证测试 ===");
        System.out.println(parseResponse("GetIdCardResult#张三#男#汉#1990#03#15#北京市...#11010119900315...#公安局#2020#01#01#2030#01#01"));
        System.out.println(parseResponse("GetIdCardImage#front#back#face"));

        System.out.println("\n=== 人脸识别测试 ===");
        System.out.println(parseResponse("FaceDetectExEvent#3#85"));
        System.out.println(parseResponse("FaceResultEvent#0#feature_data"));
        System.out.println(parseResponse("CompareFaceEx#78"));

        System.out.println("\n=== 其他功能测试 ===");
        System.out.println(parseResponse("GetBarcode#978756#123456"));
        System.out.println(parseResponse("StartRecord#1"));
        System.out.println(parseResponse("GetBase64#long_base64_string"));
    }
}