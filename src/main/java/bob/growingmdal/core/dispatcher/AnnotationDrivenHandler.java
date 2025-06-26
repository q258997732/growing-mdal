package bob.growingmdal.core.dispatcher;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.command.HardwareCommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AnnotationDrivenHandler implements HardwareCommandHandler {
    private final Map<String, Method> operationMethods = new ConcurrentHashMap<>();
    private final Set<String> executingMethods = ConcurrentHashMap.newKeySet();
    private volatile boolean initialized = false;

    // 初始化方法路由表
    private synchronized void initMethodRegistry() {
        if (initialized) return;

        for (Method method : this.getClass().getDeclaredMethods()) {
            DeviceOperation annotation = method.getAnnotation(DeviceOperation.class);
            if (annotation != null) {
                String key = buildOperationKey(annotation.DeviceType(), annotation.ProcessCommand());
                method.setAccessible(true); // 允许调用私有方法
                operationMethods.put(key, method);
            }
        }
        initialized = true;
    }

    // 构建方法查找键
    protected String buildOperationKey(String deviceType, String processCommand) {
        return deviceType + ":" + processCommand;
    }

    // parameterTypes方法参数toString
    protected String parameterTypesToString(Class<?>[] parameterTypes) {
        StringBuilder returnString = new StringBuilder();
        for (Class<?> parameterType : parameterTypes) {
            returnString.append(parameterType.getName()).append(" ");
        }
        return returnString.toString();
    }

    /**
     * 处理命令
     *
     * @param command 调用方法的参数
     */
    @Override
    public Object handle(DeviceCommand command) {
        log.debug("AnnotationDrivenHandler handle command: {}", command);
        if (!initialized) initMethodRegistry();

        String key = buildOperationKey(command.getDeviceType(), command.getProcessCommand());
        Method method = operationMethods.get(key);
        if (method == null) {
            throw new UnsupportedOperationException("unsupported operate: " + key);
        }

        // 获取方法参数
        Class<?>[] parameterTypes = method.getParameterTypes();

        Object result = null;
        // 判断是否需要传入参数
        try {
            if (parameterTypes.length == 0) {
                result = method.invoke(this);
            } else {
                result = method.invoke(this, command);
            }

            // 组合返回内容
            if (result == null) {
                command.setTransferData("");
            } else {
                command.setTransferData(result.toString());
            }
            command.setFunction("output");
            return command.toString();

        } catch (Exception e) {
            command.setTransferData("error :" + e.getMessage());
            throw new RuntimeException("need parameter: " + parameterTypesToString(parameterTypes) + "\n" + e);
        }

    }

}