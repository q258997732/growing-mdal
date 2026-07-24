package bob.growingmdal.entity.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommandResponse(String function, String deviceType, String processCommand,
                              boolean success, String data, String errorCode, String errorMessage) {
}
