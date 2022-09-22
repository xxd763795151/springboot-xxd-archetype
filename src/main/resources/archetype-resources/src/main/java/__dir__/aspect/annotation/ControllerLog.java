package ${package}.aspect.annotation;

import java.lang.annotation.*;

/**
 * 该注解用到controller层的方法上
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ControllerLog {

    String value() default "";
}
