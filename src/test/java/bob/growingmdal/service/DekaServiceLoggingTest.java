package bob.growingmdal.service;

import bob.growingmdal.adapter.DekaReaderAdapter;
import bob.growingmdal.config.WebSocketSessionManager;
import bob.growingmdal.core.command.DeviceCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class DekaServiceLoggingTest {

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DekaReaderAdapter adapter = mock(DekaReaderAdapter.class);
    private final DekaService service = new DekaService(publisher, sessionManager, objectMapper, adapter);

    @Test
    void shouldNotLogPiiOnInitFailure(CapturedOutput output) {
        when(adapter.dc_init(anyShort(), anyInt())).thenReturn(-1);

        DeviceCommand command = new DeviceCommand();
        command.setSession(mock(WebSocketSession.class));
        service.getIDCardInfo(command);

        // No real PII in this path, but ensure no unexpected content leaks
        assertThat(output.getAll()).doesNotContain("name", "id_number", "address", "tmp.bmp");
    }

    @Test
    void shouldNotLogDomesticPii(CapturedOutput output) throws Exception {
        when(adapter.dc_init(anyShort(), anyInt())).thenReturn(1);
        when(adapter.dc_beep(anyInt(), anyShort())).thenReturn((short) 0);
        when(adapter.dc_find_i_d(anyInt())).thenReturn((short) 0);
        when(adapter.dc_SamAReadCardInfo(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn((short) 0);
        when(adapter.dc_ParseTextInfo(anyInt(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn((short) 0);
        when(adapter.dc_ParsePhotoInfo(anyInt(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn((short) -1);
        when(adapter.dc_exit(anyInt())).thenReturn((short) 0);

        DeviceCommand command = new DeviceCommand();
        command.setSession(mock(WebSocketSession.class));
        service.getIDCardInfo(command);

        String logs = output.getAll();
        assertThat(logs).doesNotContain("张三");
        assertThat(logs).doesNotContain("110101");
    }
}
