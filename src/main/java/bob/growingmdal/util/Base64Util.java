package bob.growingmdal.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
public class Base64Util {

    public static String convertBmpToBase64(String filePath) {
        try {
            // 读取BMP文件
            File file = new File(filePath);
            byte[] fileContent = new byte[(int) file.length()];

            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileContent);
            }

            // 使用Base64编码器进行编码
            return Base64.getEncoder().encodeToString(fileContent);

        } catch (IOException e) {
            log.error("转换BMP到Base64时出错: {}", e.getMessage());
            return null;
        }
    }

    public static String convertBmpToBase64Prefix(String filePath){
        return "data:image/bmp;base64," + convertBmpToBase64(filePath);
    }

}
