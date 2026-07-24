package bob.growingmdal.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HardwareHandshakeInterceptorTest {

    private static final String API_KEY = "secret-key";
    private static final String ALLOWED_ORIGINS = "http://localhost:8080,https://ai-console.example.com";

    private final HardwareHandshakeInterceptor interceptor =
            new HardwareHandshakeInterceptor(API_KEY, ALLOWED_ORIGINS);

    @Test
    void shouldAllowHandshakeWithValidKeyAndOrigin() {
        ServerHttpRequest request = createRequest("http://localhost:8080", API_KEY);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), Map.of());

        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectHandshakeWithMissingKey() {
        ServerHttpRequest request = createRequest("http://localhost:8080", null);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), Map.of());

        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectHandshakeWithInvalidKey() {
        ServerHttpRequest request = createRequest("http://localhost:8080", "wrong-key");
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), Map.of());

        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectHandshakeWithInvalidOrigin() {
        ServerHttpRequest request = createRequest("http://evil.com", API_KEY);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), Map.of());

        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldRejectHandshakeWithMissingOrigin() {
        ServerHttpRequest request = createRequest(null, API_KEY);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), Map.of());

        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldRejectBlankApiKeyAtConstruction() {
        assertThatThrownBy(() -> new HardwareHandshakeInterceptor("", ALLOWED_ORIGINS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hardware.api-key must not be blank");
    }

    @Test
    void shouldRejectBlankAllowedOriginsAtConstruction() {
        assertThatThrownBy(() -> new HardwareHandshakeInterceptor(API_KEY, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spring.websocket.allowed-origins must not be blank");
    }

    private ServerHttpRequest createRequest(String origin, String apiKey) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (origin != null) {
            headers.setOrigin(origin);
        }
        if (apiKey != null) {
            headers.put("X-API-Key", List.of(apiKey));
        }
        when(request.getHeaders()).thenReturn(headers);
        return request;
    }
}
