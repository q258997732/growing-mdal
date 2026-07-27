package bob.growingmdal.validation;

import bob.growingmdal.core.command.DeviceCommand;

public final class CommandValidator {

    private static final int MAX_TRANSFER_DATA_LENGTH = 100_000;

    private CommandValidator() {
    }

    public static void validate(DeviceCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is null");
        }
        if (isBlank(command.getDeviceType())) {
            throw new IllegalArgumentException("deviceType is required");
        }
        if (isBlank(command.getProcessCommand())) {
            throw new IllegalArgumentException("processCommand is required");
        }
        String transferData = command.getTransferData();
        if (transferData != null && transferData.length() > MAX_TRANSFER_DATA_LENGTH) {
            throw new IllegalArgumentException("transferData exceeds maximum length of " + MAX_TRANSFER_DATA_LENGTH);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
