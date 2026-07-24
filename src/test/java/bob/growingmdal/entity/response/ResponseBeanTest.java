package bob.growingmdal.entity.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseBeanTest {

    @Test
    void successBeanShouldHaveDefaultValues() {
        SuccessResponseBean<String> bean = new SuccessResponseBean<>("data");
        assertThat(bean.getCode()).isEqualTo(200);
        assertThat(bean.getMessage()).isEqualTo("Success");
        assertThat(bean.getData()).isEqualTo("data");
    }

    @Test
    void successBeanShouldAcceptCustomMessage() {
        SuccessResponseBean<String> bean = new SuccessResponseBean<>("Done", "data");
        assertThat(bean.getCode()).isEqualTo(200);
        assertThat(bean.getMessage()).isEqualTo("Done");
    }

    @Test
    void errorBeanShouldHaveDefaultValues() {
        ErrorResponseBean<String> bean = new ErrorResponseBean<>("error");
        assertThat(bean.getCode()).isEqualTo(400);
        assertThat(bean.getMessage()).isEqualTo("error");
        assertThat(bean.getData()).isNull();
    }

    @Test
    void errorBeanShouldAcceptCodeAndData() {
        ErrorResponseBean<String> bean = new ErrorResponseBean<>(500, "server error", "details");
        assertThat(bean.getCode()).isEqualTo(500);
        assertThat(bean.getData()).isEqualTo("details");
    }

    @Test
    void commandResponseShouldOmitNullFields() throws Exception {
        CommandResponse response = new CommandResponse("OutPut", "Printer", "Print", true, null, null, null);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(response);
        assertThat(json).contains("success");
        assertThat(json).doesNotContain("\"data\"");
    }
}
