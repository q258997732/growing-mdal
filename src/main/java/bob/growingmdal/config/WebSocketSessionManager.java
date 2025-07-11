package bob.growingmdal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
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

    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }

    public void closeAllSessions() {
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException ignored) {
                log.warn("Error closing session {}: {}", session.getId(), ignored.getMessage());
            }
        });
        sessions.clear();
    }
}