package bob.growingmdal.service;

import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.CommandRegistry;
import bob.growingmdal.core.dispatcher.HandlerMapping;
import bob.growingmdal.core.dispatcher.HandlerMethodInvoker;
import bob.growingmdal.validation.CommandValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommandDispatcherService {

    private final CommandRegistry registry;
    private final HandlerMethodInvoker invoker;

    public CommandDispatcherService(CommandRegistry registry, HandlerMethodInvoker invoker) {
        this.registry = registry;
        this.invoker = invoker;
        log.info("Command dispatcher initialized with {} mappings", registry.size());
    }

    public String dispatch(DeviceCommand command) {
        CommandValidator.validate(command);

        log.debug("Dispatching command: device={}, cmd={}",
                command.getDeviceType(), command.getProcessCommand());

        HandlerMapping mapping = resolveMapping(command);
        return invoker.invoke(mapping, command);
    }

    public boolean isStreamingCommand(DeviceCommand command) {
        return resolveMapping(command).streaming();
    }

    public boolean isReadOnlyCommand(DeviceCommand command) {
        return resolveMapping(command).readOnly();
    }

    public boolean isRegistered(DeviceCommand command) {
        return registry.resolve(command).isPresent();
    }

    private HandlerMapping resolveMapping(DeviceCommand command) {
        return registry.resolve(command)
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No handler for: " + command.getDeviceType() + ":" + command.getProcessCommand()));
    }
}
