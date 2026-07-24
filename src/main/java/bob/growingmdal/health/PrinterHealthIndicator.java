package bob.growingmdal.health;

import bob.growingmdal.service.LocalPrinterService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PrinterHealthIndicator implements HealthIndicator {

    private final LocalPrinterService localPrinterService;

    public PrinterHealthIndicator(LocalPrinterService localPrinterService) {
        this.localPrinterService = localPrinterService;
    }

    @Override
    public Health health() {
        return localPrinterService.health();
    }
}
