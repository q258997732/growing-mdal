package bob.growingmdal.handler;

import bob.growingmdal.config.WebSocketSessionManager;
import bob.growingmdal.service.CommandDispatcherService;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class HardwareWebSocketHandlerTest {

    private final CommandDispatcherService dispatcher = mock(CommandDispatcherService.class);
    private final WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
    private final TaskExecutor taskExecutor = mock(TaskExecutor.class);
    private final HardwareWebSocketHandler handler =
            new HardwareWebSocketHandler(dispatcher, sessionManager, taskExecutor);

    @Test
    void shouldSubmitMessageToTaskExecutor() {
        WebSocketSession session = mock(WebSocketSession.class);
        TextMessage message = new TextMessage("{\"DeviceType\":\"Printer\",\"ProcessCommand\":\"PrintLocalPDF\"}");

        handler.handleTextMessage(session, message);

        verify(taskExecutor).execute(any(Runnable.class));
        verifyNoMoreInteractions(dispatcher);
    }
}
