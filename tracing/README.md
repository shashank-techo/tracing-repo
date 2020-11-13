# Tracing Microservice(s)

### Use case
Sample microservices to show case tracing of different requests through the microservices. The requests can be traced using trace-id & span-id which is generated using sleuth libraries,
and the traces are logged using libraries of AOP. 

### Contents
1. [micro-service-1](https://github.com/Techolution/xlaxiata/tree/master/tracing/micro-service-1) (based on Spring boot)
2. [micro-service-2](https://github.com/Techolution/xlaxiata/tree/master/tracing/micro-service-2) (based on Spring boot)

### Dependencies 
Apart from regular spring-boot & spring web dependencies, an additional dependency for sleuth has been included.
```maven
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-sleuth</artifactId>
			<version>2.2.5.RELEASE</version>
		</dependency>
```

### Configuration to handle concerns related to traces
The [CrossCuttingSpan.java](https://github.com/Techolution/xlaxiata/blob/master/tracing/micro-service-1/src/main/java/com/demo/microservice/config/CrossCuttingSpans.java) class has been used
as a configuration for aspects for creating new spans( for execution of every method which satisfies the conditions defined in the @Around advice) and logging the service-name, trace-id, span-id,
class name, method name, method parameters, method response, execution time of the respective method.

```java
package com.demo.microservice.config;

import brave.Span;
import brave.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
public class CrossCuttingSpans {

    private static final Logger LOG = LoggerFactory.getLogger(CrossCuttingSpans.class.getName());

    @Autowired

    private Tracer tracer;


  /**
   * methods using annotations which participate in the process of bean weaving should be ignored
   * as it might result in conflicts while bean weaving
   *
   * */
  @Pointcut(
          " @annotation(org.springframework.context.annotation.Bean)"
          + "|| @annotation(org.aspectj.lang.annotation.Around)"
          + "|| @annotation(org.aspectj.lang.annotation.Pointcut)"
          + "|| @annotation(org.aspectj.lang.annotation.Aspect)")
  private void ignoredAnnotations(){}


  /**
   * this method( an @Around advice) which will create a new span for the current trace before the
   * execution of any method satisfying the conditions passed as parameters of the @Around annotation.
   * The conditions include execution of every method which comes under the package referred inside within designator
   * and also it'll ignore the methods annotated with the annotations provided in ignoredAnnotations() pointcut.
   *
   * After the execution of the respective method, this method will log the class name, method name,
   * parameters passed, response object and execution time of the method along with [application name, trace-id, span-id, zipkin-flag]
   *
   *
   * */
  @Around("execution(* *(..)) && within(com.demo.microservice..*)&&  !ignoredAnnotations()")
  public Object joinPoints(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
      StringBuilder parameters = new StringBuilder();
      MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
      for(Object obj:proceedingJoinPoint.getArgs()){
              parameters.append(parameters.length()==0?obj:", "+obj);
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
          LOG.info("class : {} , method : {} , parameters : {} , response : {} , execution time : {}", className, methodName, parameters.toString(),response, stopWatch.shortSummary());
        } finally {
            newSpan.finish();
        }
      return response;
    }
}

```

###See the logs
1. Run both of the microservices(micro-service-1 & micro-service-2), micro-service-1 runs on port 8081 and micro-service-2 runs on port 8082
2. Hit the API http://localhost:8081/default-api
3. In the console of your IDE, you can see the logs as per the following format<br />
   a. micro-service-1
    ```logs
    2020-11-12 18:47:56.929  INFO [zipkin-server1,20c7b73765734f66,91a0a431e38f2f44,true] 33508 --- [nio-8081-exec-1] c.d.m.config.CrossCuttingSpans           : class : ZipkinService , method : checkServiceMethod , parameters :  , response : null , execution time : StopWatch 'ZipkinService.checkServiceMethod': running time = 1000969000 ns
    2020-11-12 18:47:56.930  INFO [zipkin-server1,20c7b73765734f66,5afe9578574e9b9a,true] 33508 --- [nio-8081-exec-1] c.d.m.config.CrossCuttingSpans           : class : ZipkinService , method : withResponseObj , parameters :  , response : DemoModel(firstField=responseFirstField, secondField=responseSecondField) , execution time : StopWatch 'ZipkinService.withResponseObj': running time = 74900 ns
    2020-11-12 18:47:56.931  INFO [zipkin-server1,20c7b73765734f66,f86b53149dd2306d,true] 33508 --- [nio-8081-exec-1] c.d.m.config.CrossCuttingSpans           : class : ZipkinController , method : defaultMethod , parameters :  , response : <200 OK OK,microservice 2 executed,[]> , execution time : StopWatch 'ZipkinController.defaultMethod': running time = 2500712200 ns
                                          |               |                |
                                    service-name       trace-id         span-id
    ```
    a. micro-service-2
    ```logs
    2020-11-12 18:47:55.867  INFO [zipkin-server2,20c7b73765734f66,fe0648436398bff7,true] 27108 --- [nio-8082-exec-1] c.d.m.controller.ZipkinController        : Inside microservice 2..
    2020-11-12 18:47:55.867  INFO [zipkin-server2,20c7b73765734f66,fe0648436398bff7,true] 27108 --- [nio-8082-exec-1] c.d.m.config.CrossCuttingSpans           : class : ZipkinController , method : defaultMethod , parameters :  , response : microservice 2 executed , execution time : StopWatch 'ZipkinController.defaultMethod': running time = 9350100 ns
                                          |               |                |
                                    service-name       trace-id         span-id
    ```
    
    The trace-id will remain same across a request.
    
 
    

