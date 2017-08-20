package com.louie.trace;

import com.github.kristofa.brave.http.HttpRequest;
import com.github.kristofa.brave.http.SpanNameProvider;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

/**
 * Spring
 * Created by freeway on 2017/8/20.
 */
public class SpringControllerSpanNameProvider implements SpanNameProvider {

    private HandlerMethod handlerMethod;

    public void setMethod(HandlerMethod handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    @Override
    public String spanName(HttpRequest request) {
        String spanName = "";
        RequestMapping requestMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            if (requestMapping.value().length > 0) {
                spanName = requestMapping.value()[0];
            }
        }

        RequestMapping methodRequestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);

        if (methodRequestMapping.value().length > 0) {
            spanName = spanName + methodRequestMapping.value()[0];
        }

        return spanName;
    }
}
