package com.spldeolin.cadeau.support.controller;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ControllerFTL {

    private String basePackage;

    private String modelName;

    private String modelCn;

    private String classDocEnd;

    private String pageRef;

    private String pageParamRef;

}
