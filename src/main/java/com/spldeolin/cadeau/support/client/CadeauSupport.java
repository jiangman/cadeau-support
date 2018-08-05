package com.spldeolin.cadeau.support.client;

import com.spldeolin.cadeau.support.controller.ControllerGenerator;
import com.spldeolin.cadeau.support.input.InputGenerator;
import com.spldeolin.cadeau.support.persistence.MybatisGenerator;
import com.spldeolin.cadeau.support.service.ServiceGenerator;

/**
 * @author Deolin
 */
public class CadeauSupport {

    public static void main(String[] args) {
        MybatisGenerator.daoMapperModel();
        ServiceGenerator.serviceServiceImpl();
        ControllerGenerator.controller();
        InputGenerator.input();
    }

}