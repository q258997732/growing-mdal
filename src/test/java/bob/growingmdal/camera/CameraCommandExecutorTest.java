package bob.growingmdal.camera;

import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.entity.response.NantianCameraResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CameraCommandExecutorTest {

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final CameraResponseMatcher matcher = mock(CameraResponseMatcher.class);
    private final CameraMessageRouter router = mock(CameraMessageRouter.class);
    private final CameraLifecycleManager lifecycleManager = mock(CameraLifecycleManager.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CameraCommandExecutor executor = new CameraCommandExecutor(
            publisher, matcher, router, lifecycleManager, objectMapper);

    @Test
    void shouldSupportCameraCommands() {
        DeviceCommand command = new DeviceCommand();
        command.setDeviceType("Camera");
        assertThat(executor.supports(command)).isTrue();
    }

    @Test
    void shouldReturnUnavailableWhenDisabled() {
        when(lifecycleManager.isEnabled()).thenReturn(false);

        NantianCameraResponse response = executor.openDevice(1);

        assertThat(response.getCode()).isEqualTo(500);
        assertThat(response.getData()).contains("disabled");
    }

    @Test
    void shouldStopGetFaceTempl() {
        assertThat(executor.stopGetFaceTempl()).isEqualTo("StopGetFaceTempl success");
    }

    @Test
    void shouldStopGetVideo() {
        assertThat(executor.stopGetVideo()).isEqualTo("Stop Get Video");
    }

    @Test
    void shouldStopFaceDetect() {
        assertThat(executor.stopFaceDetect()).isEqualTo("stop face detect success");
    }

    @Test
    void shouldExecuteSimpleCommandsWhenEnabled() {
        when(lifecycleManager.isEnabled()).thenReturn(true);
        when(lifecycleManager.getSession()).thenReturn(mock(jakarta.websocket.Session.class));
        when(matcher.sendMessageGetResponse(anyString(), anyInt(), any()))
                .thenReturn(new NantianCameraResponse(200, "ok", ""));

        assertThat(executor.openDevice(1).getCode()).isEqualTo(200);
        assertThat(executor.openHideDevice(1).getCode()).isEqualTo(200);
        assertThat(executor.openHideVideo().getCode()).isEqualTo(200);
        assertThat(executor.openVideo().getCode()).isEqualTo(200);
        assertThat(executor.closeDevice().getCode()).isEqualTo(200);
        assertThat(executor.closeHideDevice().getCode()).isEqualTo(200);
        assertThat(executor.closeVideo().getCode()).isEqualTo(200);
        assertThat(executor.closeHideVideo().getCode()).isEqualTo(200);
        assertThat(executor.rotateRight().getCode()).isEqualTo(200);
        assertThat(executor.rotateLeft().getCode()).isEqualTo(200);
        assertThat(executor.rotateHideRight().getCode()).isEqualTo(200);
        assertThat(executor.rotateHideLeft().getCode()).isEqualTo(200);
        assertThat(executor.deinitFaceMgr().getCode()).isEqualTo(200);
        assertThat(executor.initFaceMgr().getCode()).isEqualTo(200);
        assertThat(executor.unFaceDetect().getCode()).isEqualTo(200);
        assertThat(executor.stopGetFace().getCode()).isEqualTo(200);
    }
}
