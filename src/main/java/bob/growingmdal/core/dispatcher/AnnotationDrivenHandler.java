package bob.growingmdal.core.dispatcher;

import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.command.HardwareCommandHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AnnotationDrivenHandler implements HardwareCommandHandler {

    public abstract String getDeviceType();

    @Override
    public boolean supports(DeviceCommand command) {
        return getDeviceType().equals(command.getDeviceType());
    }
}
