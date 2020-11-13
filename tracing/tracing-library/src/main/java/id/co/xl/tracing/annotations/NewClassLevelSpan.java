package id.co.xl.tracing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.core.annotation.AliasFor;

/**
 * Allows to create a new span around a class's public method. The new span will be either a child
 * of an existing span if a trace is already in progress or a new span will be created if
 * there was no previous trace.
 * <p>
 * Classes can be annotated with {@link SpanTag}, which will end in adding the
 * parameter value as a tag value to the span. The tag key will be the value of the
 * {@code key} annotation from {@link SpanTag}.
 *
 * It is suggested to use this annotation wisely, as this annotation is being used as a pointcut for
 * aspect defined for tracing, which is costly operation.
 *
 * @author mukulbansal
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface NewClassLevelSpan {

    /**
     * @return - The name of the span which will be created. Default is the annotated
     * method's name separated by hyphens.
     */
    @AliasFor("value")
    String name() default "";

    /**
     * @return - The name of the span which will be created. Default is the annotated
     * method's name separated by hyphens.
     */
    @AliasFor("name")
    String value() default "";

}
