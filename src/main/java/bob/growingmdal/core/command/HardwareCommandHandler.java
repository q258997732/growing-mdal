package bob.growingmdal.core.command;

public interface HardwareCommandHandler {
    boolean supports(DeviceCommand command);
    Object handle(DeviceCommand command);
}
