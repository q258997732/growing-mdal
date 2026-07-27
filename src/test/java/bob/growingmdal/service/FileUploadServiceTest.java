package bob.growingmdal.service;

import bob.growingmdal.config.AdapterProperties;
import bob.growingmdal.connector.FileUploadStore;
import bob.growingmdal.core.command.DeviceCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    @Mock
    private FileUploadStore fileUploadStore;

    @Mock
    private AdapterProperties adapterProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private FileUploadService service;

    @BeforeEach
    void setUp() {
        service = new FileUploadService(fileUploadStore, objectMapper, adapterProperties);
    }

    @Test
    void shouldReturnDeviceTypeFileUpload() {
        assertThat(service.getDeviceType()).isEqualTo("FileUpload");
    }

    @Test
    void shouldSupportFileUploadCommands() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("FileUpload");
        assertThat(service.supports(command)).isTrue();
    }

    @Test
    void shouldReturnErrorWhenUploadMissingFields() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"name\":\"test.txt\"}");

        String result = service.upload(command);

        assertThat(result).startsWith("error:");
    }

    @Test
    void shouldUploadFile() throws IOException {
        String content = Base64.getEncoder().encodeToString("hello".getBytes());
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"name\":\"test.txt\",\"contentBase64\":\"" + content + "\"}");
        when(fileUploadStore.saveFile("test.txt", content)).thenReturn("uuid-123");

        String result = service.upload(command);

        assertThat(result).isEqualTo("uuid-123");
    }

    @Test
    void shouldCheckMd5() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"md5\":\"abc123\"}");
        when(fileUploadStore.existsByMd5("abc123")).thenReturn(true);

        boolean result = service.checkMd5(command);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCheckMd5MissingField() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{}");

        boolean result = service.checkMd5(command);

        assertThat(result).isFalse();
    }

    @Test
    void shouldUploadChunk() throws IOException {
        String content = Base64.getEncoder().encodeToString("chunk1".getBytes());
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"md5\":\"abc123\",\"name\":\"test.txt\",\"size\":12,\"chunks\":2,\"chunk\":0,\"chunkSize\":6,\"contentBase64\":\"" + content + "\"}");
        when(fileUploadStore.saveChunk("abc123", "test.txt", 12L, 2, 0, 6, content)).thenReturn(false);

        boolean result = service.uploadChunk(command);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenUploadChunkMissingFields() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"md5\":\"abc123\"}");

        boolean result = service.uploadChunk(command);

        assertThat(result).isFalse();
    }
}
