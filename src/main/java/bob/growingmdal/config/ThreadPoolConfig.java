package bob.growingmdal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {
    /***
     * 创建一个线程池，用于处理消息
     * @return 线程池
     */
    @Bean("messageTaskExecutor")
    public TaskExecutor messageTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(10);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 线程池所使用的缓冲队列
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("ws-message-");
        executor.initialize();
        return executor;
    }
}