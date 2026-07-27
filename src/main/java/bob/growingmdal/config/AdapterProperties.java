package bob.growingmdal.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "adapter")
public class AdapterProperties {

    @NotBlank
    private String dekaWorkDir = System.getProperty("user.dir")
            + "/src/main/resources/lib/deka_T10-MX4_x64";

    @Positive
    private int dekaReaderUsbPort = 100;

    @Positive
    private int dekaReaderBaud = 115200;

    @Positive
    private int dekaReaderWaitTime = 30000;

    @Positive
    private int dekaReaderLoopPeriod = 2000;

    @NotBlank
    private String printerLocalName = "Lexmark MS439dn";

    @NotBlank
    private String printerLocalAllowedDir = System.getProperty("user.dir") + "/print-files";

    @NotBlank
    private String printerLexmarkIp = "192.168.107.112";

    @Positive
    private int printerLexmarkSnmpPort = 161;

    @NotBlank
    private String printerLexmarkSnmpCommunity = "public";

    @Positive
    private int printerLexmarkSnmpTimeout = 5000;

    @Positive
    private int printerLexmarkSnmpRetry = 3;

    @NotBlank
    private String nantianCameraUrl = "ws://192.168.107.103:7000";

    @Positive
    private int nantianCameraResponseTimeout = 3;

    @Positive
    private int nantianCameraVideoTime = 3000;

    @Positive
    private int nantianCameraDetectTime = 10;

    private boolean nantianCameraEnable = false;

    private boolean nantianCameraAutoReconnect = false;

    @Positive
    private int nantianCameraAutoReconnectInterval = 5000;

    @Positive
    private long nantianMsgCleanInterval = 600000;

    @Positive
    private long nantianVideoCleanInterval = 600000;

    // Toptron 中控配置
    @NotBlank
    private String toptronHost = "192.168.107.200";

    @Positive
    private int toptronPort = 5000;

    @NotBlank
    private String toptronToken = "";

    @Positive
    private int toptronConnectTimeout = 3000;
    @NotBlank
    private String rpaHost = "192.168.107.100";

    @Positive
    private int rpaPort = 80;

    @NotBlank
    private String rpaUser = "";

    @NotBlank
    private String rpaPass = "";

    @Positive
    private int rpaCallFunTimeout = 5000;

    // 文件上传配置
    @NotBlank
    private String fileUploadPath = System.getProperty("user.dir") + "/uploads";
}
