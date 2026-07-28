package bob.growingmdal.core.dispatcher;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.command.HardwareCommandHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CommandRegistry {

    private final Map<String, HandlerMapping> mappings = new HashMap<>();

    public CommandRegistry(List<HardwareCommandHandler> handlers) {
        for (HardwareCommandHandler handler : handlers) {
            for (Method method : handler.getClass().getDeclaredMethods()) {
                DeviceOperation op = method.getAnnotation(DeviceOperation.class);
                if (op == null) {
                    continue;
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                String key = buildKey(op.DeviceType(), op.ProcessCommand());
                if (mappings.containsKey(key)) {
                    throw new IllegalStateException("Duplicate device operation: " + key);
                }
                mappings.put(key, new HandlerMapping(handler, method, resolveBinding(method), op.streaming(), op.readOnly()));
            }
        }
    }

    public Optional<HandlerMapping> resolve(DeviceCommand command) {
        return Optional.ofNullable(mappings.get(buildKey(command.getDeviceType(), command.getProcessCommand())));
    }

    public int size() {
        return mappings.size();
    }

    private String buildKey(String deviceType, String processCommand) {
        return deviceType + ":" + processCommand;
    }

    private ParameterBinding resolveBinding(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        return switch (paramTypes.length) {
            case 0 -> ParameterBinding.NO_ARGS;
            case 1 -> resolveSingleParameterBinding(paramTypes[0]);
            case 3 -> resolveThreeParameterBinding(paramTypes);
            default -> throw new IllegalStateException(
                    "Unsupported parameter count " + paramTypes.length + " for method " + method.getName());
        };
    }

    private ParameterBinding resolveSingleParameterBinding(Class<?> paramType) {
        if (paramType == DeviceCommand.class) {
            return ParameterBinding.DEVICE_COMMAND;
        }
        if (paramType == int.class || paramType == Integer.class) {
            return ParameterBinding.INT_ARG;
        }
        throw new IllegalStateException("Unsupported single parameter type: " + paramType);
    }

    private ParameterBinding resolveThreeParameterBinding(Class<?>[] paramTypes) {
        boolean allInt = true;
        for (Class<?> paramType : paramTypes) {
            if (paramType != int.class && paramType != Integer.class) {
                allInt = false;
                break;
            }
        }
        if (allInt) {
            return ParameterBinding.INT_INT_INT;
        }
        throw new IllegalStateException("Unsupported three parameter types");
    }
}
