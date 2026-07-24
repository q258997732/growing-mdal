package bob.growingmdal.core.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceCommandSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeToValidJson() {
        DeviceCommand command = new DeviceCommand("InPut", "IDCard", "getIDCardInfo", "");

        String json = command.toString();

        assertThat(json).contains("\"Function\":\"InPut\"");
        assertThat(json).contains("\"DeviceType\":\"IDCard\"");
        assertThat(json).contains("\"ProcessCommand\":\"getIDCardInfo\"");
        assertThat(json).contains("\"TransferData\":\"\"");
    }

    @Test
    void shouldRoundTripViaObjectMapper() throws Exception {
        DeviceCommand original = new DeviceCommand("InPut", "Printer", "PrintLocalPDF", "doc.pdf");

        String json = objectMapper.writeValueAsString(original);
        DeviceCommand parsed = objectMapper.readValue(json, DeviceCommand.class);

        assertThat(parsed.getFunction()).isEqualTo(original.getFunction());
        assertThat(parsed.getDeviceType()).isEqualTo(original.getDeviceType());
        assertThat(parsed.getProcessCommand()).isEqualTo(original.getProcessCommand());
        assertThat(parsed.getTransferData()).isEqualTo(original.getTransferData());
    }

    @Test
    void shouldParseFromJsonStringConstructor() {
        String json = "{\"Function\":\"InPut\",\"DeviceType\":\"Camera\",\"ProcessCommand\":\"Capture\",\"TransferData\":\"1\"}";

        DeviceCommand command = new DeviceCommand(json);

        assertThat(command.getFunction()).isEqualTo("InPut");
        assertThat(command.getDeviceType()).isEqualTo("Camera");
        assertThat(command.getProcessCommand()).isEqualTo("Capture");
        assertThat(command.getTransferData()).isEqualTo("1");
    }

    @Test
    void shouldEscapeQuotesInTransferData() throws Exception {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"key\":\"value\"}");

        String json = command.toString();
        DeviceCommand parsed = objectMapper.readValue(json, DeviceCommand.class);

        assertThat(parsed.getTransferData()).isEqualTo(command.getTransferData());
    }
}
