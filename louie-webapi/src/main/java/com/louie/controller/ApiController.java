package com.louie.controller;

import com.louie.core.CallResult;
import com.louie.core.ServiceCall;
import com.louie.utils.JsonUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ApiController {

    @RequestMapping("/service")
    public String b(HttpServletRequest request) throws InterruptedException {
        ServiceCall call = new ServiceCall(new ServletAdapter(request));
        CallResult cr = call.invoke();
        return JsonUtils.object2JSON(cr);
    }

    @RequestMapping("/users/{id}")
    public String getUserById(@PathVariable("id") String uid) {
        return "user:" + uid;
    }

    @RequestMapping("/helloworld")
    public String helloworld() {
        return "hello world";
    }
}
