package com.demo.microservice.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author      shashank shanu
 *
 */
@Data
@Builder
public class DemoModel {
    private String firstField;
    private String secondField;
}
