package bob.growingmdal.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CustomDllLoader {

    /**
     * 从resources/lib下的指定设备目录加载DLL
     * @param deviceType 设备类型（对应lib下的子目录名）
     * @param dllName    DLL文件名（如dcrf32.dll）
     */
    public static void loadFromDeviceLib(String deviceType, String dllName) throws IOException {
        String dllPath = "lib/" + deviceType + "/" + dllName;
        loadFromClasspath(dllPath);

    }

    /**
     * 从类路径任意位置加载DLL
     * @param classpathDllPath 类路径下的相对路径（如lib/device_a/dcrf32.dll）
     */
    public static void loadFromClasspath(String classpathDllPath) throws IOException {
        // 验证路径
        if (!StringUtils.hasText(classpathDllPath)) {
            throw new IllegalArgumentException("DLL路径不能为空");
        }

        // 获取资源流
        ClassPathResource resource = new ClassPathResource(classpathDllPath);
        if (!resource.exists()) {
            throw new IOException("DLL文件不存在: " + classpathDllPath);
        }

        // 创建临时文件
        String[] parts = classpathDllPath.split("/");
        String filename = parts[parts.length - 1];
        File tempFile = File.createTempFile(filename.replace(".dll", "") + "_", ".dll");
        tempFile.deleteOnExit();

        // 复制到临时文件
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // 加载DLL
        System.load(tempFile.getAbsolutePath());
    }

    /**
     * 直接从文件系统路径加载DLL（绝对路径）
     */
    public static void loadFromAbsolutePath(String absolutePath) {
        if (!new File(absolutePath).exists()) {
            throw new IllegalArgumentException("DLL文件不存在: " + absolutePath);
        }
        System.load(absolutePath);
    }
}