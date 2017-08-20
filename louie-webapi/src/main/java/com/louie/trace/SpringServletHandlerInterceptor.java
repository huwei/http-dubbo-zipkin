package com.louie.trace;

import com.github.kristofa.brave.*;
import com.github.kristofa.brave.http.*;
import com.github.kristofa.brave.internal.Nullable;
import com.github.kristofa.brave.servlet.ServletHttpServerRequest;
import com.github.kristofa.brave.servlet.internal.MaybeAddClientAddressFromRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

import static com.github.kristofa.brave.internal.Util.checkNotNull;

@Configuration
public class SpringServletHandlerInterceptor extends HandlerInterceptorAdapter {

    static final String HTTP_SERVER_SPAN_ATTRIBUTE = SpringServletHandlerInterceptor.class.getName() + ".server-span";

    /**
     * Creates a tracing interceptor with defaults. Use {@link #builder(Brave)} to customize.
     */
    public static SpringServletHandlerInterceptor create(Brave brave) {
        return new Builder(brave).build();
    }

    public static Builder builder(Brave brave) {
        return new Builder(brave);
    }

    public static final class Builder {
        final Brave brave;
        Builder(Brave brave) { // intentionally hidden
            this.brave = checkNotNull(brave, "brave");
        }

        public SpringServletHandlerInterceptor build() {
            return new SpringServletHandlerInterceptor(this);
        }
    }

    private final ServerRequestInterceptor requestInterceptor;
    private final ServerResponseInterceptor responseInterceptor;
    private final ServerSpanThreadBinder serverThreadBinder;
    @Nullable // while deprecated constructor is in use
    private final MaybeAddClientAddressFromRequest maybeAddClientAddressFromRequest;

    @Autowired
        // internal
    SpringServletHandlerInterceptor(Brave brave) {
        this(builder(brave));
    }

    SpringServletHandlerInterceptor(Builder b) { // intentionally hidden
        this.requestInterceptor = b.brave.serverRequestInterceptor();
        this.responseInterceptor = b.brave.serverResponseInterceptor();
        this.serverThreadBinder = b.brave.serverSpanThreadBinder();
        this.maybeAddClientAddressFromRequest = MaybeAddClientAddressFromRequest.create(b.brave);
    }

    /**
     * @deprecated please use {@link #create(Brave)} or {@link #builder(Brave)}
     */
    @Deprecated
    public SpringServletHandlerInterceptor(ServerRequestInterceptor requestInterceptor, ServerResponseInterceptor responseInterceptor,
                                           final ServerSpanThreadBinder serverThreadBinder) {
        this.requestInterceptor = requestInterceptor;
        this.responseInterceptor = responseInterceptor;
        this.serverThreadBinder = serverThreadBinder;
        this.maybeAddClientAddressFromRequest = null;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if (request.getAttribute(HTTP_SERVER_SPAN_ATTRIBUTE) != null) return true; // already handled

        SpringControllerSpanNameProvider spanNameProvider = new SpringControllerSpanNameProvider();
        spanNameProvider.setMethod((HandlerMethod)handler);
        requestInterceptor.handle(new SpringHttpServerRequestAdapter(new ServletHttpServerRequest(request), spanNameProvider));
        if (maybeAddClientAddressFromRequest != null) {
            maybeAddClientAddressFromRequest.accept(request);
        }
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        request.setAttribute(HTTP_SERVER_SPAN_ATTRIBUTE, serverThreadBinder.getCurrentServerSpan());
        serverThreadBinder.setCurrentSpan(ServerSpan.EMPTY);
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex) {

        final ServerSpan span = (ServerSpan) request.getAttribute(HTTP_SERVER_SPAN_ATTRIBUTE);

        if (span != null) {
            serverThreadBinder.setCurrentSpan(span);
        }

        responseInterceptor.handle(new HttpServerResponseAdapter(new HttpResponse() {
            public int getHttpStatusCode() {
                return response.getStatus();
            }
        }));
    }

}
