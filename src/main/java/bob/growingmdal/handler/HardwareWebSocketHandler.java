package bob.growingmdal.handler;

import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.entity.OperationResultEvent;
import bob.growingmdal.service.CommandDispatcherService;
import bob.growingmdal.service.WebSocketSessionManager;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class HardwareWebSocketHandler extends TextWebSocketHandler {

    @EventListener
    public void handleOperationResult(OperationResultEvent event) {
        sendToClient(event.getSession(), event.getResult());
    }

    private TaskExecutor messageTaskExecutor;
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CommandDispatcherService dispatcher;
    private final WebSocketSessionManager sessionManager;

    public HardwareWebSocketHandler(CommandDispatcherService dispatcher,
                                    WebSocketSessionManager sessionManager) {
        this.dispatcher = dispatcher;
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionManager.registerSession(session.getId(), session);
        sendToClient(session, "CONNECTED");
        log.info("Session established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        if (!isValidJson(payload)) {
            sendError(session, "INVALID_JSON", "Payload is not valid JSON");
            return;
        }
        log.info("Received raw message: {}", payload);
        try {
            // 配置更健壮的 ObjectMapper
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);

            DeviceCommand command = mapper.readValue(payload, DeviceCommand.class);
            command.setFunction("OutPut");

            // 详细日志记录解析结果
            log.debug("Parsed command: deviceType={}, processCommand={}",
                    command.getDeviceType(), command.getProcessCommand());

            // 关键验证：确保必要字段存在
            if (command.getDeviceType() == null || command.getDeviceType().isBlank()) {
                throw new IllegalArgumentException("Missing required field: deviceType");
            }

            if (command.getProcessCommand() == null || command.getProcessCommand().isBlank()) {
                throw new IllegalArgumentException("Missing required field: processCommand");
            }
            command.setSession(session);
            Object result = dispatcher.dispatch(command);
            if (result != null)
                sendToClient(session, result.toString());
            else
                sendToClient(session, "fail");
        } catch (Exception e) {
            log.debug("Failed to process message: {}", payload, e);
            sendError(session, "INVALID_COMMAND",
                    "Error: " + e.getMessage());
        }

    }

    private void sendError(WebSocketSession session, String errorCode, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                    "error", errorCode,
                    "message", errorMessage,
                    "timestamp", Instant.now().toString()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (Exception e) {
            log.error("Failed to send error message", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("Transport error for session {}: {}", session.getId(), exception.getMessage());
        sendError(session, "TRANSPORT_ERROR", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.unregisterSession(session.getId());
        log.info("Session closed: {} with status {}", session.getId(), status);
    }

    /**
     * 发送消息给指定客户端
     *
     * @param session 当前客户端会话
     * @param message 要发送的消息内容
     */
    public static void sendToClient(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 广播给所有客户端
     *
     * @param message 要发送的消息内容
     */
    public static void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private boolean isValidJson(String json) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}