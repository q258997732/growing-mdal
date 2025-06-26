package bob.growingmdal.config;

import bob.growingmdal.handler.HardwareWebSocketHandler;
import bob.growingmdal.service.CommandDispatcherService;
import bob.growingmdal.service.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CommandDispatcherService dispatcherService;
    private final WebSocketSessionManager sessionManager;

    @Autowired
    public WebSocketConfig(CommandDispatcherService dispatcherService,
                           WebSocketSessionManager sessionManager) {
        this.dispatcherService = dispatcherService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(hardwareWebSocketHandler(), "/hardware-ws")
                .setAllowedOrigins("*");
    }

    @Bean
    public HardwareWebSocketHandler hardwareWebSocketHandler() {
        return new HardwareWebSocketHandler(dispatcherService, sessionManager);
    }
}