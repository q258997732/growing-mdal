package bob.growingmdal.controller;

import bob.growingmdal.entity.response.ResponseBean;
import bob.growingmdal.entity.response.SuccessResponseBean;
import bob.growingmdal.handler.HardwareWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

    @CrossOrigin(origins = "*")
    @GetMapping("/test-rest")
    public ResponseEntity<ResponseBean<Object>> testRest(){
        return ResponseEntity.ok(new SuccessResponseBean<>( "bob.growingmdal.test rest controller success"));
    }

    @GetMapping("/send/{message}")
    public String sendMessage(@PathVariable String message) {
        HardwareWebSocketHandler.broadcast("Server push: " + message);
        return "message push all: " + message;
    }

}
