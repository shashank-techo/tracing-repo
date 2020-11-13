package com.demo.microservice.controller;

import com.demo.microservice.model.DemoModel;
import com.demo.microservice.service.ZipkinService;
import id.co.xl.tracing.annotations.NewClassLevelSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


/**
 * @author  shashank shanu
 *
 * */
@RestController
@NewClassLevelSpan
class ZipkinController{

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ZipkinService zipkinService;

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    private static final Logger LOG = LoggerFactory.getLogger(ZipkinController.class);

    /**
     * sample API to show creation of traces & spans
     *
     **/
    @GetMapping(value="/default-api")
    public ResponseEntity<String> defaultMethod()
    {
        checkingOut();
        zipkinService.checkServiceMethod();
        String response = (String) restTemplate.exchange("http://localhost:8082/service2",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {}).getBody();
        zipkinService.withParameters("param1",2,DemoModel.builder().firstField("first").secondField("seconds").build());
        zipkinService.checkServiceMethod();
        zipkinService.withResponseObj();
        return new ResponseEntity(response, HttpStatus.OK);
    }

    private void checkingOut(){
        zipkinService.withResponse("param1",2, DemoModel.builder().firstField("first").secondField("seconds").build());
    }

}