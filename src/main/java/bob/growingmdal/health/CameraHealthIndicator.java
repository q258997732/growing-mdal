package bob.growingmdal.health;

import bob.growingmdal.service.NantianCameraService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CameraHealthIndicator implements HealthIndicator {

    private final NantianCameraService nantianCameraService;

    public CameraHealthIndicator(NantianCameraService nantianCameraService) {
        this.nantianCameraService = nantianCameraService;
    }

    @Override
    public Health health() {
        return nantianCameraService.health();
    }
}
