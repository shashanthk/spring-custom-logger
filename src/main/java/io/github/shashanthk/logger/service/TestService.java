package io.github.shashanthk.logger.service;

import io.github.shashanthk.logger.aop.Loggable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    @Loggable
    public List<String> listUsers(String name) {
        return List.of(
                "User 1",
                "User 2",
                "User 3"
        );
    }

}
