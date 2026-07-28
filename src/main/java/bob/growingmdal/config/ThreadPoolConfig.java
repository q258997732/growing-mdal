package bob.growingmdal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
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

    /**
     * 用于 WebSocket 流式事件 outbound 发送的线程池。
     * 与入站线程池隔离，避免视频帧发送阻塞命令处理。
     * 队列满时丢弃最老任务，防止内存无限增长，并记录丢弃日志。
     */
    @Bean("outboundTaskExecutor")
    public TaskExecutor outboundTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ws-outbound-");
        executor.setRejectedExecutionHandler(new LoggingDiscardOldestPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 丢弃最老任务并记录 WARN 日志，便于运维发现背压。
     */
    @Slf4j
    private static class LoggingDiscardOldestPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                Runnable dropped = executor.getQueue().poll();
                if (dropped != null) {
                    // 无法判断具体是视频帧还是其他事件，统一记录。
                    log.warn("[ws-outbound] Dropped oldest outbound message due to backpressure");
                }
                executor.execute(r);
            }
        }
    }
}
