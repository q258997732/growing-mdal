package bob.growingmdal.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebSocketOutboundServiceTest {

    private final TaskExecutor outboundTaskExecutor = mock(TaskExecutor.class);
    private final WebSocketOutboundService outboundService = new WebSocketOutboundService(outboundTaskExecutor);

    @Test
    void sendCommandResponseShouldWriteSynchronously() throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        outboundService.sendCommandResponse(session, "{\"ok\":true}");

        verify(outboundTaskExecutor, never()).execute(any(Runnable.class));
        verify(session).sendMessage(new TextMessage("{\"ok\":true}"));
    }

    @Test
    void sendStreamEventShouldSubmitToExecutor() throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        outboundService.sendStreamEvent(session, "frame");

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(outboundTaskExecutor).execute(captor.capture());
        captor.getValue().run();

        verify(session).sendMessage(new TextMessage("frame"));
    }

    @Test
    void sendShouldSynchronizeOnSession() throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);

        outboundService.sendCommandResponse(session, "msg");

        verify(session, times(1)).sendMessage(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getPayload()).isEqualTo("msg");
    }

    @Test
    void sendShouldSkipClosedSession() throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(false);

        outboundService.sendCommandResponse(session, "msg");

        verify(session, never()).sendMessage(any(TextMessage.class));
    }
}
