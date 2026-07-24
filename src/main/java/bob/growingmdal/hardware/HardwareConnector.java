package bob.growingmdal.hardware;

/**
 * 硬件协议连接器的统一抽象。
 * 每种底层协议（JNR-FFI、JNA、SNMP、WebSocket Client、串口、HTTP、MQTT）都应提供其实现。
 *
 * @param <T> 底层客户端类型，仅在必要时暴露给调用方
 */
public interface HardwareConnector<T> {

    ProtocolType protocol();

    boolean connect();

    void disconnect();

    boolean isConnected();

    T getUnderlying();
}
