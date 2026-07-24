package bob.growingmdal.service;

import bob.growingmdal.adapter.LocalPrinterAdapter;
import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.entity.OperationResultEvent;
import bob.growingmdal.security.PathTraversalValidator;
import bob.growingmdal.hardware.LifecycleManaged;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.actuate.health.Health;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LocalPrinterService extends AnnotationDrivenHandler implements LifecycleManaged {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper;
    private final LocalPrinterAdapter adapter = LocalPrinterAdapter.getInstance();

    @Autowired
    public LocalPrinterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Value("${printer.local.name}")
    private String printerName;

    @Value("${printer.local.allowed-dir:${user.dir}/print-files}")
    private String allowedDir;

    @DeviceOperation(DeviceType = "Printer", ProcessCommand = "PrintLocalPDF")
    public boolean printPDF(DeviceCommand command) {
        String path = command.getTransferData();
        PathTraversalValidator.validate(allowedDir, path);
        return adapter.printPDF(allowedDir, path, printerName);
    }

    @DeviceOperation(DeviceType = "Printer", ProcessCommand = "PrintPDFBase64")
    public boolean printPDFFromBase64(DeviceCommand command) {
        String transferDataJson = command.getTransferData();
        try {
            String normalized = transferDataJson.replace("\\\"", "\"");
            JsonNode transferData = objectMapper.readTree(normalized);

            String base64String = transferData.get("pdfBase64").asText();
            return adapter.printPDFFromBase64(base64String, printerName);
        } catch (Exception e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            command.setTransferData("error :" + e.getMessage());
            performOperation(command);
            return false;
        }
    }


    public void performOperation(DeviceCommand command) {
        eventPublisher.publishEvent(new OperationResultEvent(command.getSession(), command.toString()));
    }

    @Override
    public String getDeviceType() {
        return "Printer";
    }

    @Override
    public void initialize() {
        log.info("Local printer service initialized");
    }

    @Override
    public Health health() {
        boolean exists = adapter.isPrinterExist(printerName);
        if (exists) {
            return Health.up().withDetail("printer", printerName).build();
        }
        return Health.down().withDetail("printer", printerName).withDetail("reason", "printer not found").build();
    }

    @Override
    public void shutdown() {
        log.info("Local printer service shutdown");
    }
}
