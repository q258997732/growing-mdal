package bob.growingmdal.util.api;

import bob.growingmdal.entity.DeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

@Slf4j
public class MessageConvertUtil {

    /**
     * 检查消息格式
     * @param message 消息
     * @return 格式是否正确
     */
    public static boolean checkMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject( message);
        } catch (JSONException e) {
            return false;
        }
        return message.contains("DeviceType") && message.contains("ProcessCommand") && message.contains("TransferData");
    }

    /**
     * 从消息中提取请求主体DeviceMessage
     * @param message 消息
     * @return 请求主体DeviceMessage
     */
    public static DeviceMessage extractMessage(String message) {
        if(checkMessage(message)){
            return new DeviceMessage(message);
        }else{
            log.error("Invalid message format");
            return null;
        }

    }

}
