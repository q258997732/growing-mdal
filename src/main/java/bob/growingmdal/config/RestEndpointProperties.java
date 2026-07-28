package bob.growingmdal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * REST 硬件命令接口开关配置。
 */
@Data
@Validated
@ConfigurationProperties(prefix = "adapter.rest")
public class RestEndpointProperties {

    /**
     * 全局总开关。关闭后所有 REST 命令接口返回 503。
     */
    private boolean enabled = true;

    private boolean idcard = true;
    private boolean printer = true;
    private boolean lexmarkPrinter = true;
    private boolean toptron = true;
    private boolean rpa = true;
    private boolean fileUpload = true;

    /**
     * 摄像头 REST 默认关闭，需显式开启。
     */
    private boolean camera = false;

    /**
     * 根据 deviceType 判断是否启用 REST。
     */
    public boolean isDeviceEnabled(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        return switch (deviceType) {
            case "IDCard" -> idcard;
            case "Printer" -> printer;
            case "LexmarkPrinter" -> lexmarkPrinter;
            case "Toptron" -> toptron;
            case "Rpa" -> rpa;
            case "FileUpload" -> fileUpload;
            case "Camera" -> camera;
            default -> false;
        };
    }
}
