package ${package}.aspect;

import ${package}.aspect.annotation.ControllerLog;
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
public class ControllerLogAspect {

    private Map<String, String> descMap = new HashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    @Pointcut("@annotation(${package}.aspect.annotation.ControllerLog)")
    private void pointcut() {

    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        StringBuilder params = new StringBuilder("[");
        try {
            String methodName = getMethodFullName(joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());

            log.info("请求调用开始，{}", methodName);

            if (!descMap.containsKey(methodName)) {
                cacheDescInfo(joinPoint);
            }

            Object[] args = joinPoint.getArgs();
            long startTime = System.currentTimeMillis();
            Object res = joinPoint.proceed();
            long endTime = System.currentTimeMillis();

            for (int i = 0; i < args.length; i++) {
                params.append(args[i]);
            }
            params.append("]");

            String resStr = "[" + res.toString() + "]";

            StringBuilder sb = new StringBuilder();
            sb.append(descMap.get(methodName))
                    .append("调用完成: ")
                    .append("请求参数=").append(params).append(", ")
                    .append("响应值=").append(resStr).append(", ")
                    .append("耗时=").append(endTime - startTime);
            log.info(sb.toString());
            log.info("请求调用结束，{}", methodName);
            return res;
        } catch (Throwable e) {
            log.error("调用方法异常， 请求参数：" + params, e);
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
                log.error("cacheDescInfo error", e);
            }

            String fullMethodName = getMethodFullName(aClass.getName(), methodName);
            String desc = "[" + fullMethodName + "]";
            if (method == null) {
                descMap.put(fullMethodName, desc);
                return;
            }

            ControllerLog controllerLog = method.getAnnotation(ControllerLog.class);
            String value = controllerLog.value();
            if (StringUtils.isBlank(value)) {
                descMap.put(fullMethodName, desc);
            } else {
                descMap.put(fullMethodName, value + desc);
            }
        } finally {
            lock.unlock();
        }
    }

    private String getMethodFullName(String className, String methodName) {
        return className + "#" + methodName;
    }
}
