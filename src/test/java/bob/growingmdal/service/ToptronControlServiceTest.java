package bob.growingmdal.service;

import bob.growingmdal.config.AdapterProperties;
import bob.growingmdal.connector.ToptronTcpConnector;
import bob.growingmdal.core.command.DeviceCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToptronControlServiceTest {

    @Mock
    private ToptronTcpConnector toptronTcpConnector;

    @Mock
    private AdapterProperties adapterProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ToptronControlService service;

    @BeforeEach
    void setUp() {
        service = new ToptronControlService(toptronTcpConnector, objectMapper, adapterProperties);
    }

    @Test
    void shouldReturnDeviceTypeToptron() {
        assertThat(service.getDeviceType()).isEqualTo("Toptron");
    }

    @Test
    void shouldSupportToptronCommands() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Toptron");
        assertThat(service.supports(command)).isTrue();
    }

    @Test
    void shouldNotSupportOtherDeviceTypes() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Printer");
        assertThat(service.supports(command)).isFalse();
    }

    @Test
    void shouldSendPowerControl() {
        when(toptronTcpConnector.sendPowerControl("1", true)).thenReturn(true);

        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"gatePosition\":\"1\",\"turnon\":true}");

        boolean result = service.powerControl(command);

        assertThat(result).isTrue();
        verify(toptronTcpConnector).sendPowerControl("1", true);
    }

    @Test
    void shouldReturnFalseWhenPowerControlMissingFields() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"turnon\":true}");

        boolean result = service.powerControl(command);

        assertThat(result).isFalse();
    }

    @Test
    void shouldSendRawControl() {
        when(toptronTcpConnector.sendRawMessage("fa000002000305fdff00010155")).thenReturn(true);

        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"controlMsg\":\"fa000002000305fdff00010155\"}");

        boolean result = service.rawControl(command);

        assertThat(result).isTrue();
        verify(toptronTcpConnector).sendRawMessage("fa000002000305fdff00010155");
    }

    @Test
    void shouldReturnFalseWhenRawControlMissingMessage() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{}");

        boolean result = service.rawControl(command);

        assertThat(result).isFalse();
    }
}
