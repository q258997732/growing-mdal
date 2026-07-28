package bob.growingmdal.config;

import bob.growingmdal.handler.HardwareWebSocketHandler;
import bob.growingmdal.security.HardwareHandshakeInterceptor;
import bob.growingmdal.service.CommandDispatcherService;
import bob.growingmdal.service.WebSocketOutboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${hardware.api-key}")
    private String apiKey;

    @Value("${spring.websocket.allowed-origins}")
    private String allowedOrigins;

    private final CommandDispatcherService dispatcherService;
    private final WebSocketSessionManager sessionManager;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;
    private final WebSocketOutboundService outboundService;

    @Autowired
    public WebSocketConfig(CommandDispatcherService dispatcherService,
                           WebSocketSessionManager sessionManager,
                           @Qualifier("messageTaskExecutor") TaskExecutor taskExecutor,
                           ObjectMapper objectMapper,
                           WebSocketOutboundService outboundService) {
        this.dispatcherService = dispatcherService;
        this.sessionManager = sessionManager;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
        this.outboundService = outboundService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(hardwareWebSocketHandler(), "/hardware-ws")
                .setAllowedOrigins(allowedOrigins.split(","))
                .addInterceptors(hardwareHandshakeInterceptor());
    }

    @Bean
    public HardwareHandshakeInterceptor hardwareHandshakeInterceptor() {
        return new HardwareHandshakeInterceptor(apiKey, allowedOrigins);
    }

    @Bean
    public HardwareWebSocketHandler hardwareWebSocketHandler() {
        return new HardwareWebSocketHandler(dispatcherService, sessionManager, taskExecutor, objectMapper, outboundService);
    }

    @Lazy
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