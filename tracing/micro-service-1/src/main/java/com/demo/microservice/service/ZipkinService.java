package com.demo.microservice.service;

import com.demo.microservice.model.DemoModel;
import id.co.xl.tracing.annotations.NewClassLevelSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author    shashank shanu
 *
 * */
@Service
@NewClassLevelSpan
public class ZipkinService {

    private static final Logger LOG = LoggerFactory.getLogger(ZipkinService.class);

    /**
     * sample method; used thread.sleep() in order to check
     * the execution of the method
     *
     * */
    public void checkServiceMethod(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * sample method
     * @param       parameter1
     * @param       parameter2
     * @param       demoModel
     *
     * */
    public void withParameters(String parameter1, int parameter2, DemoModel demoModel){
    }

    /**
     * sample method
     * @param       parameter1
     * @param       parameter2
     * @param       demoModel
     *
     * @return      DemoModel
     * */
    public DemoModel withResponse(String parameter1, int parameter2, DemoModel demoModel){
        return DemoModel.builder().firstField("responseFirstField").build();
    }


    /**
     * sample method
     *
     * @return      DemoModel
     * */
    public DemoModel withResponseObj(){
        return DemoModel.builder().firstField("responseFirstField").secondField("responseSecondField").build();
    }
}
