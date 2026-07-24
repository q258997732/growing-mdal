package bob.growingmdal.health;

import bob.growingmdal.service.DekaService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DekaHealthIndicatorTest {

    private final DekaService dekaService = mock(DekaService.class);
    private final DekaHealthIndicator indicator = new DekaHealthIndicator(dekaService);

    @Test
    void shouldReturnUpWhenServiceHealthy() {
        when(dekaService.health()).thenReturn(Health.up().withDetail("device", "Deka T10-MX4").build());

        Health result = indicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("device", "Deka T10-MX4");
    }

    @Test
    void shouldReturnDownWhenServiceUnhealthy() {
        when(dekaService.health()).thenReturn(Health.down().withDetail("device", "Deka T10-MX4").build());

        Health result = indicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
    }
}
