package bob.growingmdal.camera;

import bob.growingmdal.entity.response.NantianCameraResponse;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CameraResponseMatcherTest {

    private final CameraResponseMatcher matcher = new CameraResponseMatcher();

    @Test
    void shouldMatchByExactCommand() throws Exception {
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);
        when(session.isOpen()).thenReturn(true);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
                matcher.onMessage("OpenDevice#1#data");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NantianCameraResponse response = matcher.sendMessageGetResponse("OpenDevice@2", 2, session);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).contains("OpenDevice");
    }

    @Test
    void shouldNotMatchBySubstring() throws Exception {
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);
        when(session.isOpen()).thenReturn(true);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
                matcher.onMessage("OpenHideDevice#1#data");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NantianCameraResponse response = matcher.sendMessageGetResponse("OpenDevice@2", 2, session);

        assertThat(response.getCode()).isEqualTo(500);
        assertThat(response.getMessage()).contains("Internal Server Error");
    }

    @Test
    void shouldTimeoutWhenResponseMissing() {
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);
        when(session.isOpen()).thenReturn(true);

        NantianCameraResponse response = matcher.sendMessageGetResponse("OpenDevice@2", 1, session);

        assertThat(response.getCode()).isEqualTo(500);
    }
}
