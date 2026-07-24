package bob.growingmdal.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import jakarta.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AdapterProperties.class, AdapterPropertiesTest.TestConfig.class})
@TestPropertySource(properties = {
        "adapter.printer-local-name=Test Printer",
        "adapter.printer-lexmark-ip=10.0.0.1",
        "adapter.nantian-camera-url=ws://10.0.0.2:7000"
})
class AdapterPropertiesTest {

    @Resource
    private AdapterProperties adapterProperties;

    @Test
    void shouldBindFromEnvironmentVariables() {
        assertThat(adapterProperties.getPrinterLocalName()).isEqualTo("Test Printer");
        assertThat(adapterProperties.getPrinterLexmarkIp()).isEqualTo("10.0.0.1");
        assertThat(adapterProperties.getNantianCameraUrl()).isEqualTo("ws://10.0.0.2:7000");
    }

    @Test
    void shouldUseDefaultWhenEnvMissing() {
        assertThat(adapterProperties.getDekaReaderWaitTime()).isEqualTo(30000);
        assertThat(adapterProperties.getPrinterLexmarkSnmpCommunity()).isEqualTo("public");
        assertThat(adapterProperties.getNantianCameraResponseTimeout()).isEqualTo(3);
    }

    @Configuration
    @EnableConfigurationProperties(AdapterProperties.class)
    static class TestConfig {
    }
}
