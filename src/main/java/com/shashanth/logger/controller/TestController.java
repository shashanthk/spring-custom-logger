package com.shashanth.logger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/users")
    public List<String> getUsers() {

        return List.of(
                "User 1",
                "User 2",
                "User 3"
        );
    }

    @PostMapping("/users")
    public Map<String, Object> saveUser(@RequestBody Map<String, Object> body) {
        return body;
    }

}
