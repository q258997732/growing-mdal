package bob.growingmdal.service;

import bob.growingmdal.config.AdapterProperties;
import bob.growingmdal.connector.RpaHttpConnector;
import bob.growingmdal.core.command.DeviceCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RpaClientServiceTest {

    @Mock
    private RpaHttpConnector rpaHttpConnector;

    @Mock
    private AdapterProperties adapterProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RpaClientService service;

    @BeforeEach
    void setUp() {
        service = new RpaClientService(rpaHttpConnector, objectMapper, adapterProperties);
    }

    @Test
    void shouldReturnDeviceTypeRpa() {
        assertThat(service.getDeviceType()).isEqualTo("Rpa");
    }

    @Test
    void shouldSupportRpaCommands() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Rpa");
        assertThat(service.supports(command)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCallComponentMissingFields() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"script\":\"test\"}");

        boolean result = service.callComponent(command);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAddDataQueueMissingFields() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"flow\":\"test\"}");

        boolean result = service.addDataQueue(command);

        assertThat(result).isFalse();
    }
}
