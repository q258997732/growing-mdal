package bob.growingmdal.adapter;

import jakarta.websocket.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ClientEndpoint
public class VideoTest {

    private Session session;
    private final CountDownLatch messageLatch = new CountDownLatch(1);
    private static final AtomicBoolean shouldContinue = new AtomicBoolean(true);

    public static void main(String[] args) {
        try {
            URI uri = new URI("ws://192.168.107.103:7000");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxBinaryMessageBufferSize(100 * 1024 * 1024);

            VideoTest client = new VideoTest();
            container.connectToServer(client, uri);

            shouldContinue.set(false);
            client.sendMessage("OpenDevice@2");

            client.sendMessage("OpenVideo");

            Thread.sleep(2000);
            boolean received = client.messageLatch.await(10, TimeUnit.SECONDS);
            if(!received) {
                System.out.println("No response from server within 10 seconds");
            }

            client.sendMessage("CloseVideo");


            client.sendMessage("CloseDevice");


            client.session.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server");
        this.session = session;
    }

    // 处理文本消息
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received TEXT message: " + message);
        shouldContinue.set(true);
        messageLatch.countDown();
    }

    // 处理二进制消息
    @OnMessage
    public void onMessage(ByteBuffer bytes, Session session) {
        System.out.println("Received BINARY message, length: " + bytes.remaining());
        try {
            // 创建文件输出流
            FileOutputStream fos = new FileOutputStream("received_image.jpg"+System.currentTimeMillis());
            // 获取通道
            FileChannel channel = fos.getChannel();
            // 写入文件
            channel.write(bytes);
            channel.close();
            fos.close();
            System.out.println("Image saved as received_image.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageLatch.countDown();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: ");
        throwable.printStackTrace();
    }

    public void sendMessage(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
                System.out.println("Sent message: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}