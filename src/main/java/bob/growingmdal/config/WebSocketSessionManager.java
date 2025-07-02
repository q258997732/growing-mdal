package bob.growingmdal.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WebSocketSessionManager {
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
    }

    public void unregisterSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public void sendToSession(String sessionId, TextMessage message) throws IOException {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            session.sendMessage(message);
        }
    }
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
}