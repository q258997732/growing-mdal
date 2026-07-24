package bob.growingmdal.entity.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NantianCameraResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeToJson() throws Exception {
        NantianCameraResponse response = new NantianCameraResponse(200, "success", "base64data");

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"code\":200");
        assertThat(json).contains("\"message\":\"success\"");
        assertThat(json).contains("\"data\":\"base64data\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = "{\"code\":500,\"message\":\"error\",\"data\":\"details\"}";

        NantianCameraResponse response = objectMapper.readValue(json, NantianCameraResponse.class);

        assertThat(response.getCode()).isEqualTo(500);
        assertThat(response.getMessage()).isEqualTo("error");
        assertThat(response.getData()).isEqualTo("details");
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    void shouldReportSuccessForCode200() {
        NantianCameraResponse response = new NantianCameraResponse(200, "ok", "");

        assertThat(response.isSuccess()).isTrue();
    }
}
