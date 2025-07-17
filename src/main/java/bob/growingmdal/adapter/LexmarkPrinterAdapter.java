package bob.growingmdal.adapter;


import bob.growingmdal.util.snmp.SnmpUtil;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LexmarkPrinterAdapter {

    SnmpUtil snmpUtil;

    /**
     * Lexmark打印机状态位含义
     */
    private static final Map<Integer, String> STATUS_BIT_MEANINGS = new HashMap<>();

    static {
        STATUS_BIT_MEANINGS.put(0, "少纸");
        STATUS_BIT_MEANINGS.put(1, "无纸");
        STATUS_BIT_MEANINGS.put(2, "少粉");
        STATUS_BIT_MEANINGS.put(3, "无粉");
        STATUS_BIT_MEANINGS.put(4, "盖门开启");
        STATUS_BIT_MEANINGS.put(5, "卡纸");
        STATUS_BIT_MEANINGS.put(6, "设备离线");
        STATUS_BIT_MEANINGS.put(7, "需要维护");
        STATUS_BIT_MEANINGS.put(8, "进纸盒缺失");
        STATUS_BIT_MEANINGS.put(9, "出纸盒缺失");
        STATUS_BIT_MEANINGS.put(10, "耗材缺失");
        STATUS_BIT_MEANINGS.put(11, "出纸盒接近最高容量");
        STATUS_BIT_MEANINGS.put(12, "出纸盒已满");
        STATUS_BIT_MEANINGS.put(13, "进纸盒已空");
        STATUS_BIT_MEANINGS.put(14, "定期保养已超时");
    }

    public LexmarkPrinterAdapter(String ip, String community) {
        this(ip, community, SnmpConstants.version2c, 1500, 2);
    }

    public LexmarkPrinterAdapter(String ip, String community, int version, int timeout, int retries) {
        try {
            snmpUtil = new SnmpUtil(ip, community, version, timeout, retries);
        } catch (Exception e) {
            log.error("LexmarkPrinterAdapter init error: {}", e.getMessage());
        }
    }

    /**
     * 获取打印状态
     * @return 异常信息
     * @throws IOException 报错信息
     */
    public String getPrinterErrStatus() throws IOException {
        Variable singleResult = snmpUtil.snmpGetSingle(".1.3.6.1.2.1.25.3.5.1.2.1");
        return parsePrinterErrStatus(singleResult.toString());
    }

    public String getPrinterStatus() throws IOException {
        Variable singleResult = snmpUtil.snmpGetSingle(".1.3.6.1.2.1.25.3.5.1.1.1");
        return parsePrinterStatus(singleResult.toString());
    }

    private String parsePrinterStatus(String printerStatus) {
        return switch (printerStatus) {
            case "0" -> "其他";
            case "2" -> "状态未知";
            case "3" -> "空闲";
            case "4" -> "预热(处理打印队列或后台程序)";
            default -> "未知";
        };
    }

    private String parsePrinterErrStatus(String printerStatus) {
        // 验证输入格式
        if (!printerStatus.matches("[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}")) {
            return "错误：无效的状态格式，应为XX:XX的16进制形式";
        }

        try {
            // 去除冒号并转换为二进制
            String cleanHex = printerStatus.replace(":", "");
            String binaryString = hexToBinary(cleanHex);

            // 解析状态位
            StringBuilder result = new StringBuilder();

            boolean hasError = false;
            for (int i = 0; i < binaryString.length(); i++) {
                if (binaryString.charAt(i) == '1') {
                    String statusDesc = STATUS_BIT_MEANINGS.get(i);
                    if (statusDesc != null) {
                        result.append(String.format("%s ", statusDesc));
                        hasError = true;
                    }
                }
            }

            if (!hasError) {
                result.append("设备状态正常，无异常");
            }

            return result.toString();

        } catch (Exception e) {
            return "解析状态时发生错误: " + e.getMessage();
        }
    }

    /**
     * 16进制字符串转二进制字符串
     */
    private static String hexToBinary(String hexString) {
        // 每个16进制字符对应4位二进制
        StringBuilder binaryString = new StringBuilder();
        for (char c : hexString.toCharArray()) {
            String binary = String.format("%4s",
                            Integer.toBinaryString(Character.digit(c, 16)))
                    .replace(' ', '0');
            binaryString.append(binary);
        }
        return binaryString.toString();
    }

    /**
     * 格式化二进制字符串，每4位加空格
     */
    private static String formatBinary(String binary) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < binary.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(binary.charAt(i));
        }
        return formatted.toString();
    }


}
