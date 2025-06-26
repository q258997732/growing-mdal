package bob.growingmdal.core.command;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceCommand {

    public DeviceCommand(){
    }

    /**
     * 通过JSON字符串构造DeviceMessage
     * @param jsonString JSON字符串
     */
    public DeviceCommand(String jsonString){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            this.Function = jsonObject.getString("Function");
            this.DeviceType = jsonObject.getString("DeviceType");
            this.ProcessCommand = jsonObject.getString("ProcessCommand");
            this.TransferData = jsonObject.getString("TransferData");
        } catch (JSONException e) {
            log.error("String convert to DeviceMessage err : {}",e.getMessage());
        }
    }

    public DeviceCommand(String Function, String DeviceType, String ProcessCommand, String TransferData){
        this.Function = Function;
        this.DeviceType = DeviceType;
        this.ProcessCommand = ProcessCommand;
        this.TransferData = TransferData;
    }

    @JsonAlias({"Function", "function", "func"})
    private String Function;       // Input/Output

    @JsonProperty("deviceType")
    @JsonAlias({"DeviceType", "DEVICE_TYPE", "device_type"})
    private String DeviceType;     // 设备类型

    @JsonProperty("processCommand")
    @JsonAlias({"ProcessCommand", "COMMAND", "command"})
    private String ProcessCommand; // 操作指令


    private String TransferData;   // 传输数据(JSON字符串)
    private WebSocketSession session;   // WebSocket会话ID


    // 返回Json字符串
    @Override
    public String toString() {
        try {
            return toJSONObject().toString();
        } catch (JSONException e) {
            // 自己拼接json字符串
            return "{" +
                    "\"Function\":\"" + this.Function + "\"," +
                    "\"DeviceType\":\"" + this.DeviceType + "\"," +
                    "\"ProcessCommand\":\"" + this.ProcessCommand + "\"," +
                    "\"TransferData\":\"" + this.TransferData + "\"" +
                    "}";
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Function", this.Function);
        jsonObject.put("DeviceType", this.DeviceType);
        jsonObject.put("ProcessCommand", this.ProcessCommand);
        jsonObject.put("TransferData", this.TransferData);
        return jsonObject;
    }

}