package com.spldeolin.cadeau.support.client;

import com.spldeolin.cadeau.support.controller.ControllerGenerator;
import com.spldeolin.cadeau.support.input.InputGenerator;
import com.spldeolin.cadeau.support.persistence.MybatisGenerator;
import com.spldeolin.cadeau.support.service.ServiceGenerator;
import com.spldeolin.cadeau.support.util.ConfigUtil;

/**
 * @author Deolin
 */
public class CadeauSupport {

    public static void main(String[] args) throws Exception {
        ConfigUtil.assign();
        MybatisGenerator.daoMapperModel();
        InputGenerator.input();
        ServiceGenerator.serviceServiceImpl();
        ControllerGenerator.controller();



//        DtoGenerator.generateInputVo();
    }

}
