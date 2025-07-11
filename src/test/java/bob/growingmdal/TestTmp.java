package bob.growingmdal;

import bob.growingmdal.adapter.DekaReaderAdapter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;

import static java.lang.Thread.sleep;

public class TestTmp {
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        String currentPath = System.getProperty("user.dir");
        System.out.println("currentPath: " + currentPath);
        String libPath = currentPath + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "lib" + File.separator + "deka_T10-MX4_x64";
        System.out.println("libPath: " + libPath);

    }
}
