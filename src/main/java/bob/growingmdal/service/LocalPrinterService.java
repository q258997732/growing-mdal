package bob.growingmdal.service;

import bob.growingmdal.adapter.LocalPrinterAdapter;
import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.entity.OperationResultEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LocalPrinterService extends AnnotationDrivenHandler {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${printer.local.name}")
    private String printerName;

    private final LocalPrinterAdapter adapter = LocalPrinterAdapter.getInstance();

    @DeviceOperation(DeviceType = "Printer", ProcessCommand = "PrintLocalPDF")
    public boolean printPDF(DeviceCommand command) {
        String path = command.getTransferData();
        return adapter.printPDF(path, printerName);
    }

    @DeviceOperation(DeviceType = "Printer", ProcessCommand = "PrintPDFBase64")
    public boolean printPDFFromBase64(DeviceCommand command) {
        String transferDataJson = command.getTransferData();
        try {
            transferDataJson = transferDataJson.replace("\\\"", "\"");
            JSONObject transferData = new JSONObject(transferDataJson);

            String base64String = transferData.getString("pdfBase64");
            return adapter.printPDFFromBase64(base64String, printerName);
        } catch (JSONException e) {
            log.error("Error parsing JSON: " + e.getMessage());
            command.setTransferData("error :" + e.getMessage());
            performOperation(command);
            return false;
        }
    }


    public void performOperation(DeviceCommand command) {
        eventPublisher.publishEvent(new OperationResultEvent(command.getSession(), command.toString()));
    }

    @Override
    public boolean supports(DeviceCommand command) {
        return "Printer".equals(command.getDeviceType());
    }
}
