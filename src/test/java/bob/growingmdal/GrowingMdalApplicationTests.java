package bob.growingmdal;

import bob.growingmdal.adapter.DekaReader;
import bob.growingmdal.service.DekaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.ZoneId;


@SpringBootTest
class GrowingMdalApplicationTests {

    @Autowired
    DekaService dekaService;
    @Test
    void contextLoads() throws IOException {
        DekaReader reader = DekaReader.load();
    }
}
