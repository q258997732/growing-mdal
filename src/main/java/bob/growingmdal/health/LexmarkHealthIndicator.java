package bob.growingmdal.health;

import bob.growingmdal.service.LexmarkPrinterService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class LexmarkHealthIndicator implements HealthIndicator {

    private final LexmarkPrinterService lexmarkPrinterService;

    public LexmarkHealthIndicator(LexmarkPrinterService lexmarkPrinterService) {
        this.lexmarkPrinterService = lexmarkPrinterService;
    }

    @Override
    public Health health() {
        return lexmarkPrinterService.health();
    }
}
