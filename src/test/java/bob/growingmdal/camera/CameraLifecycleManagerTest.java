package bob.growingmdal.camera;

import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CameraLifecycleManagerTest {

    private final CameraLifecycleManager lifecycleManager = new CameraLifecycleManager();

    @Test
    void shouldBeDisabledByDefault() {
        assertThat(lifecycleManager.isEnabled()).isFalse();
    }

    @Test
    void shouldNotConnectWhenDisabled() {
        lifecycleManager.setEndpoint(this);
        assertThat(lifecycleManager.connect()).isFalse();
    }

    @Test
    void shouldNotConnectWithoutEndpoint() {
        assertThat(lifecycleManager.connect()).isFalse();
    }

    @Test
    void shouldTrackConnectionState() {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);

        lifecycleManager.onOpen(session);

        assertThat(lifecycleManager.isConnected()).isTrue();
        assertThat(lifecycleManager.getSession()).isEqualTo(session);
    }

    @Test
    void shouldMarkDisconnectedOnClose() {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);

        lifecycleManager.onOpen(session);
        lifecycleManager.onClose();

        assertThat(lifecycleManager.isConnected()).isFalse();
    }

    @Test
    void shouldShutdownWithoutError() {
        lifecycleManager.shutdown();
    }
}
