package bob.growingmdal.handler;

import bob.growingmdal.config.WebSocketSessionManager;
import bob.growingmdal.service.CommandDispatcherService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class HardwareWebSocketHandlerTest {

    private final CommandDispatcherService dispatcher = mock(CommandDispatcherService.class);
    private final WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
    private final TaskExecutor taskExecutor = mock(TaskExecutor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HardwareWebSocketHandler handler =
            new HardwareWebSocketHandler(dispatcher, sessionManager, taskExecutor, objectMapper);

    @Test
    void shouldSubmitMessageToTaskExecutor() {
        WebSocketSession session = mock(WebSocketSession.class);
        TextMessage message = new TextMessage("{\"DeviceType\":\"Printer\",\"ProcessCommand\":\"PrintLocalPDF\"}");

        handler.handleTextMessage(session, message);

        verify(taskExecutor).execute(any(Runnable.class));
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    void shouldSendCommandResponseToSession() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        TextMessage message = new TextMessage("{\"DeviceType\":\"Printer\",\"ProcessCommand\":\"PrintLocalPDF\"}");
        when(dispatcher.dispatch(any())).thenReturn("\"ok\"");

        handler.handleTextMessage(session, message);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getPayload()).contains("\"success\":true");
        assertThat(messageCaptor.getValue().getPayload()).contains("\"deviceType\":\"Printer\"");
        assertThat(messageCaptor.getValue().getPayload()).contains("\"processCommand\":\"PrintLocalPDF\"");
    }
}
