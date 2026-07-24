package bob.growingmdal.camera;

import bob.growingmdal.hardware.HardwareConnector;
import bob.growingmdal.hardware.ProtocolType;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 南天摄像头 WebSocket 客户端的 HardwareConnector 适配。
 * 将 {@link CameraLifecycleManager} 包装为统一的连接器抽象。
 */
@Component
public class NantianCameraConnector implements HardwareConnector<Session> {

    private final CameraLifecycleManager lifecycleManager;

    @Autowired
    public NantianCameraConnector(CameraLifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    public ProtocolType protocol() {
        return ProtocolType.WEBSOCKET_CLIENT;
    }

    @Override
    public boolean connect() {
        return lifecycleManager.connect();
    }

    @Override
    public void disconnect() {
        lifecycleManager.shutdown();
    }

    @Override
    public boolean isConnected() {
        return lifecycleManager.isConnected();
    }

    @Override
    public Session getUnderlying() {
        return lifecycleManager.getSession();
    }
}
