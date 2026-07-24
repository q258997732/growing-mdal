package bob.growingmdal.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathTraversalValidatorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldAllowPathInsideBaseDir() {
        Path result = PathTraversalValidator.validate(tempDir.toString(), "document.pdf");

        assertThat(result).isAbsolute();
        assertThat(result.toString()).startsWith(tempDir.toString());
    }

    @Test
    void shouldRejectPathEscapingBaseDir() {
        assertThatThrownBy(() -> PathTraversalValidator.validate(tempDir.toString(), "../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Path traversal attempt");
    }

    @Test
    void shouldRejectNullPath() {
        assertThatThrownBy(() -> PathTraversalValidator.validate(tempDir.toString(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userPath must not be blank");
    }

    @Test
    void shouldRejectBlankPath() {
        assertThatThrownBy(() -> PathTraversalValidator.validate(tempDir.toString(), "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userPath must not be blank");
    }

    @Test
    void shouldRejectNullBaseDir() {
        assertThatThrownBy(() -> PathTraversalValidator.validate(null, "document.pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseDir must not be blank");
    }

    @Test
    void shouldAllowNestedPathInsideBaseDir() {
        Path result = PathTraversalValidator.validate(tempDir.toString(), "invoices/2024/jan.pdf");

        assertThat(result.toString()).startsWith(tempDir.toString());
    }
}
