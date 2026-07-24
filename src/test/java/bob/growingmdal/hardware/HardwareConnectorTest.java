package bob.growingmdal.hardware;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HardwareConnectorTest {

    @Test
    void shouldExposeProtocolType() {
        HardwareConnector<Object> connector = new TestHardwareConnector(ProtocolType.HTTP);

        assertThat(connector.protocol()).isEqualTo(ProtocolType.HTTP);
    }

    @Test
    void shouldReportConnectionState() {
        TestHardwareConnector connector = new TestHardwareConnector(ProtocolType.SERIAL);

        assertThat(connector.isConnected()).isFalse();

        boolean connected = connector.connect();

        assertThat(connected).isTrue();
        assertThat(connector.isConnected()).isTrue();

        connector.disconnect();

        assertThat(connector.isConnected()).isFalse();
    }

    static class TestHardwareConnector implements HardwareConnector<Object> {
        private final ProtocolType protocol;
        private boolean connected;

        TestHardwareConnector(ProtocolType protocol) {
            this.protocol = protocol;
        }

        @Override
        public ProtocolType protocol() {
            return protocol;
        }

        @Override
        public boolean connect() {
            connected = true;
            return true;
        }

        @Override
        public void disconnect() {
            connected = false;
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public Object getUnderlying() {
            return new Object();
        }
    }
}
