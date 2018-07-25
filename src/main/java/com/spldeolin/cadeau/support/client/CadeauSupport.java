package com.spldeolin.cadeau.support.client;

import com.spldeolin.cadeau.support.persistence.MybatisGenerator;
import com.spldeolin.cadeau.support.service.ServiceGenerator;
import com.spldeolin.cadeau.support.util.ConfigUtils;

/**
 * @author Deolin
 */
public class CadeauSupport {


    public static void main(String[] args) {
        ConfigUtils.assign();
        MybatisGenerator.daoMapperModel();
        ServiceGenerator.serviceServiceImpl();
//        ControllerGenerator.controller();
//        InputGenerator.input();
    }

}
