package net.xmeter.samplers.assertions;

import lombok.Data;

@Data
public class AssertionsContent {

    private String type;

    private String condition;

    private Boolean enable;

    private String value;

    private String expression;

    private String option;

    private String expect;


}
