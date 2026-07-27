package bob.growingmdal.health;

import bob.growingmdal.service.FileUploadService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 文件上传健康指示器。
 */
@Component
public class FileUploadHealthIndicator implements HealthIndicator {

    private final FileUploadService fileUploadService;

    public FileUploadHealthIndicator(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Override
    public Health health() {
        return fileUploadService.health();
    }
}
