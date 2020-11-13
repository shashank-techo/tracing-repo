package com.demo.microservice.controller;

import id.co.xl.tracing.annotations.NewClassLevelSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@NewClassLevelSpan
class ZipkinController {


    private static final Logger LOG = LoggerFactory.getLogger(ZipkinController.class.getName());

    @GetMapping(value="/service2")
    public String defaultMethod()
    {
        return "microservice 2 executed";
    }
}