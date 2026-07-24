package bob.growingmdal.health;

import bob.growingmdal.service.LocalPrinterService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrinterHealthIndicatorTest {

    private final LocalPrinterService localPrinterService = mock(LocalPrinterService.class);
    private final PrinterHealthIndicator indicator = new PrinterHealthIndicator(localPrinterService);

    @Test
    void shouldReturnUpWhenPrinterExists() {
        when(localPrinterService.health()).thenReturn(Health.up().withDetail("printer", "Lexmark MS439dn").build());

        Health result = indicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("printer", "Lexmark MS439dn");
    }

    @Test
    void shouldReturnDownWhenPrinterMissing() {
        when(localPrinterService.health()).thenReturn(Health.down().withDetail("reason", "printer not found").build());

        Health result = indicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
    }
}
