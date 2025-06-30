package bob.growingmdal;

import bob.growingmdal.adapter.DekaReader;
import bob.growingmdal.service.DekaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;


@SpringBootTest
class GrowingMdalApplicationTests {

    @Autowired
    DekaService dekaService;
    @Test
    void contextLoads() throws IOException {
        DekaReader reader = DekaReader.load();

        byte[] info_buffer = new byte[512];
        reader.LibMain(0,info_buffer);
        System.out.println("dcrf32 version: " + DekaReader.gbk_bytes_to_string(info_buffer));

        reader.LibMain(1,DekaReader.string_to_gbk_bytes("/lib/deka_T10-MX4_x64/"));



    }
}
