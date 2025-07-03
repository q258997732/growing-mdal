package bob.growingmdal.core.command;

import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NantianCameraAdapter {

    private Session session;

    private final ArrayDeque<ByteBuffer> binaryQueue = new ArrayDeque<>();
    private final ArrayDeque<String> messageQueue = new ArrayDeque<>();
    private final AtomicBoolean shouldCollect = new AtomicBoolean(true);
    private final CountDownLatch messageLatch = new CountDownLatch(1);
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    private long msgStartTime;
    private long binaryStartTime;
    private long timeout = 60000;

    public NantianCameraAdapter() {
        super();
    }

    public NantianCameraAdapter(long timeout){
        super();
        this.timeout = timeout;
    }

    private final Runnable cleanupTask = () -> {
        if (!shouldCollect.get()) return; // 如果已停止收集，直接跳过

        synchronized (this) { // 确保线程安全
            int removedBinary = binaryQueue.size();
            int removedText = messageQueue.size();
            long currentTime = System.currentTimeMillis();

            // 清理二进制队列
            if(currentTime > binaryStartTime + timeout){
                binaryQueue.poll();
                removedBinary++;
            }

            // 清理文本队列
            while (currentTime > msgStartTime + timeout) {
                messageQueue.poll();
                removedText++;
            }

            if (removedBinary > 0 || removedText > 0) {
                log.info("Cleaned up {} binary and {} text messages", removedBinary, removedText);
            }

            // 重置开始时间
            binaryStartTime = msgStartTime = System.currentTimeMillis();

        }
    };

    @ClientEndpoint
    public class WebSocketClientEndpoint {

        @OnOpen
        public void onOpen(Session session) {
            // 根据获取到的信息做分类
            System.out.println("Connected to server");
            NantianCameraAdapter.this.session = session;
        }

        /**
         * 接收到字符串消息
         * @param message 接收到的字符串消息
         */
        @OnMessage
        public void onMessage(String message) {
            if(shouldCollect.get()){
                log.info("Received message: {}", message);
                messageQueue.add(message);
            }
            messageLatch.countDown();
        }

        /**
         * 接收到二进制消息
         * @param bytes 接收到的二进制消息
         * @param session 会话
         */
        @OnMessage
        public void onMessage(ByteBuffer bytes, Session session) {
            if(shouldCollect.get()) {
                log.info("Received BINARY message, length: {}", bytes.remaining());
                binaryQueue.add(bytes);
            }
            messageLatch.countDown();
        }

        @OnClose
        public void onClose(Session session, CloseReason closeReason) {
            System.out.println("Disconnected from server: " + closeReason);
            NantianCameraAdapter.this.session = null;
        }

        @OnError
        public void onError(Session session, Throwable throwable) {
            System.err.println("WebSocket error:");
            throwable.printStackTrace();
        }
    }

    /**
     * 连接WebSocket服务器
     */
    public void connect(String serverUri) throws DeploymentException, IOException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        // 设置最大缓存空间
        container.setDefaultMaxBinaryMessageBufferSize(100 * 1024 * 1024);
        container.connectToServer(new WebSocketClientEndpoint(), URI.create(serverUri));
    }

    /**
     * 关闭连接
     */
    public void disconnect() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
        executor.shutdown();
    }

    /**
     * 同步发送文本消息（第一种方式）
     * @param command 要发送的命令字符串
     * @return 是否发送成功
     */
    public boolean sendCameraCommand(String command) {
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket session is not open, cannot send command: {}", command);
            return false;
        }

        try {
            session.getBasicRemote().sendText(command); // 直接同步发送文本
            log.debug("Sent command: {}", command);
            return true;
        } catch (IOException e) {
            log.error("Failed to send command: {}", command, e);
            return false;
        }
    }






}