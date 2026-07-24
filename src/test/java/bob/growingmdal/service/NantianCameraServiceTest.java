package bob.growingmdal.service;

import bob.growingmdal.camera.CameraCommandExecutor;
import bob.growingmdal.camera.CameraLifecycleManager;
import bob.growingmdal.camera.CameraMessageRouter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NantianCameraServiceTest {

    private final CameraLifecycleManager lifecycleManager = mock(CameraLifecycleManager.class);
    private final CameraMessageRouter messageRouter = mock(CameraMessageRouter.class);
    private final CameraCommandExecutor commandExecutor = mock(CameraCommandExecutor.class);
    private final NantianCameraService service = new NantianCameraService(
            lifecycleManager, messageRouter, commandExecutor);

    @Test
    void shouldReportUpWhenConnected() {
        when(lifecycleManager.isConnected()).thenReturn(true);

        assertThat(service.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void shouldReportDownWhenDisconnected() {
        when(lifecycleManager.isConnected()).thenReturn(false);

        assertThat(service.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void shouldNotStartCameraWhenDisabled() {
        when(lifecycleManager.isEnabled()).thenReturn(false);

        service.initialize();

        assertThat(service.health().getStatus()).isEqualTo(Status.DOWN);
    }
}
