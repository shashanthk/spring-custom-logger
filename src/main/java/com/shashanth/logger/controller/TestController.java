package com.shashanth.logger.controller;

import com.shashanth.logger.util.LogHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @GetMapping("/users")
    public List<String> getUsers() {

        LogHelper.info("Reached to controller");

        return List.of(
                "User 1",
                "User 2",
                "User 3"
        );
    }

}
