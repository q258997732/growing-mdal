package bob.growingmdal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrowingMdalApplication {

    public static void main(String[] args) {
//        System.out.println("file.encoding: " + System.getProperty("file.encoding"));
//        System.out.println("Default Charset: " + java.nio.charset.Charset.defaultCharset());
        SpringApplication.run(GrowingMdalApplication.class, args);
    }

}
