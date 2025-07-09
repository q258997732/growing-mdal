package bob.growingmdal.entity;

import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class TimestampedBuffer implements Comparable<TimestampedBuffer> {

    private final ByteBuffer buffer;
    private final Instant timestamp;

    public TimestampedBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        this.timestamp = Instant.now();
    }

    @Override
    public int compareTo(TimestampedBuffer other) {
        return this.timestamp.compareTo(other.timestamp);
    }
}
