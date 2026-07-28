package bob.growingmdal.service;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.config.AdapterProperties;
import bob.growingmdal.connector.FileUploadStore;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.dto.upload.ChunkUploadRequest;
import bob.growingmdal.dto.upload.FileUploadRequest;
import bob.growingmdal.hardware.LifecycleManaged;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 文件上传服务。
 */
@Slf4j
@Service
public class FileUploadService extends AnnotationDrivenHandler implements LifecycleManaged {

    public static final String DEVICE_TYPE = "FileUpload";

    private final FileUploadStore fileUploadStore;
    private final ObjectMapper objectMapper;
    private final AdapterProperties adapterProperties;

    public FileUploadService(FileUploadStore fileUploadStore,
                             ObjectMapper objectMapper,
                             AdapterProperties adapterProperties) {
        this.fileUploadStore = fileUploadStore;
        this.objectMapper = objectMapper;
        this.adapterProperties = adapterProperties;
    }

    @Override
    public String getDeviceType() {
        return DEVICE_TYPE;
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "Upload")
    public String upload(DeviceCommand command) {
        FileUploadRequest request;
        try {
            request = objectMapper.readValue(command.getTransferData(), FileUploadRequest.class);
        } catch (IOException e) {
            log.error("Failed to parse upload request: {}", e.getMessage());
            return "error: invalid request";
        }
        if (request.getName() == null || request.getName().isBlank()
                || request.getContentBase64() == null || request.getContentBase64().isBlank()) {
            return "error: name and contentBase64 are required";
        }
        try {
            return fileUploadStore.saveFile(request.getName(), request.getContentBase64());
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            return "error: file storage failed";
        }
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "UploadChunk")
    public boolean uploadChunk(DeviceCommand command) {
        ChunkUploadRequest request;
        try {
            request = objectMapper.readValue(command.getTransferData(), ChunkUploadRequest.class);
        } catch (IOException e) {
            log.error("Failed to parse chunk upload request: {}", e.getMessage());
            return false;
        }
        if (request.getMd5() == null || request.getMd5().isBlank()
                || request.getName() == null || request.getName().isBlank()
                || request.getSize() == null || request.getChunks() == null
                || request.getChunk() == null || request.getChunkSize() == null
                || request.getContentBase64() == null
                || request.getContentBase64().isBlank()) {
            log.error("Chunk upload missing required fields");
            return false;
        }
        try {
            return fileUploadStore.saveChunk(request.getMd5(), request.getName(), request.getSize(),
                    request.getChunks(), request.getChunk(), request.getChunkSize(), request.getContentBase64());
        } catch (IOException e) {
            log.error("Failed to upload chunk: {}", e.getMessage());
            return false;
        }
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "CheckMd5", readOnly = true)
    public boolean checkMd5(DeviceCommand command) {
        try {
            String md5 = objectMapper.readTree(command.getTransferData()).get("md5").asText();
            return fileUploadStore.existsByMd5(md5);
        } catch (IOException | NullPointerException e) {
            log.error("Failed to check md5: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void initialize() {
        log.info("File upload service initialized, path={}", adapterProperties.getFileUploadPath());
    }

    @Override
    public Health health() {
        boolean writable = fileUploadStore.isWritable();
        if (writable) {
            return Health.up()
                    .withDetail("path", adapterProperties.getFileUploadPath())
                    .build();
        }
        return Health.down()
                .withDetail("path", adapterProperties.getFileUploadPath())
                .withDetail("reason", "Upload path not writable")
                .build();
    }

    @Override
    public void shutdown() {
        log.info("File upload service shutdown");
    }
}
