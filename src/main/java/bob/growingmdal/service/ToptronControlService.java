package bob.growingmdal.service;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.config.AdapterProperties;
import bob.growingmdal.connector.ToptronTcpConnector;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.hardware.LifecycleManaged;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Toptron 中控设备服务。
 */
@Slf4j
@Service
public class ToptronControlService extends AnnotationDrivenHandler implements LifecycleManaged {

    public static final String DEVICE_TYPE = "Toptron";

    private final ToptronTcpConnector toptronTcpConnector;
    private final ObjectMapper objectMapper;
    private final AdapterProperties adapterProperties;

    public ToptronControlService(ToptronTcpConnector toptronTcpConnector,
                                 ObjectMapper objectMapper,
                                 AdapterProperties adapterProperties) {
        this.toptronTcpConnector = toptronTcpConnector;
        this.objectMapper = objectMapper;
        this.adapterProperties = adapterProperties;
    }

    @Override
    public String getDeviceType() {
        return DEVICE_TYPE;
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "PowerControl")
    public boolean powerControl(DeviceCommand command) {
        JsonNode transferData = parseTransferData(command.getTransferData());
        if (transferData == null) {
            return false;
        }
        JsonNode gatePositionNode = transferData.get("gatePosition");
        JsonNode turnonNode = transferData.get("turnon");
        if (gatePositionNode == null || turnonNode == null) {
            log.error("Toptron PowerControl requires gatePosition and turnon");
            return false;
        }
        return toptronTcpConnector.sendPowerControl(gatePositionNode.asText(), turnonNode.asBoolean());
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "RawControl")
    public boolean rawControl(DeviceCommand command) {
        JsonNode transferData = parseTransferData(command.getTransferData());
        if (transferData == null) {
            return false;
        }
        JsonNode controlMsgNode = transferData.get("controlMsg");
        if (controlMsgNode == null) {
            log.error("Toptron RawControl requires controlMsg");
            return false;
        }
        return toptronTcpConnector.sendRawMessage(controlMsgNode.asText());
    }

    private JsonNode parseTransferData(String transferData) {
        if (transferData == null || transferData.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(transferData);
        } catch (IOException e) {
            log.error("Failed to parse Toptron transferData: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void initialize() {
        log.info("Toptron control service initialized, host={}, port={}",
                adapterProperties.getToptronHost(), adapterProperties.getToptronPort());
    }

    @Override
    public Health health() {
        boolean reachable = toptronTcpConnector.isReachable();
        if (reachable) {
            return Health.up()
                    .withDetail("host", adapterProperties.getToptronHost())
                    .withDetail("port", adapterProperties.getToptronPort())
                    .build();
        }
        return Health.down()
                .withDetail("host", adapterProperties.getToptronHost())
                .withDetail("port", adapterProperties.getToptronPort())
                .withDetail("reason", "Toptron host not reachable")
                .build();
    }

    @Override
    public void shutdown() {
        log.info("Toptron control service shutdown");
    }
}
