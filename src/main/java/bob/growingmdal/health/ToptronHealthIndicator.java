package bob.growingmdal.health;

import bob.growingmdal.service.ToptronControlService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Toptron 中控健康指示器。
 */
@Component
public class ToptronHealthIndicator implements HealthIndicator {

    private final ToptronControlService toptronControlService;

    public ToptronHealthIndicator(ToptronControlService toptronControlService) {
        this.toptronControlService = toptronControlService;
    }

    @Override
    public Health health() {
        return toptronControlService.health();
    }
}
