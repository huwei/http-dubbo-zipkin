package com.louie.controller;

import com.louie.core.CallResult;
import com.louie.core.ServiceCall;
import com.louie.utils.JsonUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/users/{id}")
    public String getUserById(@PathVariable("id") String uid) {
        return "user:" + uid;
    }

    @GetMapping("/helloworld")
    public String helloworld() {
        return "hello world";
    }
}
