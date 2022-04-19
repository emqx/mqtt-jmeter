package net.xmeter.samplers.assertions;

import lombok.Data;

import java.util.List;

@Data
public class Assertions {

    private String timeOut;

    private String type;

    private String filterType;

    private List<AssertionsContent> list;



}
