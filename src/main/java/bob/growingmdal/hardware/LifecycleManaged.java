package bob.growingmdal.hardware;

import org.springframework.boot.actuate.health.Health;

/**
 * 硬件服务的统一生命周期管理接口。
 * 所有硬件服务都应实现此接口，以便在启动、健康检查和优雅关闭时复用统一逻辑。
 */
public interface LifecycleManaged {

    void initialize();

    Health health();

    void shutdown();
}
