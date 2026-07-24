package bob.growingmdal.service;

import bob.growingmdal.camera.CameraCommandExecutor;
import bob.growingmdal.camera.CameraLifecycleManager;
import bob.growingmdal.camera.CameraMessageRouter;
import bob.growingmdal.hardware.LifecycleManaged;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Slf4j
@Service
@ClientEndpoint
public class NantianCameraService implements LifecycleManaged {

    private final CameraLifecycleManager lifecycleManager;
    private final CameraMessageRouter messageRouter;
    private final CameraCommandExecutor commandExecutor;

    @Autowired
    public NantianCameraService(CameraLifecycleManager lifecycleManager,
                                CameraMessageRouter messageRouter,
                                CameraCommandExecutor commandExecutor) {
        this.lifecycleManager = lifecycleManager;
        this.messageRouter = messageRouter;
        this.commandExecutor = commandExecutor;
    }

    @PostConstruct
    public void init() {
        lifecycleManager.setEndpoint(this);
        lifecycleManager.setOnReconnect(() -> commandExecutor.startNtCamera());
        initialize();
    }

    @Override
    public void initialize() {
        lifecycleManager.initialize();
        if (lifecycleManager.isEnabled()) {
            commandExecutor.startNtCamera();
        }
    }

    @Override
    public Health health() {
        if (lifecycleManager.isConnected()) {
            return Health.up().withDetail("device", "Nantian Camera").build();
        }
        return Health.down().withDetail("device", "Nantian Camera").withDetail("reason", "not connected").build();
    }

    @Override
    @PreDestroy
    public void shutdown() {
        commandExecutor.shutdown();
        lifecycleManager.shutdown();
    }

    @OnOpen
    public void onOpen(Session session) {
        lifecycleManager.onOpen(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        messageRouter.onTextMessage(message);
    }

    @OnMessage
    public void onMessage(ByteBuffer bytes, Session session) {
        messageRouter.onBinaryMessage(bytes);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        lifecycleManager.onClose();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        lifecycleManager.onError(throwable);
    }
}
