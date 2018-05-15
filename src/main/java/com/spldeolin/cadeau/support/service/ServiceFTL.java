package com.spldeolin.cadeau.support.service;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ServiceFTL {

    private String basePackage;

    private String bussinessPart;

    private String modelName;

    private String modelCn;

    private String classDocEnd;

    private String derivedServiceRef;

    private String derivedServiceName;

    private String pageRef;

}
