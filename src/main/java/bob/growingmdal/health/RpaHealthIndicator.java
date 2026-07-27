package bob.growingmdal.health;

import bob.growingmdal.service.RpaClientService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * K-RPA 健康指示器。
 */
@Component
public class RpaHealthIndicator implements HealthIndicator {

    private final RpaClientService rpaClientService;

    public RpaHealthIndicator(RpaClientService rpaClientService) {
        this.rpaClientService = rpaClientService;
    }

    @Override
    public Health health() {
        return rpaClientService.health();
    }
}
