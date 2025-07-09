package bob.growingmdal.entity;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.PriorityBlockingQueue;

public class TimeSortedBufferQueue {
    private final PriorityBlockingQueue<TimestampedBuffer> queue
            = new PriorityBlockingQueue<>();

    /**
     * 添加数据（自动按时间戳排序）
     */
    public void add(ByteBuffer buffer) {
        queue.put(new TimestampedBuffer(buffer));
    }

    /**
     * 取出最早的数据（队列头部）
     */
    public ByteBuffer takeEarliest() throws InterruptedException {
        return queue.take().getBuffer(); // 阻塞直到有数据
    }

    /**
     * 获取但不移除最早的数据
     */
    public ByteBuffer peekEarliest() {
        TimestampedBuffer item = queue.peek();
        return item != null ? item.getBuffer() : null;
    }

    /**
     * 获取所有数据（按时间戳从早到晚排序）
     */
    public PriorityBlockingQueue<TimestampedBuffer> getAll() {
        return new PriorityBlockingQueue<>(queue); // 返回副本
    }

    /**
     * 获取某个时间点之后的数据（仍保持排序）
     */
    public PriorityBlockingQueue<TimestampedBuffer> getAfter(Instant time) {
        PriorityBlockingQueue<TimestampedBuffer> result = new PriorityBlockingQueue<>();
        queue.forEach(tb -> {
            if (tb.getTimestamp().isAfter(time)) {
                result.add(tb);
            }
        });
        return result;
    }

    /**
     * 获取某个时间段内的数据（并移除）
     * @param start 时间段开始
     * @param end 时间段结束
     * @return 获取到的数据
     */
    public PriorityBlockingQueue<TimestampedBuffer> getBetweenAndRemove(Instant start, Instant end) {
        PriorityBlockingQueue<TimestampedBuffer> result = new PriorityBlockingQueue<>();
        queue.forEach(tb -> {
            if (!tb.getTimestamp().isBefore(start) && !tb.getTimestamp().isAfter(end)) {
                result.add(tb);
            }
        });
        queue.removeAll(result);
        return result;
    }

    /**
     * 清空队列
     */
    public void clear() {
        queue.clear();
    }

    /**
     * 队列大小
     */
    public int size() {
        return queue.size();
    }
}