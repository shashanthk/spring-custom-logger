package io.github.shashanthk.logger.controller;

import io.github.shashanthk.logger.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/users")
    public List<String> getUsers() {
        return testService.listUsers("Shashanth");
    }

    @PostMapping("/users")
    public Map<String, Object> saveUser(@RequestBody Map<String, Object> body) {
        return body;
    }

}
