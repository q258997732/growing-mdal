package bob.growingmdal.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手拦截器。
 * <p>
 * 校验请求头中的 Origin 与 X-API-Key，未通过则拒绝握手。
 */
@Slf4j
public class HardwareHandshakeInterceptor implements HandshakeInterceptor {

    private final String expectedApiKey;
    private final List<String> allowedOrigins;

    public HardwareHandshakeInterceptor(String expectedApiKey, String allowedOrigins) {
        if (expectedApiKey == null || expectedApiKey.isBlank()) {
            throw new IllegalArgumentException("hardware.api-key must not be blank");
        }
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            throw new IllegalArgumentException("spring.websocket.allowed-origins must not be blank");
        }
        this.expectedApiKey = expectedApiKey;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (this.allowedOrigins.isEmpty()) {
            throw new IllegalArgumentException("spring.websocket.allowed-origins must contain at least one origin");
        }
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String origin = getFirstHeader(request, "Origin");
        if (origin == null || !allowedOrigins.contains(origin)) {
            log.warn("WebSocket handshake rejected: invalid or missing origin");
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        String apiKey = getFirstHeader(request, "X-API-Key");
        if (apiKey == null || !expectedApiKey.equals(apiKey)) {
            log.warn("WebSocket handshake rejected: invalid or missing API key");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    private String getFirstHeader(ServerHttpRequest request, String name) {
        List<String> values = request.getHeaders().get(name);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }
}
