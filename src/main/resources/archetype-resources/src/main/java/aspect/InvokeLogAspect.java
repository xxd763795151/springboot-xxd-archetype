package ${package}.aspect;

import ${package}.aspect.annotation.InvokeLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Aspect
@Component
public class InvokeLogAspect {

    private Map<String, String> descMap = new HashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    @Pointcut("@annotation(${package}.aspect.annotation.InvokeLog) " +
//            "|| within(${package}.service.impl.*) " +
            "|| @within(${package}.aspect.annotation.InvokeLog) ")
    private void pointcut() {

    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        StringBuilder params = new StringBuilder("[");
        try {
            String methodName = getMethodFullName(joinPoint.getTarget().getClass(), joinPoint.getSignature().getName());
            if (!descMap.containsKey(methodName)) {
                cacheDescInfo(joinPoint);
            }

            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                params.append(args[i]);
            }
            params.append("]");
            log.info("{}方法调用开始，入参：{}", descMap.get(methodName), params);

            long startTime = System.currentTimeMillis();
            Object res = joinPoint.proceed();
            long endTime = System.currentTimeMillis();

            log.info("{}方法调用结束，出参：{}，耗时{}", descMap.get(methodName), res, endTime - startTime);
            return res;
        } catch (Throwable e) {
            log.error("调用方法异常， 入参：" + params, e);
            throw e;
        }
    }

    private void cacheDescInfo(ProceedingJoinPoint joinPoint) {
        try {
            lock.lock();
            String methodName = joinPoint.getSignature().getName();
            Class<?> aClass = joinPoint.getTarget().getClass();

            Method method = null;
            try {
                Object[] args = joinPoint.getArgs();

                Class<?>[] clzArr = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    clzArr[i] = args[i].getClass();
                }
                method = aClass.getDeclaredMethod(methodName, clzArr);

            } catch (NoSuchMethodException e) {
                log.warn("cacheDescInfo error: {}", e.getMessage());
            }

            String fullMethodName = getMethodFullName(aClass, methodName);
            String desc = "[" + fullMethodName + "]";
            if (method == null) {
                descMap.put(fullMethodName, desc);
                return;
            }

            // 这个注解可以用类，也可以用在方法上。同时切面拦截的有service层的代码，没有这个注解
            // 所以做个判断，如果没有注解，直接设置方法全名
            InvokeLog invokeLog = method.getAnnotation(InvokeLog.class);
            if (invokeLog != null) {
                String value = invokeLog.value();
                if (StringUtils.isBlank(value)) {
                    descMap.put(fullMethodName, desc);
                } else {
                    descMap.put(fullMethodName, value + desc);
                }
            } else {
                descMap.put(fullMethodName, desc);
            }
        } finally {
            lock.unlock();
        }
    }

    private String getMethodFullName(Class<?> clz, String methodName) {
        return clz.getSimpleName() + "#" + methodName;
    }
}
