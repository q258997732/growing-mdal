package bob.growingmdal;

import bob.growingmdal.adapter.DekaReaderAdapter;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.service.DekaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@SpringBootTest
class GrowingMdalApplicationTests {

    @Autowired
    DekaService dekaService;
    @Test
    void contextLoads() throws IOException {
//        DekaReaderAdapter reader = DekaReaderAdapter.load();
//
//        byte[] info_buffer = new byte[512];
//        reader.LibMain(0,info_buffer);
//        System.out.println("dcrf32 version: " + DekaReaderAdapter.gbk_bytes_to_string(info_buffer));
//
//        reader.LibMain(1, DekaReaderAdapter.string_to_gbk_bytes("/lib/deka_T10-MX4_x64/"));




    }


}

