package bob.growingmdal.config;

import bob.growingmdal.handler.HardwareWebSocketHandler;
import bob.growingmdal.service.CommandDispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

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

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 设置文本消息缓冲区大小（单位：字节），默认是8192字节
        container.setMaxTextMessageBufferSize(2048 * 1024); // KB
        // 设置二进制消息缓冲区大小（单位：字节），默认是8192字节
        container.setMaxBinaryMessageBufferSize(2048 * 1024); // KB
        // 设置空闲超时时间（毫秒）
        container.setMaxSessionIdleTimeout(30 * 60 * 1000L); // 30分钟
        return container;
    }
}