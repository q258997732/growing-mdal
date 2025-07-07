package bob.growingmdal.entity.response;

import lombok.Data;

@Data
public class NantianCameraResponse {
    private int code;
    private String message;
    private String data;

    public NantianCameraResponse() {
    }

    public NantianCameraResponse(int code, String message, String data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String toString() {
        return "{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

    // 转换为Json字符串
    public String toJson() {
        return "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":\"" + data + "\"}";
    }

    public boolean isSuccess() {
        return code == 200;
    }

}

