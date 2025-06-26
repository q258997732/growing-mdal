package bob.growingmdal.util;


import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
public class DllLoader {
    public static void loadDll(String dllName) {
        try {
            // 从resources读取DLL
            InputStream in = DllLoader.class.getClassLoader().getResourceAsStream(dllName);

            // 创建临时文件
            File temp = File.createTempFile(dllName.replace(".dll", ""), ".dll");
            temp.deleteOnExit();

            // 写入临时文件
            if (in != null) {
                Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else{
                log.error("DLL not found: " + dllName);
            }

            // 加载DLL
            System.load(temp.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DLL: " + dllName, e);
        }
    }
}
