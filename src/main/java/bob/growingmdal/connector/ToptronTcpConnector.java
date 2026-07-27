package bob.growingmdal.connector;

import bob.growingmdal.config.AdapterProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Toptron 中控设备 TCP 连接器。
 */
@Slf4j
@Component
public class ToptronTcpConnector {

    private final AdapterProperties adapterProperties;

    public ToptronTcpConnector(AdapterProperties adapterProperties) {
        this.adapterProperties = adapterProperties;
    }

    /**
     * 发送电源控制报文。
     *
     * @param gatePosition  gate 位置（字符串）
     * @param turnon        true=开，false=关
     * @return 是否发送成功
     */
    public boolean sendPowerControl(String gatePosition, boolean turnon) {
        if (gatePosition == null || gatePosition.isBlank()) {
            log.error("Toptron gatePosition is required");
            return false;
        }
        String status = turnon ? "1" : "0";
        String hexString = "fa000002000305fdff000" + gatePosition + "0" + status + "55";
        return sendHexMessage(hexString);
    }

    /**
     * 发送原始十六进制报文。
     *
     * @param hexString 十六进制字符串
     * @return 是否发送成功
     */
    public boolean sendRawMessage(String hexString) {
        if (hexString == null || hexString.isBlank()) {
            log.error("Toptron controlMsg is required");
            return false;
        }
        return sendHexMessage(hexString);
    }

    /**
     * 测试 TCP 连通性，用于健康检查。
     *
     * @return true=可达
     */
    public boolean isReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(adapterProperties.getToptronHost(),
                    adapterProperties.getToptronPort()), 3000);
            return socket.isConnected();
        } catch (IOException e) {
            log.warn("Toptron host not reachable: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendHexMessage(String hexString) {
        byte[] hexBytes;
        try {
            hexBytes = hexStringToByteArray(hexString);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Toptron hex message: {}", e.getMessage());
            return false;
        }

        try (Socket socket = new Socket();
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
            socket.connect(new InetSocketAddress(adapterProperties.getToptronHost(),
                    adapterProperties.getToptronPort()), adapterProperties.getToptronConnectTimeout());
            dataOutputStream.write(hexBytes);
            dataOutputStream.flush();
            log.debug("Toptron message sent: {}", hexString);
            return true;
        } catch (IOException e) {
            log.error("Failed to send Toptron message: {}", e.getMessage());
            return false;
        }
    }

    private byte[] hexStringToByteArray(String s) {
        if (s.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be even");
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(s.charAt(i), 16);
            int low = Character.digit(s.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex character at position " + i);
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    public String getHost() {
        return adapterProperties.getToptronHost();
    }

    public int getPort() {
        return adapterProperties.getToptronPort();
    }
}
