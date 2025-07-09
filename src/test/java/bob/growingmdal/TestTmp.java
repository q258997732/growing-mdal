package bob.growingmdal;

import java.time.Duration;
import java.time.Instant;

import static java.lang.Thread.sleep;

public class TestTmp {
    public static void main(String[] args) throws InterruptedException {
        Instant start = Instant.now();
        sleep(1000);
        Instant end = Instant.now();
        start = start.plus(Duration.ofSeconds(30));
        System.out.println(Duration.between(start, end).toMillis());
    }
}
