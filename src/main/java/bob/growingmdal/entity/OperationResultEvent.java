package bob.growingmdal.entity;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class OperationResultEvent {
    private final WebSocketSession session;
    private final String result;
}
