package bob.growingmdal.service;

import bob.growingmdal.adapter.LexmarkPrinterAdapter;
import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.hardware.LifecycleManaged;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class LexmarkPrinterService extends AnnotationDrivenHandler implements LifecycleManaged {

    LexmarkPrinterAdapter lexmarkPrinterAdapter;

    @Value("${adapter.printer-lexmark-ip}")
    private String ip;
    @Value("${adapter.printer-lexmark-snmp-community}")
    private String community;
    @Value("${adapter.printer-lexmark-snmp-timeout}")
    private int timeout;
    @Value("${adapter.printer-lexmark-snmp-retry}")
    private int retries;


    public void initialize() {
        lexmarkPrinterAdapter = new LexmarkPrinterAdapter(ip, community, timeout);
    }

    @DeviceOperation(DeviceType = "LexmarkPrinter", ProcessCommand = "getLexmarkErrStatus", readOnly = true)
    public String getLexmarkErrStatus() {
        try {
            return lexmarkPrinterAdapter.getPrinterErrStatus();
        } catch (IOException e) {
            log.error("get lexmark printer status error", e);
            return "error ,"+e.getMessage() ;
        }
    }

    @DeviceOperation(DeviceType = "LexmarkPrinter", ProcessCommand = "getLexmarkStatus", readOnly = true)
    public String getLexmarkStatus() {
        try {
            return lexmarkPrinterAdapter.getPrinterStatus();
        } catch (IOException e) {
            log.error("get lexmark printer status error", e);
            return "error ,"+e.getMessage() ;
        }
    }

    @DeviceOperation(DeviceType = "LexmarkPrinter", ProcessCommand = "getLexmarkPrintingAvailable", readOnly = true)
    public String getLexmarkPrintingAvailable() {
        try {
            String status = lexmarkPrinterAdapter.getPrinterStatus();
            String err = lexmarkPrinterAdapter.getPrinterErrStatus();
            if("设备状态正常，无异常".equals(err)&&"空闲".equals(status)){
                return "打印已就绪";
            }else{
                return "打印未就绪, 状态 :"+status+" ,错误 :"+err;
            }
        } catch (IOException e) {
            log.error("get lexmark printer status error", e);
            return "error ,"+e.getMessage() ;
        }
    }

    @Override
    public String getDeviceType() {
        return "LexmarkPrinter";
    }

    @Override
    public Health health() {
        try {
            String status = lexmarkPrinterAdapter.getPrinterStatus();
            return Health.up().withDetail("printer", ip).withDetail("status", status).build();
        } catch (IOException e) {
            return Health.down(e).withDetail("printer", ip).build();
        }
    }

    @Override
    public void shutdown() {
        log.info("Lexmark printer service shutdown");
    }
}
