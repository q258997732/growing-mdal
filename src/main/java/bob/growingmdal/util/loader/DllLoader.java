package bob.growingmdal.util.loader;


import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
                System.out.println("DLL not found in resources: " + dllName);
            }

            // 加载DLL
            System.load(temp.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DLL: " + dllName, e);
        }
    }
}
