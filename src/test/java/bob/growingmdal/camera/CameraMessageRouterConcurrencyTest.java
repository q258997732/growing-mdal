package bob.growingmdal.camera;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CameraMessageRouterConcurrencyTest {

    @Test
    void shouldNotLoseMessagesUnderConcurrentLoad() throws InterruptedException {
        CameraMessageRouter router = new CameraMessageRouter();
        int threads = 10;
        int messagesPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < messagesPerThread; j++) {
                        router.onTextMessage("msg-" + threadId + "-" + j);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertThat(doneLatch.await(30, TimeUnit.SECONDS)).isTrue();
        assertThat(router.getTextQueueSize()).isEqualTo(1000);
    }

    @Test
    void shouldMatchResponsesToCorrectRequests() throws Exception {
        CameraResponseMatcher matcher = new CameraResponseMatcher();
        CameraMessageRouter router = new CameraMessageRouter();
        router.setResponseMatcher(matcher);

        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);
        when(session.isOpen()).thenReturn(true);

        CountDownLatch responseLatch = new CountDownLatch(2);
        AtomicInteger openDeviceCode = new AtomicInteger();
        AtomicInteger closeDeviceCode = new AtomicInteger();

        new Thread(() -> {
            var resp = matcher.sendMessageGetResponse("OpenDevice@2", 2, session);
            openDeviceCode.set(resp.getCode());
            responseLatch.countDown();
        }).start();

        new Thread(() -> {
            var resp = matcher.sendMessageGetResponse("CloseDevice@2", 2, session);
            closeDeviceCode.set(resp.getCode());
            responseLatch.countDown();
        }).start();

        Thread.sleep(200);
        router.onTextMessage("CloseDevice#1#data");
        router.onTextMessage("OpenDevice#1#data");

        assertThat(responseLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(openDeviceCode.get()).isEqualTo(200);
        assertThat(closeDeviceCode.get()).isEqualTo(200);
    }
}
