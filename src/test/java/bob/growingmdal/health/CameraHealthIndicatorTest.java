package bob.growingmdal.health;

import bob.growingmdal.service.NantianCameraService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CameraHealthIndicatorTest {

    private final NantianCameraService nantianCameraService = mock(NantianCameraService.class);
    private final CameraHealthIndicator indicator = new CameraHealthIndicator(nantianCameraService);

    @Test
    void shouldReturnUpWhenCameraConnected() {
        when(nantianCameraService.health()).thenReturn(Health.up().withDetail("device", "Nantian Camera").build());

        Health result = indicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("device", "Nantian Camera");
    }

    @Test
    void shouldReturnDownWhenCameraDisconnected() {
        when(nantianCameraService.health()).thenReturn(Health.down().withDetail("device", "Nantian Camera").build());

        Health result = indicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
    }
}
