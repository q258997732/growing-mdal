package bob.growingmdal.entity;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSortedBufferQueueTest {

    @Test
    void shouldRemoveOnlyElementsInRange() throws InterruptedException {
        TimeSortedBufferQueue queue = new TimeSortedBufferQueue();
        Instant now = Instant.now();
        queue.add(ByteBuffer.wrap(new byte[]{1}));
        Thread.sleep(10);
        queue.add(ByteBuffer.wrap(new byte[]{2}));
        Thread.sleep(10);
        queue.add(ByteBuffer.wrap(new byte[]{3}));
        Thread.sleep(10);
        Instant end = Instant.now();

        PriorityBlockingQueue<TimestampedBuffer> result = queue.getBetweenAndRemove(now, end);

        assertThat(result).hasSize(3);
        assertThat(queue.size()).isZero();
    }

    @Test
    void shouldNotRemoveElementsOutsideRange() throws InterruptedException {
        TimeSortedBufferQueue queue = new TimeSortedBufferQueue();
        Instant past = Instant.now().minusSeconds(10);
        queue.add(ByteBuffer.wrap(new byte[]{1}));
        Thread.sleep(10);
        Instant future = Instant.now().plusSeconds(10);

        PriorityBlockingQueue<TimestampedBuffer> result = queue.getBetweenAndRemove(past, past.plusMillis(5));

        assertThat(result).isEmpty();
        assertThat(queue.size()).isEqualTo(1);
    }

    @Test
    void shouldBeThreadSafeForAddAndRemove() throws InterruptedException {
        TimeSortedBufferQueue queue = new TimeSortedBufferQueue();
        int threads = 10;
        int iterations = 1000;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger added = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        queue.add(ByteBuffer.wrap(new byte[]{(byte) j}));
                        added.incrementAndGet();
                        if (j % 100 == 0) {
                            queue.getBetweenAndRemove(Instant.now().minusSeconds(1), Instant.now().plusSeconds(1));
                        }
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
        assertThat(queue.size()).isLessThanOrEqualTo(added.get());
    }
}
