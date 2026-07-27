package bob.growingmdal.connector;

import bob.growingmdal.config.AdapterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadStoreTest {

    @Mock
    private AdapterProperties adapterProperties;

    private FileUploadStore store;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        when(adapterProperties.getFileUploadPath()).thenReturn(tempDir.toString());
        store = new FileUploadStore(adapterProperties);
        store.init();
    }

    @Test
    void shouldSaveSmallFile(@TempDir Path tempDir) throws IOException {
        String content = "hello world";
        String base64 = Base64.getEncoder().encodeToString(content.getBytes());

        String storageName = store.saveFile("test.txt", base64);

        assertThat(storageName).isNotBlank();
        Path saved = tempDir.resolve(storageName);
        assertThat(Files.readString(saved)).isEqualTo(content);
    }

    @Test
    void shouldSaveChunksAndMerge(@TempDir Path tempDir) throws IOException {
        String part1 = "hello ";
        String part2 = "world";
        String fullContent = part1 + part2;
        byte[] fullBytes = fullContent.getBytes();
        String md5 = md5(fullBytes);

        boolean first = store.saveChunk(md5, "test.txt", fullBytes.length, 2, 0,
                part1.getBytes().length, Base64.getEncoder().encodeToString(part1.getBytes()));
        boolean second = store.saveChunk(md5, "test.txt", fullBytes.length, 2, 1,
                part1.getBytes().length, Base64.getEncoder().encodeToString(part2.getBytes()));

        assertThat(first).isFalse();
        assertThat(second).isTrue();
        assertThat(store.existsByMd5(md5)).isTrue();
    }

    @Test
    void shouldCheckMd5Exists() throws IOException {
        String content = "exists";
        String base64 = Base64.getEncoder().encodeToString(content.getBytes());
        String storageName = store.saveFile("exists.txt", base64);

        boolean exists = store.existsByMd5(md5(content.getBytes()));

        assertThat(exists).isTrue();
        assertThat(store.getOriginalName(storageName)).isEqualTo("exists.txt");
    }

    @Test
    void shouldReportWritable(@TempDir Path tempDir) {
        assertThat(store.isWritable()).isTrue();
    }

    private String md5(byte[] content) {
        try {
            java.security.MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
