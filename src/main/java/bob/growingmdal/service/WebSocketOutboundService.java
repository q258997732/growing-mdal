package bob.growingmdal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * 统一负责 WebSocket outbound 发送的服务。
 * <p>
 * - 命令响应/错误：同步发送，保证命令结果顺序。
 * - 流式事件：提交到独立 outbound 线程池，避免摄像头工作线程阻塞在网络 I/O。
 *   由于使用多线程池，同一会话的流式事件顺序为尽力保证，极端背压下可能丢帧。
 * - 所有发送均对 session 加锁，防止帧交错。
 */
@Slf4j
@Service
public class WebSocketOutboundService {

    private final TaskExecutor outboundTaskExecutor;

    public WebSocketOutboundService(@Qualifier("outboundTaskExecutor") TaskExecutor outboundTaskExecutor) {
        this.outboundTaskExecutor = outboundTaskExecutor;
    }

    /**
     * 发送命令同步响应或错误。调用方线程执行，保持命令顺序。
     */
    public void sendCommandResponse(WebSocketSession session, String json) {
        safeSend(session, json);
    }

    /**
     * 发送流式事件（视频帧、人脸检测结果、身份证插卡提示等）。
     * 提交到独立线程池执行，发布线程立即返回。
     */
    public void sendStreamEvent(WebSocketSession session, String json) {
        outboundTaskExecutor.execute(() -> safeSend(session, json));
    }

    private void safeSend(WebSocketSession session, String json) {
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("Failed to send WebSocket message to session {}", session.getId(), e);
            }
        }
    }
}
