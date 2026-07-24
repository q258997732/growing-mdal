package bob.growingmdal.service;

import bob.growingmdal.adapter.DekaReaderAdapter;
import bob.growingmdal.config.WebSocketSessionManager;
import bob.growingmdal.core.command.DeviceCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DekaServiceUnitTest {

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DekaReaderAdapter adapter = mock(DekaReaderAdapter.class);
    private final DekaService service = new DekaService(publisher, sessionManager, objectMapper, adapter);

    @Test
    void shouldReturnErrorWhenInitFails() {
        when(adapter.dc_init(anyShort(), anyInt())).thenReturn(-1);

        DeviceCommand command = new DeviceCommand();
        command.setSession(mock(WebSocketSession.class));
        Object result = service.getIDCardInfo(command);

        assertThat(result).isInstanceOf(String.class);
        assertThat(result).asString().contains("init deka readcard device failed");
    }

    @Test
    void shouldCallExitOnReadFailure() {
        when(adapter.dc_init(anyShort(), anyInt())).thenReturn(1);
        when(adapter.dc_beep(anyInt(), anyShort())).thenReturn((short) 0);
        when(adapter.dc_find_i_d(anyInt())).thenReturn((short) 0);
        when(adapter.dc_SamAReadCardInfo(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn((short) -1);
        when(adapter.dc_exit(anyInt())).thenReturn((short) 0);

        DeviceCommand command = new DeviceCommand();
        command.setSession(mock(WebSocketSession.class));
        service.getIDCardInfo(command);

        verify(adapter, times(1)).dc_exit(anyInt());
    }

    @Test
    void shouldNotShareContextBetweenCalls() {
        when(adapter.dc_init(anyShort(), anyInt())).thenReturn(-1);

        DeviceCommand command1 = new DeviceCommand();
        DeviceCommand command2 = new DeviceCommand();
        command1.setSession(mock(WebSocketSession.class));
        command2.setSession(mock(WebSocketSession.class));

        service.getIDCardInfo(command1);
        service.getIDCardInfo(command2);

        verify(adapter, times(2)).dc_init(anyShort(), anyInt());
        verify(adapter, never()).dc_exit(anyInt());
    }

    @Test
    void shouldResetCheckingFlagOnInitFailure() {
        when(adapter.dc_init(anyShort(), anyInt())).thenReturn(-1);

        DeviceCommand command = new DeviceCommand();
        command.setSession(mock(WebSocketSession.class));
        service.getIDCardInfo(command);

        // If flag was not reset, the next call would return "already in progress"
        Object result = service.getIDCardInfo(command);
        assertThat(result).asString().doesNotContain("already in progress");
    }

    @Test
    void shouldCancelCheck() {
        assertThat(service.cancelIdCardCheck()).isEqualTo("No ID card check in progress to cancel");
    }
}
