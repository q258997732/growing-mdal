package bob.growingmdal.camera;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CameraMessageRouterTest {

    @Test
    void shouldRouteTextMessageToResponseMatcher() {
        CameraMessageRouter router = new CameraMessageRouter();
        AtomicReference<String> received = new AtomicReference<>();
        CameraResponseMatcher matcher = new CameraResponseMatcher() {
            @Override
            public void onMessage(String message) {
                received.set(message);
            }
        };
        router.setResponseMatcher(matcher);

        router.onTextMessage("OpenDevice#1");

        assertThat(received.get()).isEqualTo("OpenDevice#1");
    }

    @Test
    void shouldDropOldMessagesWhenQueueFull() {
        CameraMessageRouter router = new CameraMessageRouter();
        for (int i = 0; i < 1005; i++) {
            router.onTextMessage("msg" + i);
        }

        assertThat(router.getTextQueueSize()).isEqualTo(1000);
        // 最旧的消息应该被丢弃
        assertThat(router.getTextQueue().contains("msg0")).isFalse();
        assertThat(router.getTextQueue().contains("msg4")).isFalse();
        assertThat(router.getTextQueue().contains("msg1004")).isTrue();
    }

    @Test
    void shouldRouteBinaryMessageToBinaryQueue() {
        CameraMessageRouter router = new CameraMessageRouter();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3});

        router.onBinaryMessage(buffer);

        assertThat(router.getBinaryQueueSize()).isEqualTo(1);
    }

    @Test
    void shouldDrainMatchingMessages() {
        CameraMessageRouter router = new CameraMessageRouter();
        router.onTextMessage("OpenDevice#1");
        router.onTextMessage("CloseDevice#0");
        router.onTextMessage("OpenDevice#2");

        var matched = router.drainMatching(msg -> msg.startsWith("OpenDevice"));

        assertThat(matched).hasSize(2);
        assertThat(router.getTextQueueSize()).isEqualTo(1);
    }
}
