package bob.growingmdal.health;

import bob.growingmdal.service.DekaService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DekaHealthIndicator implements HealthIndicator {

    private final DekaService dekaService;

    public DekaHealthIndicator(DekaService dekaService) {
        this.dekaService = dekaService;
    }

    @Override
    public Health health() {
        return dekaService.health();
    }
}
