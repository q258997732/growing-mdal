package bob.growingmdal.camera;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CameraLifecycleManager {

    @Value("${nantian.camera.url}")
    private String cameraUrl;
    @Value("${nantian.camera.enable}")
    private boolean enable;
    @Value("${nantian.camera.auto.reconnect}")
    private boolean autoReconnect;
    @Value("${nantian.camera.auto.reconnect.interval}")
    private int autoReconnectInterval;

    @Getter
    private Session session;
    private final ScheduledExecutorService autoConnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private Object endpoint;
    private Runnable onReconnect;

    public void setEndpoint(Object endpoint) {
        this.endpoint = endpoint;
    }

    public void setOnReconnect(Runnable onReconnect) {
        this.onReconnect = onReconnect;
    }

    public void initialize() {
        if (!enable) {
            log.info("Nantian camera service is disabled.");
            return;
        }
        connect();
        if (autoReconnect) {
            autoConnectExecutor.scheduleAtFixedRate(autoConnectRunnable, 0, autoReconnectInterval, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isEnabled() {
        return enable;
    }

    public boolean connect() {
        if (session != null && session.isOpen()) {
            return false;
        }
        if (endpoint == null) {
            log.error("Camera endpoint not set");
            return false;
        }
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxBinaryMessageBufferSize(100 * 1024 * 1024);
            container.setDefaultMaxTextMessageBufferSize(50 * 1024 * 1024);
            container.connectToServer(endpoint, new URI(cameraUrl));
            log.info("Connected to server: {}", cameraUrl);
            return true;
        } catch (Exception e) {
            log.error("Failed to connect nantian WebSocket : {}", e.getMessage());
            return false;
        }
    }

    public void onOpen(Session session) {
        this.session = session;
        session.setMaxIdleTimeout(0);
    }

    public void onClose() {
        this.session = null;
        log.info("Nantian camera api Session closed");
    }

    public void onError(Throwable throwable) {
        log.error("WebSocket error: ", throwable);
    }

    public void shutdown() {
        autoConnectExecutor.shutdownNow();
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("Failed to close camera session", e);
            }
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    private final Runnable autoConnectRunnable = () -> {
        if (isConnected()) {
            log.debug("auto reconnect : Nantian camera is already connected.");
            return;
        }
        log.info("Trying to reconnect to Nantian camera...");
        boolean connected = connect();
        if (connected && onReconnect != null) {
            onReconnect.run();
        }
    };
}
