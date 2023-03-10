package com.example.demotest;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Aspect
@Component
public class ThrottlerAspect {

    private static final Logger logger = LoggerFactory.getLogger(ThrottlerAspect.class);

    private final Throttler ipThrottler;

    public ThrottlerAspect(Throttler ipThrottler) {
        this.ipThrottler = ipThrottler;
    }


    @Pointcut("@annotation(com.example.demotest.Throttling)")
    public void ThrottlingMethod() {
    }

    @Before("ThrottlingMethod()")
    public void throttle() {

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if (servletRequest == null) {
            if (logger.isErrorEnabled()) {
                logger.error("cannot find HttpServletRequest in RequestContextHolder while processing @Throttling annotation ");
            }
        } else {
            boolean isAllowed;

            String path = (String) servletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            if (path.contains("/testIpThrottler/")) {
                final Map pathVariable = (Map) servletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                isAllowed = ipThrottler.throttle((String) pathVariable.get("ip"));
            } else {

                isAllowed = ipThrottler.throttle(servletRequest.getRemoteAddr());
            }
            if (!isAllowed) {
                throw new ThrottlingException();
            }
        }

    }
}