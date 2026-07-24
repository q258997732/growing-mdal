package bob.growingmdal.security;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 路径遍历校验器。
 * <p>
 * 确保用户传入的路径解析后仍位于允许的基目录内。
 */
public final class PathTraversalValidator {

    private PathTraversalValidator() {
        // utility class
    }

    /**
     * 校验用户路径是否位于基目录内。
     *
     * @param baseDir  允许的基目录
     * @param userPath 用户传入的路径
     * @return 解析后的绝对路径
     * @throws IllegalArgumentException 如果路径为空、不合法或试图跳出基目录
     */
    public static Path validate(String baseDir, String userPath) {
        if (baseDir == null || baseDir.isBlank()) {
            throw new IllegalArgumentException("baseDir must not be blank");
        }
        if (userPath == null || userPath.isBlank()) {
            throw new IllegalArgumentException("userPath must not be blank");
        }

        Path base = Paths.get(baseDir).toAbsolutePath().normalize();
        Path resolved = base.resolve(userPath).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("Path traversal attempt: " + userPath);
        }
        return resolved;
    }
}
