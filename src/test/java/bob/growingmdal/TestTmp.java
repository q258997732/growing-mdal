package bob.growingmdal;

import bob.growingmdal.adapter.DekaReaderAdapter;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;

import static java.lang.Thread.sleep;

public class TestTmp {
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        byte[] version = new byte[64];
        DekaReaderAdapter dekaReaderAdapter = DekaReaderAdapter.load();


        dekaReaderAdapter.LibMain(0, version);
        System.out.println("dcrf32 version: " + DekaReaderAdapter.gbk_bytes_to_string(version));



    }
}
