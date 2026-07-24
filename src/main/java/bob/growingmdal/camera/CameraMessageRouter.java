package bob.growingmdal.camera;

import bob.growingmdal.entity.TimeSortedBufferQueue;
import jakarta.websocket.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

@Slf4j
@Component
public class CameraMessageRouter {

    private static final int TEXT_QUEUE_CAPACITY = 1000;

    @Getter
    private final LinkedBlockingQueue<String> textQueue = new LinkedBlockingQueue<>(TEXT_QUEUE_CAPACITY);
    @Getter
    private final TimeSortedBufferQueue binaryQueue = new TimeSortedBufferQueue();

    private CameraResponseMatcher responseMatcher;

    public void setResponseMatcher(CameraResponseMatcher responseMatcher) {
        this.responseMatcher = responseMatcher;
    }

    /**
     * 处理文本消息：加入队列（满时丢弃最旧），并通知响应匹配器
     */
    public void onTextMessage(String message) {
        synchronized (textQueue) {
            if (!textQueue.offer(message)) {
                String dropped = textQueue.poll();
                if (dropped != null) {
                    log.debug("Text queue full, dropped oldest message: {}", dropped);
                }
                textQueue.offer(message);
            }
        }
        if (responseMatcher != null) {
            responseMatcher.onMessage(message);
        }
    }

    /**
     * 处理二进制消息：加入时间排序缓冲区队列
     */
    public void onBinaryMessage(ByteBuffer bytes) {
        binaryQueue.add(bytes);
    }

    /**
     * 清空文本队列
     */
    public void clearTextQueue() {
        synchronized (textQueue) {
            textQueue.clear();
        }
    }

    /**
     * 清空二进制队列
     */
    public void clearBinaryQueue() {
        binaryQueue.clear();
    }

    /**
     * 获取文本队列大小
     */
    public int getTextQueueSize() {
        return textQueue.size();
    }

    /**
     * 获取二进制队列大小
     */
    public int getBinaryQueueSize() {
        return binaryQueue.size();
    }

    /**
     * 安全地扫描文本队列，取出所有匹配的消息，未匹配的重新入队
     */
    public List<String> drainMatching(Predicate<String> predicate) {
        synchronized (textQueue) {
            List<String> matched = new ArrayList<>();
            List<String> remaining = new ArrayList<>();
            textQueue.drainTo(remaining);
            for (String msg : remaining) {
                if (predicate.test(msg)) {
                    matched.add(msg);
                } else {
                    textQueue.offer(msg);
                }
            }
            return matched;
        }
    }
}
