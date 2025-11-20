package bob.growingmdal.service;

import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.command.HardwareCommandHandler;
import bob.growingmdal.core.exception.HardwareOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CommandDispatcherService {
    private final List<HardwareCommandHandler> handlers;

    @Autowired
    public CommandDispatcherService(List<HardwareCommandHandler> handlers) {
        this.handlers = handlers;
        log.info("Loaded {} command handlers", handlers.size());
    }

    public Object dispatch(DeviceCommand command) {
        Object result;
        log.debug("Dispatching command: device={}, cmd={}",
                command.getDeviceType(), command.getProcessCommand());

        // 获取支持该命令的处理器
        Optional<HardwareCommandHandler> handler = handlers.stream()
                .filter(h -> h.supports(command))
                .findFirst();
        log.debug("handler stream count:{} ", handler.stream().count());

        // 没找到相应处理器，抛出异常
        if (handler.isEmpty()) {
            throw new UnsupportedOperationException(
                    "No handler for: " + command.getDeviceType() + ":" + command.getProcessCommand());
        }

        try {
            result = handler.get().handle(command);
        } catch (Exception e) {
            throw new HardwareOperationException(
                    "Handler execution failed - " + e.getMessage(), e);
        }
        return result;
    }
}