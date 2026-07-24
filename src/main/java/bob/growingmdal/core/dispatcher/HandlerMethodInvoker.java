package bob.growingmdal.core.dispatcher;

import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.exception.HardwareOperationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
public class HandlerMethodInvoker {

    private final ObjectMapper objectMapper;

    public HandlerMethodInvoker(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String invoke(HandlerMapping mapping, DeviceCommand command) {
        Object result;
        try {
            result = switch (mapping.binding()) {
                case NO_ARGS -> mapping.method().invoke(mapping.handler());
                case DEVICE_COMMAND -> mapping.method().invoke(mapping.handler(), command);
                case INT_ARG -> mapping.method().invoke(mapping.handler(), parseInt(command));
                case INT_INT_INT -> {
                    int[] values = parseInts(command);
                    yield mapping.method().invoke(mapping.handler(), values[0], values[1], values[2]);
                }
            };
        } catch (InvocationTargetException e) {
            throw new HardwareOperationException("Handler execution failed", e.getCause());
        } catch (IllegalAccessException e) {
            throw new HardwareOperationException("Handler access failed", e);
        }
        return serializeResult(result);
    }

    private int parseInt(DeviceCommand command) {
        String data = command.getTransferData();
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("transferData required for int argument");
        }
        String first = data.split("@")[0];
        return Integer.parseInt(first.trim());
    }

    private int[] parseInts(DeviceCommand command) {
        String data = command.getTransferData();
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("transferData required for int arguments");
        }
        String[] parts = data.split("@");
        if (parts.length < 3) {
            throw new IllegalArgumentException("transferData must contain 3 int values separated by @");
        }
        return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim())
        };
    }

    private String serializeResult(Object result) {
        if (result == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return result.toString();
        }
    }
}
