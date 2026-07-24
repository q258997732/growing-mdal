package bob.growingmdal.config;

import bob.growingmdal.service.NantianCameraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GracefulShutdownConfig implements ApplicationListener<ContextClosedEvent> {

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30;

    private final TaskExecutor messageTaskExecutor;
    private final WebSocketSessionManager sessionManager;
    private final NantianCameraService nantianCameraService;

    public GracefulShutdownConfig(TaskExecutor messageTaskExecutor,
                                  WebSocketSessionManager sessionManager,
                                  NantianCameraService nantianCameraService) {
        this.messageTaskExecutor = messageTaskExecutor;
        this.sessionManager = sessionManager;
        this.nantianCameraService = nantianCameraService;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Starting graceful shutdown...");

        closeAllWebSocketSessions();
        shutdownTaskExecutor();
        shutdownCameraService();

        log.info("Graceful shutdown completed");
    }

    private void closeAllWebSocketSessions() {
        log.info("Closing all WebSocket sessions");
        sessionManager.closeAllSessions();
    }

    private void shutdownTaskExecutor() {
        if (messageTaskExecutor instanceof ThreadPoolTaskExecutor executor) {
            log.info("Shutting down message task executor");
            executor.shutdown();
            try {
                if (!executor.getThreadPoolExecutor().awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    log.warn("Message task executor did not terminate within {} seconds, forcing shutdown", SHUTDOWN_TIMEOUT_SECONDS);
                    executor.getThreadPoolExecutor().shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for task executor shutdown");
                executor.getThreadPoolExecutor().shutdownNow();
            }
        } else {
            log.warn("Message task executor is not a ThreadPoolTaskExecutor, cannot perform graceful shutdown");
        }
    }

    private void shutdownCameraService() {
        log.info("Shutting down camera service");
        nantianCameraService.shutdown();
    }
}
