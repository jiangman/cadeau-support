package com.spldeolin.cadeau.support.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.spldeolin.cadeau.support.util.ConfigUtil;
import com.spldeolin.cadeau.support.util.FileMoveUtil;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import com.spldeolin.cadeau.support.util.StringCaseUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class ServiceGenerator {

    public static void serviceServiceImpl() {
        String[] tableNames = ConfigUtil.getTableNames();
        String[] modelCns = ConfigUtil.getModelCns();
        for (int i = 0; i < tableNames.length; i++) {
            String modelName = StringCaseUtil.snakeToUpperCamel(tableNames[i]);
            String modelCn = modelCns[i];
            service(modelName, modelCn);
            serviceImpl(modelName, modelCn);
        }
    }

    @SneakyThrows
    private static void service(String modelName, String modelCn) {
        ServiceFTL template = new ServiceFTL();
        template.setBasePackage(ConfigUtil.getBasePackage());
        String bussiness = ConfigUtil.getBussiness();
        String bussinessPart;
        if (StringUtils.isBlank(bussiness)) {
            bussinessPart = "";
        } else {
            bussinessPart = "." + bussiness;
        }
        template.setBussinessPart(bussinessPart);
        template.setModelName(modelName);
        template.setModelCn(modelCn);
        template.setBlockComment(ConfigUtil.getBlockComment());
        template.setClassDocEnd(ConfigUtil.getClassDocEnd());
        String derivedService = ConfigUtil.getDerivedService();
        template.setDerivedServiceRef(derivedService);
        String[] parts = derivedService.split("\\.");
        template.setDerivedServiceName(parts[parts.length - 1]);
        template.setPageRef(ConfigUtil.getPage());
        String serviceContent = FreeMarkerUtil.format(true, "service.ftl", template);
        if (ConfigUtil.getOverWrite()) {
            FileUtils.write(new File(ConfigUtil.getServicePath() + modelName + "Service.java"),
                    serviceContent, StandardCharsets.UTF_8);
        } else {
            File f = new File(ConfigUtil.getServicePath() + modelName + "Service.java");
            if (f.exists()) {
                f = FileMoveUtil.renameFile(f, 1);
            }
            FileUtils.write(f, serviceContent, StandardCharsets.UTF_8);
        }
    }

    @SneakyThrows
    private static void serviceImpl(String modelName, String modelCn) {
        ServiceImplFTL template = new ServiceImplFTL();
        template.setBasePackage(ConfigUtil.getBasePackage());
        String bussiness = ConfigUtil.getBussiness();
        String bussinessPart;
        if (StringUtils.isBlank(bussiness)) {
            bussinessPart = "";
        } else {
            bussinessPart = "." + bussiness;
        }
        template.setBussinessPart(bussinessPart);
        template.setModelName(modelName);
        template.setModelCn(modelCn);
        template.setBlockComment(ConfigUtil.getBlockComment());
        template.setClassDocEnd(ConfigUtil.getClassDocEnd());
        String derivedServiceImpl = ConfigUtil.getDerivedServiceImpl();
        template.setDerivedServiceRef(ConfigUtil.getDerivedService());
        template.setDerivedServiceImplRef(derivedServiceImpl);
        String[] parts = derivedServiceImpl.split("\\.");
        template.setDerivedServiceImplName(parts[parts.length - 1]);
        template.setPageRef(ConfigUtil.getPage());
        template.setServiceExceptionRef(ConfigUtil.getServiceException());
        String serviceImplContent = FreeMarkerUtil.format(true, "service-impl.ftl", template);
        if (ConfigUtil.getOverWrite()) {
            FileUtils.write(new File(ConfigUtil.getServiceImplPath() + modelName + "ServiceImpl.java"),
                    serviceImplContent, StandardCharsets.UTF_8);
        } else {
            File f = new File(ConfigUtil.getServiceImplPath() + modelName + "ServiceImpl.java");
            if (f.exists()) {
                f = FileMoveUtil.renameFile(f, 1);
            }
            FileUtils.write(f, serviceImplContent, StandardCharsets.UTF_8);
        }
    }

}
