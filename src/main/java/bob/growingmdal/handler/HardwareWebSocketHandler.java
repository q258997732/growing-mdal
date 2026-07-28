package bob.growingmdal.handler;

import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.entity.OperationResultEvent;
import bob.growingmdal.entity.response.CommandResponse;
import bob.growingmdal.service.CommandDispatcherService;
import bob.growingmdal.config.WebSocketSessionManager;
import bob.growingmdal.service.WebSocketOutboundService;
import bob.growingmdal.validation.CommandValidator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Slf4j
public class HardwareWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final CommandDispatcherService dispatcher;
    private final WebSocketSessionManager sessionManager;
    private final TaskExecutor taskExecutor;
    private final WebSocketOutboundService outboundService;

    public HardwareWebSocketHandler(CommandDispatcherService dispatcher,
                                    WebSocketSessionManager sessionManager,
                                    @Qualifier("messageTaskExecutor") TaskExecutor taskExecutor,
                                    ObjectMapper objectMapper,
                                    WebSocketOutboundService outboundService) {
        this.dispatcher = dispatcher;
        this.sessionManager = sessionManager;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
        this.outboundService = outboundService;
    }

    @EventListener
    public void handleOperationResult(OperationResultEvent event) {
        outboundService.sendStreamEvent(event.getSession(), event.getResult());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionManager.registerSession(session.getId(), session);
        outboundService.sendCommandResponse(session, "CONNECTED");
        log.info("Session established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        taskExecutor.execute(() -> processMessage(session, message));
    }

    private void processMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        if (!isValidJson(payload)) {
            sendError(session, "INVALID_JSON", "Payload is not valid JSON");
            return;
        }
        try {
            // 配置更健壮的 ObjectMapper
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);

            DeviceCommand command = mapper.readValue(payload, DeviceCommand.class);
            command.setFunction("OutPut");

            CommandValidator.validate(command);

            command.setSession(session);

            // 判断是否摄像头命令
            if (isCameraCommand(command)) {
                return;
            }

            String result = dispatcher.dispatch(command);
            sendCommandResponse(session, command, true, result, null, null);

        } catch (Exception e) {
            log.debug("Failed to process message: {}", payload, e);
            sendError(session, "INVALID_COMMAND", "Error: " + e.getMessage());
        }
    }

    private void sendCommandResponse(WebSocketSession session, DeviceCommand command,
                                     boolean success, String data, String errorCode, String errorMessage) {
        if (session == null || !session.isOpen()) {
            return;
        }
        CommandResponse response = new CommandResponse(
                command.getFunction(),
                command.getDeviceType(),
                command.getProcessCommand(),
                success,
                data,
                errorCode,
                errorMessage
        );
        try {
            outboundService.sendCommandResponse(session, objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("Failed to send command response", e);
        }
    }

    private void sendError(WebSocketSession session, String errorCode, String errorMessage) {
        if (session == null || !session.isOpen()) {
            return;
        }
        CommandResponse response = new CommandResponse(
                "OutPut", null, null, false, null, errorCode, errorMessage);
        try {
            outboundService.sendCommandResponse(session, objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("Failed to send error message", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("Transport error for session {}: {}", session.getId(), exception.getMessage());

        if (session != null && session.isOpen()) {
            sendError(session, "TRANSPORT_ERROR", exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.unregisterSession(session.getId());
        log.info("Session closed: {} with status {}", session.getId(), status);
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

    public void closeAllSessions() {
        sessionManager.closeAllSessions();
    }

    private boolean isCameraCommand(DeviceCommand command) {
        return command.getProcessCommand().equals("Camera");
    }
}
