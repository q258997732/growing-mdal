package bob.growingmdal.camera;

import bob.growingmdal.entity.response.NantianCameraResponse;
import bob.growingmdal.util.ZZWsResponseParser;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CameraResponseMatcher {

    private final Map<String, CompletableFuture<String>> pendingResponses = new ConcurrentHashMap<>();

    /**
     * 发送消息并等待响应，使用 CompletableFuture 与超时机制
     *
     * @param message        发送的消息（如 OpenDevice@2）
     * @param timeoutSeconds 超时时间（秒）
     * @param session        WebSocket 会话
     * @return NantianCameraResponse
     */
    public NantianCameraResponse sendMessageGetResponse(String message, int timeoutSeconds, Session session) {
        String prefix = extractPrefix(message);
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingResponses.put(prefix, future);

        sendMessage(message, session);

        String response;
        try {
            response = future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Interrupted or timeout while waiting for response to {}", message, e);
            pendingResponses.remove(prefix);
            return new NantianCameraResponse(500, "Internal Server Error", e.getMessage());
        }
        pendingResponses.remove(prefix);

        log.info("response is: {}", response);

        String retData = null;
        if (message.startsWith("Capture")) {
            retData = ZZWsResponseParser.getCaptureBase64(response);
        } else if (message.startsWith("GetFaceTemplFromBase64")) {
            retData = ZZWsResponseParser.getFaceEigenvalueData(response);
        }

        String parsed = ZZWsResponseParser.parseResponse(response);
        log.info(parsed);

        int code = parsed.contains("成功") ? 200 : 500;
        return new NantianCameraResponse(code, parsed, retData);
    }

    /**
     * 当收到新消息时，尝试匹配挂起的请求
     */
    public void onMessage(String message) {
        for (Map.Entry<String, CompletableFuture<String>> entry : pendingResponses.entrySet()) {
            String prefix = entry.getKey();
            if (message.startsWith(prefix + "#")) {
                entry.getValue().complete(message);
                break;
            }
        }
    }

    private void sendMessage(String message, Session session) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
                log.info("Sent message: {}", message);
            }
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private String extractPrefix(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        int atIndex = message.indexOf('@');
        return atIndex >= 0 ? message.substring(0, atIndex) : message;
    }
}
