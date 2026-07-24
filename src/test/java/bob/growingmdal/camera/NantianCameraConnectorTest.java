package bob.growingmdal.camera;

import bob.growingmdal.hardware.ProtocolType;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NantianCameraConnectorTest {

    @Test
    void shouldExposeProtocolType() {
        CameraLifecycleManager lifecycleManager = mock(CameraLifecycleManager.class);
        NantianCameraConnector connector = new NantianCameraConnector(lifecycleManager);

        assertThat(connector.protocol()).isEqualTo(ProtocolType.WEBSOCKET_CLIENT);
    }

    @Test
    void shouldReportConnectionState() {
        CameraLifecycleManager lifecycleManager = mock(CameraLifecycleManager.class);
        when(lifecycleManager.isConnected()).thenReturn(true);
        Session session = mock(Session.class);
        when(lifecycleManager.getSession()).thenReturn(session);
        NantianCameraConnector connector = new NantianCameraConnector(lifecycleManager);

        assertThat(connector.isConnected()).isTrue();
        assertThat(connector.getUnderlying()).isSameAs(session);
    }
}
