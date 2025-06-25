package bob.growingmdal.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

@Slf4j
@Data
public class DeviceMessage {

    public DeviceMessage(){
    }

    /**
     * 通过JSON字符串构造DeviceMessage
     * @param jsonString JSON字符串
     */
    public DeviceMessage(String jsonString){
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

    private String Function;       // Input/Output
    private String DeviceType;     // 设备类型
    private String ProcessCommand; // 操作指令
    private String TransferData;   // 传输数据(JSON字符串)

    @Override
    public String toString() {
        return "DeviceMessage{" +
                "Function='" + Function + '\'' +
                ", DeviceType='" + DeviceType + '\'' +
                ", ProcessCommand='" + ProcessCommand + '\'' +
                ", TransferData='" + TransferData + '\'' +
                '}';
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