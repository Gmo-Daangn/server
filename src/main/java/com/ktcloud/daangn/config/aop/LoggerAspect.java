package com.ktcloud.daangn.config.aop;

import com.ktcloud.daangn.config.aop.trace.LogTrace;
import com.ktcloud.daangn.config.aop.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class LoggerAspect {

    private final LogTrace logTrace;

    @Pointcut("execution(* com.ktcloud.daangn..controller..*(..))")
    private void controllerLayer() {
    }

    @Pointcut("execution(* com.ktcloud.daangn..service..*(..))")
    private void serviceLayer() {
    }

    @Pointcut("execution(* com.ktcloud.daangn..repository..*(..))")
    private void repositoryLayer() {
    }

    @Pointcut("controllerLayer() || serviceLayer() || repositoryLayer()")
    private void applicationLayer() {
    }

    @Around("applicationLayer()")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceStatus status = null;

        try {
            String methodSignature = createMethodSignature(joinPoint);
            status = logTrace.begin(methodSignature);
            Object result = joinPoint.proceed();
            logTrace.end(status);
            return result;
        } catch (Throwable e) {
            logTrace.exception(status, e);
            throw e;
        }
    }

    private String createMethodSignature(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.stream(joinPoint.getArgs())
                .filter(this::shouldLogArgument)
                .map(this::summarizeArgument)
                .collect(Collectors.joining(", "));
        return className + "." + methodName + "(" + args + ")";
    }

    private boolean shouldLogArgument(Object arg) {
        if (arg == null) {
            return false;
        }
        String name = arg.getClass().getName();
        return !name.startsWith("org.springframework.")
                && !name.startsWith("org.apache.")
                && !name.startsWith("jakarta.servlet.")
                && !name.startsWith("javax.servlet.");
    }

    private String summarizeArgument(Object arg) {
        if (arg == null) {
            return "null";
        }

        if (arg instanceof Number || arg instanceof Boolean || arg instanceof Character) {
            return String.valueOf(arg);
        }

        if (arg instanceof String str) {
            return "String(len=" + str.length() + ")";
        }

        if (arg instanceof java.util.Collection<?> col) {
            return arg.getClass().getSimpleName() + "(size=" + col.size() + ")";
        }

        return arg.getClass().getSimpleName();
    }
}
