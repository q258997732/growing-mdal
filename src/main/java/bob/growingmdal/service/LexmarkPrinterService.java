package bob.growingmdal.service;

import bob.growingmdal.adapter.LexmarkPrinterAdapter;
import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Data
@Service
public class LexmarkPrinterService extends AnnotationDrivenHandler {

    LexmarkPrinterAdapter lexmarkPrinterAdapter;

    @Value("${printer.lexmark.ip}")
    private String ip;
    @Value("${printer.lexmark.snmp.community}")
    private String community;
    @Value("${printer.lexmark.snmp.timeout}")
    private int timeout;
    @Value("${printer.lexmark.snmp.retry}")
    private int retries;


    @PostConstruct
    public void init() {
        lexmarkPrinterAdapter = new LexmarkPrinterAdapter(ip, community);
    }

    @DeviceOperation(DeviceType = "Printer", ProcessCommand = "getLexmarkErrStatus")
    public String getLexmarkErrStatus() {
        try {
            return lexmarkPrinterAdapter.getPrinterErrStatus();
        } catch (IOException e) {
            log.error("get lexmark printer status error", e);
            return "error ,"+e.getMessage() ;
        }
    }

    @DeviceOperation(DeviceType = "Printer", ProcessCommand = "getLexmarkStatus")
    public String getLexmarkStatus() {
        try {
            return lexmarkPrinterAdapter.getPrinterStatus();
        } catch (IOException e) {
            log.error("get lexmark printer status error", e);
            return "error ,"+e.getMessage() ;
        }
    }

    @Override
    public boolean supports(DeviceCommand command) {
        return "Printer".equals(command.getDeviceType());
    }
}
