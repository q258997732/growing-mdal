package bob.growingmdal.controller;

import bob.growingmdal.config.RestEndpointProperties;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.dto.HardwareCommandRequest;
import bob.growingmdal.entity.response.ResponseBean;
import bob.growingmdal.service.CommandDispatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HardwareCommandRestControllerTest {

    private final CommandDispatcherService dispatcher = mock(CommandDispatcherService.class);
    private final RestEndpointProperties restProperties = new RestEndpointProperties();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HardwareCommandRestController controller =
            new HardwareCommandRestController(dispatcher, restProperties, objectMapper);

    @Test
    void shouldReturn200ForReadOnlyCommand() {
        restProperties.setEnabled(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isReadOnlyCommand(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isStreamingCommand(any(DeviceCommand.class))).thenReturn(false);
        when(dispatcher.dispatch(any(DeviceCommand.class))).thenReturn("\"ok\"");

        ResponseEntity<ResponseBean<?>> response =
                controller.executeReadOnlyCommand("IDCard", "cardExists", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
    }

    @Test
    void shouldReturn202ForStreamingCommand() {
        restProperties.setEnabled(true);
        restProperties.setCamera(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isReadOnlyCommand(any(DeviceCommand.class))).thenReturn(false);
        when(dispatcher.isStreamingCommand(any(DeviceCommand.class))).thenReturn(true);

        ResponseEntity<ResponseBean<?>> response =
                controller.executeWriteCommand("Camera", "StartGetVideo", new HardwareCommandRequest(null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(202);
    }

    @Test
    void shouldReturn404ForUnknownCommand() {
        restProperties.setEnabled(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(false);

        ResponseEntity<ResponseBean<?>> response =
                controller.executeReadOnlyCommand("IDCard", "unknown", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn503WhenRestDisabled() {
        restProperties.setEnabled(false);

        ResponseEntity<ResponseBean<?>> response =
                controller.executeReadOnlyCommand("IDCard", "cardExists", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldReturn503WhenDeviceDisabled() {
        restProperties.setEnabled(true);
        restProperties.setIdcard(false);

        ResponseEntity<ResponseBean<?>> response =
                controller.executeReadOnlyCommand("IDCard", "cardExists", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldReturn202ForStreamingGetCommand() {
        restProperties.setEnabled(true);
        restProperties.setCamera(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isReadOnlyCommand(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isStreamingCommand(any(DeviceCommand.class))).thenReturn(true);

        ResponseEntity<ResponseBean<?>> response =
                controller.executeReadOnlyCommand("Camera", "StartGetVideo", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void shouldReturnRawStringWhenResultIsNotJson() {
        restProperties.setEnabled(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isReadOnlyCommand(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isStreamingCommand(any(DeviceCommand.class))).thenReturn(false);
        when(dispatcher.dispatch(any(DeviceCommand.class))).thenReturn("plain text");

        ResponseEntity<ResponseBean<?>> response =
                controller.executeReadOnlyCommand("LexmarkPrinter", "getLexmarkStatus", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo("plain text");
    }

    @Test
    void shouldReturn500WhenDispatchFails() {
        restProperties.setEnabled(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isReadOnlyCommand(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isStreamingCommand(any(DeviceCommand.class))).thenReturn(false);
        when(dispatcher.dispatch(any(DeviceCommand.class))).thenThrow(new RuntimeException("boom"));

        ResponseEntity<ResponseBean<?>> response =
                controller.executeReadOnlyCommand("IDCard", "cardExists", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("boom");
    }

    @Test
    void shouldPassTransferDataFromQueryParam() {
        restProperties.setEnabled(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isReadOnlyCommand(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isStreamingCommand(any(DeviceCommand.class))).thenReturn(false);
        when(dispatcher.dispatch(any(DeviceCommand.class))).thenReturn("\"ok\"");

        controller.executeReadOnlyCommand("IDCard", "cardExists", "{\"key\":\"value\"}");

        ArgumentCaptor<DeviceCommand> captor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertThat(captor.getValue().getTransferData()).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void shouldPassTransferDataFromBody() {
        restProperties.setEnabled(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isReadOnlyCommand(any(DeviceCommand.class))).thenReturn(false);
        when(dispatcher.isStreamingCommand(any(DeviceCommand.class))).thenReturn(false);
        when(dispatcher.dispatch(any(DeviceCommand.class))).thenReturn("\"ok\"");

        HardwareCommandRequest request = new HardwareCommandRequest("{\"gatePosition\":1}");
        controller.executeWriteCommand("Toptron", "PowerControl", request);

        ArgumentCaptor<DeviceCommand> captor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertThat(captor.getValue().getTransferData()).isEqualTo("{\"gatePosition\":1}");
    }

    @Test
    void shouldReturn400WhenHttpMethodMismatches() {
        restProperties.setEnabled(true);
        when(dispatcher.isRegistered(any(DeviceCommand.class))).thenReturn(true);
        when(dispatcher.isReadOnlyCommand(any(DeviceCommand.class))).thenReturn(false);

        ResponseEntity<ResponseBean<?>> response =
                controller.executeReadOnlyCommand("Toptron", "PowerControl", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
