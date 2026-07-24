package bob.growingmdal.controller;

import bob.growingmdal.entity.response.ResponseBean;
import bob.growingmdal.entity.response.SuccessResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调试 REST 端点，仅在 debug.enabled=true 时加载。
 */
@Slf4j
@RestController
@ConditionalOnProperty(name = "debug.enabled", havingValue = "true")
public class TestRestController {

    @GetMapping("/test-rest")
    public ResponseEntity<ResponseBean<Object>> testRest() {
        return ResponseEntity.ok(new SuccessResponseBean<>("bob.growingmdal.test rest controller success"));
    }

}
