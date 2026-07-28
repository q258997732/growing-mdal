package bob.growingmdal.core.dispatcher;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.command.HardwareCommandHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandRegistryTest {

    @Test
    void shouldRegisterHandlerMethodsOnInit() {
        TestHandler handler = new TestHandler();

        CommandRegistry registry = new CommandRegistry(List.of(handler));

        assertThat(registry.size()).isEqualTo(4);
    }

    @Test
    void shouldLookupMethodByKey() {
        TestHandler handler = new TestHandler();
        CommandRegistry registry = new CommandRegistry(List.of(handler));
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("TestDevice");
        command.setProcessCommand("NoArg");

        HandlerMapping mapping = registry.resolve(command).orElseThrow();

        assertThat(mapping.handler()).isSameAs(handler);
        assertThat(mapping.binding()).isEqualTo(ParameterBinding.NO_ARGS);
    }

    @Test
    void shouldThrowOnDuplicateDeviceOperation() {
        DuplicateHandler first = new DuplicateHandler();
        DuplicateHandler second = new DuplicateHandler();

        assertThatThrownBy(() -> new CommandRegistry(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate device operation: TestDevice:Duplicate");
    }

    @Test
    void shouldRejectPrivateAnnotatedMethods() {
        PrivateMethodHandler handler = new PrivateMethodHandler();

        CommandRegistry registry = new CommandRegistry(List.of(handler));

        assertThat(registry.size()).isZero();
    }

    @Test
    void shouldReadStreamingAndReadOnlyFlags() {
        MetadataHandler handler = new MetadataHandler();
        CommandRegistry registry = new CommandRegistry(List.of(handler));

        DeviceCommand streamCmd = new DeviceCommand();
        streamCmd.setDeviceType("MetaDevice");
        streamCmd.setProcessCommand("Stream");
        HandlerMapping streamMapping = registry.resolve(streamCmd).orElseThrow();
        assertThat(streamMapping.streaming()).isTrue();
        assertThat(streamMapping.readOnly()).isFalse();

        DeviceCommand readCmd = new DeviceCommand();
        readCmd.setDeviceType("MetaDevice");
        readCmd.setProcessCommand("Read");
        HandlerMapping readMapping = registry.resolve(readCmd).orElseThrow();
        assertThat(readMapping.streaming()).isFalse();
        assertThat(readMapping.readOnly()).isTrue();
    }

    static class MetadataHandler implements HardwareCommandHandler {
        @Override
        public boolean supports(DeviceCommand command) {
            return "MetaDevice".equals(command.getDeviceType());
        }

        @DeviceOperation(DeviceType = "MetaDevice", ProcessCommand = "Stream", streaming = true)
        public String stream() {
            return "stream";
        }

        @DeviceOperation(DeviceType = "MetaDevice", ProcessCommand = "Read", readOnly = true)
        public String read() {
            return "read";
        }
    }

    static class TestHandler implements HardwareCommandHandler {
        @Override
        public boolean supports(DeviceCommand command) {
            return "TestDevice".equals(command.getDeviceType());
        }

        @DeviceOperation(DeviceType = "TestDevice", ProcessCommand = "NoArg")
        public String noArg() {
            return "ok";
        }

        @DeviceOperation(DeviceType = "TestDevice", ProcessCommand = "WithCommand")
        public String withCommand(DeviceCommand command) {
            return command.getTransferData();
        }

        @DeviceOperation(DeviceType = "TestDevice", ProcessCommand = "WithInt")
        public String withInt(int value) {
            return String.valueOf(value);
        }

        @DeviceOperation(DeviceType = "TestDevice", ProcessCommand = "WithInts")
        public String withInts(int a, int b, int c) {
            return a + "," + b + "," + c;
        }
    }

    static class DuplicateHandler implements HardwareCommandHandler {
        @Override
        public boolean supports(DeviceCommand command) {
            return "TestDevice".equals(command.getDeviceType());
        }

        @DeviceOperation(DeviceType = "TestDevice", ProcessCommand = "Duplicate")
        public String duplicate() {
            return "duplicate";
        }
    }

    static class PrivateMethodHandler implements HardwareCommandHandler {
        @Override
        public boolean supports(DeviceCommand command) {
            return "PrivateDevice".equals(command.getDeviceType());
        }

        @DeviceOperation(DeviceType = "PrivateDevice", ProcessCommand = "Secret")
        private String secret() {
            return "secret";
        }
    }
}
