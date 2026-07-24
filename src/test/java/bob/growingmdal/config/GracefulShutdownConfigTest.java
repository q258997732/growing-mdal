package bob.growingmdal.config;

import bob.growingmdal.service.NantianCameraService;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GracefulShutdownConfigTest {

    @Test
    void shouldCloseAllSessionsAndShutdownExecutorAndCamera() throws InterruptedException, IOException {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();

        WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
        NantianCameraService cameraService = mock(NantianCameraService.class);

        GracefulShutdownConfig config = new GracefulShutdownConfig(executor, sessionManager, cameraService);
        config.onApplicationEvent(mock(ContextClosedEvent.class));

        verify(sessionManager).closeAllSessions();
        verify(cameraService).shutdown();
        assertThat(executor.getThreadPoolExecutor().isShutdown()).isTrue();
    }

    @Test
    void shouldAwaitRunningTasksBeforeShutdown() throws InterruptedException {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();

        CountDownLatch latch = new CountDownLatch(1);
        executor.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
        NantianCameraService cameraService = mock(NantianCameraService.class);

        GracefulShutdownConfig config = new GracefulShutdownConfig(executor, sessionManager, cameraService);
        config.onApplicationEvent(mock(ContextClosedEvent.class));

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(executor.getThreadPoolExecutor().isTerminated()).isTrue();
    }
}
