package bob.growingmdal.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    /**
     * 连接建立成功后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        sendToClient(session, "连接已建立!");
    }

    /**
     * 接收到消息时调用
     * @param session 当前客户端会话
     * @param message 接收到的消息内容
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);

        // 回复发送者
        sendToClient(session, "Server: 已收到你的消息 - " + payload);

        // 广播给所有客户端
        broadcast("Server: 广播消息 - " + payload);
    }

    /**
     * 发送消息给指定客户端
     * @param session 当前客户端会话
     * @param message 要发送的消息内容
     */
    public static void sendToClient(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 广播给所有客户端
     * @param message 要发送的消息内容
     */
    public static void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}