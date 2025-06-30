package bob.growingmdal.util.loader;

import jnr.ffi.LibraryLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * JNR 本地库加载工具类
 */
public class NativeLibraryLoader {

    /**
     * 加载本地库并返回绑定接口
     * @param interfaceClass 接口类 (如 DekaReader.class)
     * @param libName 库文件名 (如 "dcrf32.dll")
     * @param resourcePath 资源路径 (如 "/lib/deka_T10-MX4_x64/")
     */
    public static <T> T load(Class<T> interfaceClass, String libName, String resourcePath) {
        try {
            // 1. 从JAR资源提取DLL到临时目录
            Path tempFile = extractLibrary(libName, resourcePath);

            // 2. 设置JNR库搜索路径
            addLibraryPath(tempFile.getParent());

            System.out.println("JNR library search path: " + System.getProperty("jnr.ffi.library.path"));

            // 3. 加载库并返回接口实例
            return LibraryLoader.create(interfaceClass)
                    .load(libName.replace(".dll", "").replace(".so", "").replace(".dylib", ""));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library: " + libName, e);
        }
    }

    private static Path extractLibrary(String libName, String resourcePath) throws IOException {
        System.out.println("Loading library: " + resourcePath + libName);
        InputStream is = io.netty.util.internal.NativeLibraryLoader.class.getResourceAsStream(resourcePath + libName);
        if (is == null) {
            throw new IOException("Resource not found: " + resourcePath + libName);
        }

        Path tempDir = Files.createTempDirectory("native-libs");
        Path tempFile = tempDir.resolve(libName);

        System.out.println("Extracting DLL to: " + tempFile.toAbsolutePath());  // 添加这行

        Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        tempFile.toFile().deleteOnExit();
        tempDir.toFile().deleteOnExit();
        return tempFile;
    }

    private static void addLibraryPath(Path libPath) {
        String existingPath = System.getProperty("jnr.ffi.library.path", "");
        System.setProperty("jnr.ffi.library.path",
                libPath.toString() + (existingPath.isEmpty() ? "" : File.pathSeparator + existingPath));
    }
}