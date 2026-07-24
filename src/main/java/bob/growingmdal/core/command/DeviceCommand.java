package bob.growingmdal.core.command;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceCommand {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DeviceCommand() {
    }

    /**
     * 通过JSON字符串构造DeviceCommand
     *
     * @param jsonString JSON字符串
     */
    public DeviceCommand(String jsonString) {
        try {
            DeviceCommand parsed = MAPPER.readValue(jsonString, DeviceCommand.class);
            this.Function = parsed.Function;
            this.DeviceType = parsed.DeviceType;
            this.ProcessCommand = parsed.ProcessCommand;
            this.TransferData = parsed.TransferData;
        } catch (Exception e) {
            log.error("String convert to DeviceCommand error: {}", e.getMessage());
        }
    }

    public DeviceCommand(String Function, String DeviceType, String ProcessCommand, String TransferData) {
        this.Function = Function;
        this.DeviceType = DeviceType;
        this.ProcessCommand = ProcessCommand;
        this.TransferData = TransferData;
    }

    @JsonProperty("Function")
    @JsonAlias({"Function", "function", "func"})
    private String Function;       // Input/Output

    @JsonProperty("DeviceType")
    @JsonAlias({"DeviceType", "DEVICE_TYPE", "device_type", "deviceType"})
    private String DeviceType;     // 设备类型

    @JsonProperty("ProcessCommand")
    @JsonAlias({"ProcessCommand", "COMMAND", "command", "processCommand"})
    private String ProcessCommand; // 操作指令

    @JsonProperty("TransferData")
    private String TransferData;   // 传输数据(JSON字符串)

    @com.fasterxml.jackson.annotation.JsonIgnore
    private WebSocketSession session;   // WebSocket会话

    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("DeviceCommand serialize error: {}", e.getMessage());
            return "{}";
        }
    }
}
