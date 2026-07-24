package bob.growingmdal.service;

import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.CommandRegistry;
import bob.growingmdal.core.dispatcher.HandlerMapping;
import bob.growingmdal.core.dispatcher.HandlerMethodInvoker;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandDispatcherServiceTest {

    private final CommandRegistry registry = mock(CommandRegistry.class);
    private final HandlerMethodInvoker invoker = mock(HandlerMethodInvoker.class);
    private final CommandDispatcherService dispatcher = new CommandDispatcherService(registry, invoker);

    @Test
    void shouldDispatchRegisteredCommand() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Printer");
        command.setProcessCommand("PrintLocalPDF");

        when(registry.resolve(any())).thenReturn(Optional.of(mock(HandlerMapping.class)));
        when(invoker.invoke(any(), any())).thenReturn("ok");

        String result = dispatcher.dispatch(command);

        assertThat(result).isEqualTo("ok");
    }

    @Test
    void shouldThrowForUnsupportedCommand() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Unknown");
        command.setProcessCommand("Unknown");

        when(registry.resolve(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dispatcher.dispatch(command))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("No handler for");
    }
}
