package it.sogei.tabacchi.accertamenti.server.interceptors;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
@Slf4j
@LogMethodInvocation
public class LoggingInterceptor {

    @AroundInvoke
    public Object logMethodEntry(InvocationContext invocationContext) throws Exception {
        Method method = invocationContext.getMethod();

        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        String parameters = this.isLogParametersEnabled(method)
                ? this.paramToString(invocationContext.getParameters())
                : "";

        log.info("[START] {}.{}({})", className, methodName, parameters);

        Object ret = invocationContext.proceed();

        log.info("[ END ] {}.{}()", className, methodName);

        return ret;
    }

    private String paramToString(Object[] params) {
        return Arrays.stream(params)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    private boolean isLogParametersEnabled(Method method) {
        LogMethodInvocation logMethodInvocationAnnotation = method.isAnnotationPresent(LogMethodInvocation.class)
                ? method.getAnnotation(LogMethodInvocation.class)
                : method.getDeclaringClass().getAnnotation(LogMethodInvocation.class);

        return Objects.nonNull(logMethodInvocationAnnotation) && method.getParameterCount() > 0 && logMethodInvocationAnnotation.logParameters();
    }
}
