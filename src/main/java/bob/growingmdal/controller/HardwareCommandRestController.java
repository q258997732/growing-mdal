package bob.growingmdal.controller;

import bob.growingmdal.config.RestEndpointProperties;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.dto.HardwareCommandRequest;
import bob.growingmdal.entity.response.ErrorResponseBean;
import bob.growingmdal.entity.response.ResponseBean;
import bob.growingmdal.entity.response.SuccessResponseBean;
import bob.growingmdal.service.CommandDispatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

/**
 * 硬件命令 REST 入口。
 * <p>
 * 除需要持续推流的命令外，其他命令均提供 RESTful 调用方式。
 * 所有命令仍可通过 WebSocket `/hardware-ws` 调用。
 * <p>
 * 当前实现为同步调用：命令在 Tomcat 工作线程中直接执行。若调用 RPA、打印、DLL 等
 * 可能耗时数秒的操作，请合理设置客户端/代理超时，或改用 WebSocket。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hardware")
public class HardwareCommandRestController {

    private static final String STREAMING_HINT = "Stream command, use /hardware-ws to receive data.";

    private final CommandDispatcherService dispatcher;
    private final RestEndpointProperties restProperties;
    private final ObjectMapper objectMapper;

    public HardwareCommandRestController(CommandDispatcherService dispatcher,
                                         RestEndpointProperties restProperties,
                                         ObjectMapper objectMapper) {
        this.dispatcher = dispatcher;
        this.restProperties = restProperties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void logEnabledDevices() {
        if (!restProperties.isEnabled()) {
            log.info("REST hardware command endpoints are disabled globally");
            return;
        }
        String enabled = Stream.of("IDCard", "Printer", "LexmarkPrinter", "Toptron", "Rpa", "FileUpload", "Camera")
                .filter(restProperties::isDeviceEnabled)
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");
        log.info("REST hardware command endpoints enabled for device types: {}", enabled);
    }

    @GetMapping("/{deviceType}/commands/{processCommand}")
    public ResponseEntity<ResponseBean<?>> executeReadOnlyCommand(
            @PathVariable String deviceType,
            @PathVariable String processCommand,
            @RequestParam(required = false) String transferData) {
        return executeCommand(deviceType, processCommand, transferData, true);
    }

    @PostMapping("/{deviceType}/commands/{processCommand}")
    public ResponseEntity<ResponseBean<?>> executeWriteCommand(
            @PathVariable String deviceType,
            @PathVariable String processCommand,
            @RequestBody(required = false) HardwareCommandRequest body) {
        String transferData = body != null ? body.transferData() : null;
        return executeCommand(deviceType, processCommand, transferData, false);
    }

    private ResponseEntity<ResponseBean<?>> executeCommand(String deviceType,
                                                                  String processCommand,
                                                                  String transferData,
                                                                  boolean httpGet) {
        if (!restProperties.isEnabled()) {
            return serviceUnavailable("REST endpoints are disabled");
        }
        if (!restProperties.isDeviceEnabled(deviceType)) {
            return serviceUnavailable("REST endpoints for device type '" + deviceType + "' are disabled");
        }

        DeviceCommand command = new DeviceCommand();
        command.setFunction("OutPut");
        command.setDeviceType(deviceType);
        command.setProcessCommand(processCommand);
        command.setTransferData(transferData);

        if (!dispatcher.isRegistered(command)) {
            return notFound("No handler for " + deviceType + ":" + processCommand);
        }

        if (dispatcher.isReadOnlyCommand(command) != httpGet) {
            String expected = dispatcher.isReadOnlyCommand(command) ? "GET" : "POST";
            return badRequest("Command " + processCommand + " requires " + expected);
        }

        if (dispatcher.isStreamingCommand(command)) {
            return accepted(STREAMING_HINT);
        }

        try {
            String resultJson = dispatcher.dispatch(command);
            Object data = parseResult(resultJson);
            return ResponseEntity.ok(new SuccessResponseBean<>(data));
        } catch (Exception e) {
            log.warn("REST command failed: {}:{}", deviceType, processCommand, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseBean(e.getMessage()));
        }
    }

    private Object parseResult(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            log.debug("Failed to parse handler result as JSON, returning raw string");
            return json;
        }
    }

    private ResponseEntity<ResponseBean<?>> accepted(String message) {
        ResponseBean<String> response = new ResponseBean<>();
        response.setCode(HttpStatus.ACCEPTED.value());
        response.setMessage(message);
        response.setData(null);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    private ResponseEntity<ResponseBean<?>> serviceUnavailable(String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponseBean(message));
    }

    private ResponseEntity<ResponseBean<?>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseBean(message));
    }

    private ResponseEntity<ResponseBean<?>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseBean(message));
    }
}
