package bob.growingmdal.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import static org.assertj.core.api.Assertions.assertThat;

class TestRestControllerNotLoadedTest {

    @Test
    void shouldBeConditionalOnDebugEnabled() {
        ConditionalOnProperty annotation = TestRestController.class
                .getAnnotation(ConditionalOnProperty.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).containsExactly("debug.enabled");
        assertThat(annotation.havingValue()).isEqualTo("true");
    }
}
