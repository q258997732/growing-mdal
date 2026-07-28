package bob.growingmdal.core.dispatcher;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.command.HardwareCommandHandler;
import bob.growingmdal.core.exception.HardwareOperationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandlerMethodInvokerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HandlerMethodInvoker invoker = new HandlerMethodInvoker(objectMapper);
    private final TestHandler handler = new TestHandler();

    @Test
    void shouldInvokeNoArgMethod() throws NoSuchMethodException {
        Method method = TestHandler.class.getMethod("noArg");
        HandlerMapping mapping = new HandlerMapping(handler, method, ParameterBinding.NO_ARGS, false, false);
        DeviceCommand command = new DeviceCommand();

        String result = invoker.invoke(mapping, command);

        assertThat(result).isEqualTo("\"ok\"");
    }

    @Test
    void shouldInvokeDeviceCommandMethod() throws NoSuchMethodException {
        Method method = TestHandler.class.getMethod("withCommand", DeviceCommand.class);
        HandlerMapping mapping = new HandlerMapping(handler, method, ParameterBinding.DEVICE_COMMAND, false, false);
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("hello");

        String result = invoker.invoke(mapping, command);

        assertThat(result).isEqualTo("\"hello\"");
    }

    @Test
    void shouldInvokeIntArgMethod() throws NoSuchMethodException {
        Method method = TestHandler.class.getMethod("withInt", int.class);
        HandlerMapping mapping = new HandlerMapping(handler, method, ParameterBinding.INT_ARG, false, false);
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("42");

        String result = invoker.invoke(mapping, command);

        assertThat(result).isEqualTo("\"42\"");
    }

    @Test
    void shouldInvokeIntIntIntMethod() throws NoSuchMethodException {
        Method method = TestHandler.class.getMethod("withInts", int.class, int.class, int.class);
        HandlerMapping mapping = new HandlerMapping(handler, method, ParameterBinding.INT_INT_INT, false, false);
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("1@2@3");

        String result = invoker.invoke(mapping, command);

        assertThat(result).isEqualTo("\"1,2,3\"");
    }

    @Test
    void shouldUnwrapInvocationTargetException() throws NoSuchMethodException {
        Method method = TestHandler.class.getMethod("fail");
        HandlerMapping mapping = new HandlerMapping(handler, method, ParameterBinding.NO_ARGS, false, false);
        DeviceCommand command = new DeviceCommand();

        assertThatThrownBy(() -> invoker.invoke(mapping, command))
                .isInstanceOf(HardwareOperationException.class)
                .hasMessage("Handler execution failed")
                .hasCauseInstanceOf(IllegalStateException.class)
                .satisfies(ex -> assertThat(ex.getCause()).hasMessageContaining("boom"));
    }

    static class TestHandler implements HardwareCommandHandler {
        @Override
        public boolean supports(DeviceCommand command) {
            return true;
        }

        @DeviceOperation(DeviceType = "Test", ProcessCommand = "NoArg")
        public String noArg() {
            return "ok";
        }

        @DeviceOperation(DeviceType = "Test", ProcessCommand = "WithCommand")
        public String withCommand(DeviceCommand command) {
            return command.getTransferData();
        }

        @DeviceOperation(DeviceType = "Test", ProcessCommand = "WithInt")
        public String withInt(int value) {
            return String.valueOf(value);
        }

        @DeviceOperation(DeviceType = "Test", ProcessCommand = "WithInts")
        public String withInts(int a, int b, int c) {
            return a + "," + b + "," + c;
        }

        @DeviceOperation(DeviceType = "Test", ProcessCommand = "Fail")
        public String fail() {
            throw new IllegalStateException("boom");
        }
    }
}
