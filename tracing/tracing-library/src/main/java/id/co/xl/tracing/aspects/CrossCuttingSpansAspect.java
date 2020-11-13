package id.co.xl.tracing.aspects;

import brave.Span;
import brave.Tracer;
import id.co.xl.tracing.annotations.NewClassLevelSpan;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;


/**
 * @author  shashank shanu
 *
 * */
@Aspect
@Configuration
public class CrossCuttingSpansAspect {

    private static final Logger LOG = LoggerFactory.getLogger(CrossCuttingSpansAspect.class);

    @Autowired
    private Tracer tracer;


  /**
   * This method( an @Around advice) which will create a new span for the current trace before the
   * execution of public methods of the class on which {@link NewClassLevelSpan} is annotated.
   *
   * After the execution of the respective method, this method will log the class name, method name,
   * parameters types & parameters values, response object and execution time of the method along with [application name, trace-id, span-id, zipkin-flag]
   *
   * */
  @Around("within(@id.co.xl.tracing.annotations.NewClassLevelSpan *)")
  public Object joinPoints(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
      StringBuilder parameters = new StringBuilder();
      MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
      Object []parameterType = methodSignature.getParameterTypes();
      int counter = 0;
      for(Object obj:proceedingJoinPoint.getArgs()){
          parameters.append(parameters.length()==0?"(type : "+((Class) parameterType[counter]).getName()+" ) "+obj:", (type : "+((Class) parameterType[counter]).getName()+" ) "+obj);
          counter++;
      }
      String className = methodSignature.getDeclaringType().getSimpleName();
      String methodName = methodSignature.getName();
      Span newSpan = tracer.nextSpan().name(proceedingJoinPoint.getSignature().getName())
              .tag("canonicalname",proceedingJoinPoint.getSignature().getDeclaringType().getCanonicalName())
              .tag("class",className)
              .tag("method",methodName)
              .tag("parameters", parameters.toString()).start();
      Object response = new Object();
      try (Tracer.SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
          StopWatch stopWatch = new StopWatch(className + "." + methodName);
          stopWatch.start(methodName);
          response = proceedingJoinPoint.proceed();// now the execution the method will start
          stopWatch.stop();
          LOG.info("class : {} , method : {} , parameters : {} , response : {} , execution time : {} ns", className, methodName, parameters.toString(),response, stopWatch.getTotalTimeNanos());
      } finally {
          newSpan.finish();
      }
      return response;
    }
}
