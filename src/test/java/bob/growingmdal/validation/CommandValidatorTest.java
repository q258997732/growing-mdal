package bob.growingmdal.validation;

import bob.growingmdal.core.command.DeviceCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class CommandValidatorTest {

    @Test
    void shouldAcceptValidCommand() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Printer");
        command.setProcessCommand("PrintLocalPDF");
        command.setTransferData("/docs/report.pdf");

        assertThatNoException().isThrownBy(() -> CommandValidator.validate(command));
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(() -> CommandValidator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("command is null");
    }

    @Test
    void shouldRejectNullDeviceType() {
        DeviceCommand command = new DeviceCommand();
        command.setProcessCommand("PrintLocalPDF");

        assertThatThrownBy(() -> CommandValidator.validate(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deviceType is required");
    }

    @Test
    void shouldRejectBlankProcessCommand() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Printer");
        command.setProcessCommand("   ");

        assertThatThrownBy(() -> CommandValidator.validate(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("processCommand is required");
    }

    @Test
    void shouldRejectOversizedTransferData() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Printer");
        command.setProcessCommand("PrintLocalPDF");
        command.setTransferData("a".repeat(10_001));

        assertThatThrownBy(() -> CommandValidator.validate(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transferData exceeds maximum length");
    }
}
