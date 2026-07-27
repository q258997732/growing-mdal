package bob.growingmdal.connector;

import bob.growingmdal.config.AdapterProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件上传存储管理器。
 * <p>
 * 使用内存索引维护文件元数据，文件内容落盘到配置目录。
 */
@Slf4j
@Component
public class FileUploadStore {

    private final AdapterProperties adapterProperties;

    private final Map<String, FileMetadata> metadataMap = new ConcurrentHashMap<>();
    private final Map<String, ChunkState> chunkMap = new ConcurrentHashMap<>();

    private static final int MAX_METADATA_ENTRIES = 10_000;

    public FileUploadStore(AdapterProperties adapterProperties) {
        this.adapterProperties = adapterProperties;
    }

    @PostConstruct
    public void init() throws IOException {
        Path path = Path.of(adapterProperties.getFileUploadPath());
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("Created file upload directory: {}", path);
        }
    }

    /**
     * 保存完整文件。
     *
     * @param name            原始文件名
     * @param contentBase64   Base64 编码的文件内容
     * @return 存储后的 UUID 文件名
     */
    public String saveFile(String name, String contentBase64) throws IOException {
        byte[] content = Base64.getDecoder().decode(contentBase64);
        String md5 = calculateMd5(content);
        String storageName = UUID.randomUUID().toString();
        Path target = resolvePath(storageName);
        Files.write(target, content);

        metadataMap.put(md5, new FileMetadata(name, storageName, target.toString(), md5, Instant.now()));
        log.info("File uploaded: originalName={}, storageName={}, size={}", name, storageName, content.length);
        return storageName;
    }

    /**
     * 保存分块。
     *
     * @param md5           文件 MD5
     * @param name          原始文件名
     * @param size          文件总大小
     * @param chunks        总块数
     * @param chunk         当前块索引（从 0 开始）
     * @param contentBase64 Base64 编码的块内容
     * @return 是否所有块都已上传完成
     */
    public boolean saveChunk(String md5, String name, long size, int chunks, int chunk, int chunkSize, String contentBase64)
            throws IOException {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        ChunkState state = chunkMap.computeIfAbsent(md5, k -> new ChunkState(md5, name, size, chunks));
        byte[] content = Base64.getDecoder().decode(contentBase64);

        synchronized (state) {
            Path target = resolvePath(state.getStorageName());
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(target.toFile(), "rw")) {
                if (chunk == 0) {
                    randomAccessFile.setLength(size);
                }
                long offset = (long) chunk * chunkSize;
                randomAccessFile.seek(offset);
                randomAccessFile.write(content);
            }
            state.markUploaded(chunk);

            if (state.isComplete()) {
                metadataMap.put(md5, new FileMetadata(name, state.getStorageName(), target.toString(), md5, Instant.now()));
                chunkMap.remove(md5);
                evictMetadataIfNeeded();
                log.info("Chunk upload completed: md5={}, name={}, chunks={}", md5, name, chunks);
                return true;
            }
            return false;
        }
    }

    /**
     * 根据 MD5 检查文件是否已存在。
     *
     * @param md5 文件 MD5
     * @return true=存在
     */
    public boolean existsByMd5(String md5) {
        return metadataMap.containsKey(md5);
    }

    /**
     * 根据存储名获取原始文件名。
     *
     * @param storageName 存储名
     * @return 原始文件名
     */
    public String getOriginalName(String storageName) {
        return metadataMap.values().stream()
                .filter(m -> m.storageName().equals(storageName))
                .map(FileMetadata::originalName)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查上传目录是否可写。
     *
     * @return true=可写
     */
    public boolean isWritable() {
        Path path = Path.of(adapterProperties.getFileUploadPath());
        return Files.isDirectory(path) && Files.isWritable(path);
    }

    private Path resolvePath(String fileName) {
        return Path.of(adapterProperties.getFileUploadPath(), fileName);
    }

    private void evictMetadataIfNeeded() {
        while (metadataMap.size() > MAX_METADATA_ENTRIES) {
            String oldestKey = metadataMap.values().stream()
                    .min(java.util.Comparator.comparing(FileMetadata::uploadTime))
                    .map(FileMetadata::md5)
                    .orElse(null);
            if (oldestKey == null) {
                break;
            }
            metadataMap.remove(oldestKey);
        }
    }

    private String calculateMd5(byte[] content) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    private record FileMetadata(String originalName, String storageName, String path, String md5, Instant uploadTime) {
    }

    private static class ChunkState {

        private final String md5;
        private final String originalName;
        private final long size;
        private final boolean[] uploaded;
        private final String storageName;

        ChunkState(String md5, String originalName, long size, int chunks) {
            this.md5 = md5;
            this.originalName = originalName;
            this.size = size;
            this.uploaded = new boolean[chunks];
            this.storageName = UUID.randomUUID().toString();
        }

        void markUploaded(int chunk) {
            uploaded[chunk] = true;
        }

        boolean isComplete() {
            for (boolean b : uploaded) {
                if (!b) {
                    return false;
                }
            }
            return true;
        }

        String getStorageName() {
            return storageName;
        }
    }
}
