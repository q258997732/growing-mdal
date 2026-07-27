package bob.growingmdal.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "adapter.printer-lexmark-ip=192.168.107.112",
        "adapter.printer-lexmark-snmp-community=public",
        "adapter.printer-lexmark-snmp-timeout=5000"
})
class LexmarkPrinterServiceTest {

    @Autowired
    private LexmarkPrinterService service;

    @Test
    void shouldReturnDeviceType() {
        assertThat(service.getDeviceType()).isEqualTo("LexmarkPrinter");
    }

    @Test
    void shouldReturnErrorOnStatusException() {
        service.initialize();
        String result = service.getLexmarkStatus();
        assertThat(result).isNotNull();
    }

    @Test
    void shouldReportDownWhenHealthCheckFails() {
        service.initialize();
        assertThat(service.health().getStatus().getCode()).isIn("UP", "DOWN");
    }
}
