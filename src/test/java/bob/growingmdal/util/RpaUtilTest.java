package bob.growingmdal.util;

import bob.growingmdal.dto.rpa.RpaRequestBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RpaUtilTest {

    private final ObjectMapper objectMapper = new ObjectMapper().setPropertyNamingStrategy(new PascalCaseNamingStrategy());

    @Test
    void shouldReturnEmptyListForNullResponse() {
        assertThat(RpaUtil.result2KAgentList(null)).isEmpty();
        assertThat(RpaUtil.result2KFlowList(null)).isEmpty();
        assertThat(RpaUtil.result2KSxfAgent(null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyMapForNullResponse() {
        assertThat(RpaUtil.result2KAgentThreadList(null)).isEmpty();
    }

    @Test
    void shouldParseSuccessfulResponse() {
        List<RpaRequestBean> response = List.of(
                new RpaRequestBean("", 4, "{50043442-8A69-4A6B-A8B5-61F882EDE4F3}")
        );
        assertThat(RpaUtil.callFunStatus(response)).isTrue();
    }

    @Test
    void shouldParseFailedResponse() {
        List<RpaRequestBean> response = List.of(
                new RpaRequestBean("error", 4, "{50043442-8A69-4A6B-A8B5-61F882EDE4F3}")
        );
        assertThat(RpaUtil.callFunStatus(response)).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenResponseHasNoAgentData() {
        List<RpaRequestBean> response = Collections.emptyList();
        assertThat(RpaUtil.result2KAgentList(response)).isEmpty();
    }
}
