package bob.growingmdal.service;

import bob.growingmdal.config.AdapterProperties;
import bob.growingmdal.connector.RpaHttpConnector;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.dto.rpa.KAgentBean;
import bob.growingmdal.dto.rpa.KAgentThreadBean;
import bob.growingmdal.dto.rpa.KFlowBean;
import bob.growingmdal.dto.rpa.KSxfAgentBean;
import bob.growingmdal.dto.rpa.RpaRequestBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RpaClientServiceTest {

    private static final String RPA_USER = "testUser";
    private static final String RPA_PASS = "testPass";
    private static final int RPA_TIMEOUT = 5000;
    private static final String RPA_HOST = "192.168.107.100";
    private static final int RPA_PORT = 80;

    @Mock
    private RpaHttpConnector rpaHttpConnector;

    @Mock
    private AdapterProperties adapterProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RpaClientService service;

    @BeforeEach
    void setUp() {
        service = new RpaClientService(rpaHttpConnector, objectMapper, adapterProperties);
        lenient().when(adapterProperties.getRpaUser()).thenReturn(RPA_USER);
        lenient().when(adapterProperties.getRpaPass()).thenReturn(RPA_PASS);
        lenient().when(adapterProperties.getRpaCallFunTimeout()).thenReturn(RPA_TIMEOUT);
    }

    @Test
    void shouldReturnDeviceTypeRpa() {
        assertThat(service.getDeviceType()).isEqualTo("Rpa");
    }

    @Test
    void shouldSupportRpaCommands() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Rpa");
        assertThat(service.supports(command)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCallComponentMissingFields() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"script\":\"test\"}");

        boolean result = service.callComponent(command);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAddDataQueueMissingFields() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"flow\":\"test\"}");

        boolean result = service.addDataQueue(command);

        assertThat(result).isFalse();
    }

    @Test
    void shouldCallComponentWithAllFields() {
        when(rpaHttpConnector.sendRequest(any())).thenReturn(Collections.emptyList());
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"script\":\"test.vcl\",\"params\":\"{}\",\"agentIp\":\"192.168.1.1\"}");

        service.callComponent(command);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RpaRequestBean>> captor = ArgumentCaptor.forClass(List.class);
        verify(rpaHttpConnector).sendRequest(captor.capture());
        List<RpaRequestBean> request = captor.getValue();
        assertThat(request).hasSize(11);
        assertThat(findByName(request, "AppName").getValue()).isEqualTo(RPA_USER);
        assertThat(findByName(request, "AppPass").getValue()).isEqualTo(RPA_PASS);
        assertThat(findByName(request, "IP").getValue()).isEqualTo("192.168.1.1");
        assertThat(findByName(request, "VCLName").getValue()).isEqualTo("test.vcl");
        assertThat(findByName(request, "Params").getValue()).isEqualTo("{}");
        assertThat(findByName(request, "VCLSynchro").getValue()).isEqualTo(true);
        assertThat(findByName(request, "VCLBlockInput").getValue()).isEqualTo(false);
        assertThat(findByName(request, "TimeOut").getValue()).isEqualTo(RPA_TIMEOUT);
    }

    @Test
    void shouldAddDataQueueWithAllFields() {
        when(rpaHttpConnector.sendRequest(any())).thenReturn(Collections.emptyList());
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"flowType\":\"name\",\"flow\":\"FlowA\",\"data\":\"{}\",\"agentIp\":\"192.168.1.1\",\"level\":1}");

        service.addDataQueue(command);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RpaRequestBean>> captor = ArgumentCaptor.forClass(List.class);
        verify(rpaHttpConnector).sendRequest(captor.capture());
        List<RpaRequestBean> request = captor.getValue();
        assertThat(request).hasSize(12);
        assertThat(findByName(request, "Robot").getValue()).isEqualTo("192.168.1.1");
        assertThat(findByName(request, "Data").getValue()).isEqualTo("{}");
        assertThat(findByName(request, "FlowName").getValue()).isEqualTo("FlowA");
        assertThat(findByName(request, "Level").getValue()).isEqualTo(1);
        assertThat(findByName(request, "IdleRobot").getValue()).isEqualTo(true);
        assertThat(findByName(request, "IsThird").getValue()).isEqualTo(true);
        assertThat(findByName(request, "IsQueue").getValue()).isEqualTo(true);
    }

    @Test
    void shouldAddDataQueueWithFlowId() {
        when(rpaHttpConnector.sendRequest(any())).thenReturn(Collections.emptyList());
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("{\"flowType\":\"id\",\"flow\":\"123\",\"data\":\"{}\",\"agentIp\":\"192.168.1.1\",\"level\":2}");

        service.addDataQueue(command);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RpaRequestBean>> captor = ArgumentCaptor.forClass(List.class);
        verify(rpaHttpConnector).sendRequest(captor.capture());
        assertThat(findByName(captor.getValue(), "FlowID").getValue()).isEqualTo("123");
    }

    @Test
    void shouldGetAgentList() {
        when(rpaHttpConnector.sendRequest(any())).thenReturn(Collections.emptyList());

        List<KAgentBean> result = service.getAgentList(new DeviceCommand());

        assertThat(result).isEmpty();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RpaRequestBean>> captor = ArgumentCaptor.forClass(List.class);
        verify(rpaHttpConnector).sendRequest(captor.capture());
        List<RpaRequestBean> request = captor.getValue();
        assertThat(request).hasSize(4);
        assertThat(findByName(request, "AppName").getValue()).isEqualTo(RPA_USER);
        assertThat(findByName(request, "AppPass").getValue()).isEqualTo(RPA_PASS);
    }

    @Test
    void shouldGetFlowList() {
        when(rpaHttpConnector.sendRequest(any())).thenReturn(Collections.emptyList());

        List<KFlowBean> result = service.getFlowList(new DeviceCommand());

        assertThat(result).isEmpty();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RpaRequestBean>> captor = ArgumentCaptor.forClass(List.class);
        verify(rpaHttpConnector).sendRequest(captor.capture());
        List<RpaRequestBean> request = captor.getValue();
        assertThat(request).hasSize(5);
        assertThat(findByName(request, "ConsumerID").getValue()).isEqualTo("");
    }

    @Test
    void shouldGetAgentThreadList() {
        when(rpaHttpConnector.sendRequest(any())).thenReturn(Collections.emptyList());

        Map<String, KAgentThreadBean> result = service.getAgentThreadList(new DeviceCommand());

        assertThat(result).isEmpty();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RpaRequestBean>> captor = ArgumentCaptor.forClass(List.class);
        verify(rpaHttpConnector).sendRequest(captor.capture());
        assertThat(captor.getValue()).hasSize(4);
    }

    @Test
    void shouldGetSxfAgentFlowQuery() {
        when(rpaHttpConnector.sendRequest(any())).thenReturn(Collections.emptyList());

        List<KSxfAgentBean> result = service.getSxfAgentFlowQuery(new DeviceCommand());

        assertThat(result).isEmpty();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RpaRequestBean>> captor = ArgumentCaptor.forClass(List.class);
        verify(rpaHttpConnector).sendRequest(captor.capture());
        List<RpaRequestBean> request = captor.getValue();
        assertThat(request).hasSize(6);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        assertThat(findByName(request, "BDate").getValue()).isEqualTo(today);
        assertThat(findByName(request, "EDate").getValue()).isEqualTo(today);
    }

    @Test
    void shouldReturnFalseWhenTransferDataIsMissing() {
        DeviceCommand command = new DeviceCommand();

        assertThat(service.callComponent(command)).isFalse();
        assertThat(service.addDataQueue(command)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTransferDataIsInvalidJson() {
        DeviceCommand command = new DeviceCommand();
        command.setTransferData("not-json");

        assertThat(service.callComponent(command)).isFalse();
        assertThat(service.addDataQueue(command)).isFalse();
    }

    @Test
    void shouldReportHealthDownWhenConnectorUnreachable() {
        lenient().when(adapterProperties.getRpaHost()).thenReturn(RPA_HOST);
        lenient().when(adapterProperties.getRpaPort()).thenReturn(RPA_PORT);
        when(rpaHttpConnector.isReachable()).thenReturn(false);

        assertThat(service.health().getStatus().getCode()).isEqualTo("DOWN");
    }

    @Test
    void shouldReportHealthUpWhenConnectorReachable() {
        lenient().when(adapterProperties.getRpaHost()).thenReturn(RPA_HOST);
        lenient().when(adapterProperties.getRpaPort()).thenReturn(RPA_PORT);
        when(rpaHttpConnector.isReachable()).thenReturn(true);

        assertThat(service.health().getStatus().getCode()).isEqualTo("UP");
    }

    private RpaRequestBean findByName(List<RpaRequestBean> request, String name) {
        return request.stream()
                .filter(bean -> name.equals(bean.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing bean with name: " + name));
    }
}
