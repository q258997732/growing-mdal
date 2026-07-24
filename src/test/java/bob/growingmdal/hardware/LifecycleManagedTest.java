package bob.growingmdal.hardware;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import static org.assertj.core.api.Assertions.assertThat;

class LifecycleManagedTest {

    @Test
    void shouldInitializeAllManagedHandlers() {
        TestLifecycleManaged managed = new TestLifecycleManaged();

        managed.initialize();

        assertThat(managed.isInitialized()).isTrue();
    }

    @Test
    void shouldShutdownAllManagedHandlers() {
        TestLifecycleManaged managed = new TestLifecycleManaged();
        managed.initialize();

        managed.shutdown();

        assertThat(managed.isShutdown()).isTrue();
    }

    @Test
    void shouldReportHealthStatus() {
        TestLifecycleManaged managed = new TestLifecycleManaged();

        Health health = managed.health();

        assertThat(health.getStatus()).isEqualTo(Health.up().build().getStatus());
    }

    static class TestLifecycleManaged implements LifecycleManaged {
        private boolean initialized;
        private boolean shutdown;

        @Override
        public void initialize() {
            initialized = true;
        }

        @Override
        public Health health() {
            return Health.up().build();
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        public boolean isInitialized() {
            return initialized;
        }

        public boolean isShutdown() {
            return shutdown;
        }
    }
}
