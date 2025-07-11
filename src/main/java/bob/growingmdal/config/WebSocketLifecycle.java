package bob.growingmdal.config;

import bob.growingmdal.handler.HardwareWebSocketHandler;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class WebSocketLifecycle implements SmartLifecycle {
    private final HardwareWebSocketHandler handler;
    private boolean running = false;

    public WebSocketLifecycle(HardwareWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void stop(Runnable callback) {
        handler.closeAllSessions(); // 关闭所有WebSocket连接
        callback.run();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}