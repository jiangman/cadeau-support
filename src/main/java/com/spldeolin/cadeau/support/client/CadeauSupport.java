package com.spldeolin.cadeau.support.client;

import com.spldeolin.cadeau.support.service.ServiceGenerator;
import com.spldeolin.cadeau.support.util.ConfigUtil;

/**
 * @author Deolin
 */
public class CadeauSupport {

    public static void main(String[] args) {
        ConfigUtil.assign();
        //MybatisGenerator.daoMapperModel();
        //InputGenerator.input();
        ServiceGenerator.serviceServiceImpl();
        //ControllerGenerator.controller();



//        DtoGenerator.generateInputVo();
    }

}
