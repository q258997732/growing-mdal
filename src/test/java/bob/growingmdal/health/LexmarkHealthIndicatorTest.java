package bob.growingmdal.health;

import bob.growingmdal.service.LexmarkPrinterService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LexmarkHealthIndicatorTest {

    private final LexmarkPrinterService lexmarkPrinterService = mock(LexmarkPrinterService.class);
    private final LexmarkHealthIndicator indicator = new LexmarkHealthIndicator(lexmarkPrinterService);

    @Test
    void shouldReturnUpWhenPrinterResponds() {
        when(lexmarkPrinterService.health()).thenReturn(Health.up().withDetail("printer", "192.168.107.112").build());

        Health result = indicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("printer", "192.168.107.112");
    }

    @Test
    void shouldReturnDownWhenPrinterUnreachable() {
        when(lexmarkPrinterService.health()).thenReturn(Health.down().withDetail("printer", "192.168.107.112").build());

        Health result = indicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
    }
}
