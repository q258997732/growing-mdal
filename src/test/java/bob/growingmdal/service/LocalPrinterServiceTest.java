package bob.growingmdal.service;

import bob.growingmdal.core.command.DeviceCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "adapter.printer-local-name=TestPrinter",
        "adapter.printer-local-allowed-dir=${user.dir}/print-files"
})
class LocalPrinterServiceTest {

    @Autowired
    private LocalPrinterService service;

    @Test
    void shouldReturnFalseForInvalidBase64Json() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("not-valid-json");

        boolean result = service.printPDFFromBase64(command);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnDeviceType() {
        assertThat(service.getDeviceType()).isEqualTo("Printer");
    }

    @Test
    void shouldReportPrinterHealth() {
        assertThat(service.health().getStatus().getCode()).isIn("UP", "DOWN");
    }
}
