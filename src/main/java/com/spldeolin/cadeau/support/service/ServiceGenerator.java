package com.spldeolin.cadeau.support.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.spldeolin.cadeau.support.util.ConfigUtils;
import com.spldeolin.cadeau.support.util.FileMoveUtils;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import com.spldeolin.cadeau.support.util.StringCaseUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class ServiceGenerator {

    public static void serviceServiceImpl() {
        String[] tableNames = ConfigUtils.getTableNames();
        String[] modelCns = ConfigUtils.getModelCns();
        for (int i = 0; i < tableNames.length; i++) {
            String modelName = StringCaseUtils.snakeToUpperCamel(tableNames[i]);
            String modelCn = modelCns[i];
            service(modelName, modelCn);
            serviceImpl(modelName, modelCn);
        }
    }

    @SneakyThrows
    private static void service(String modelName, String modelCn) {
        ServiceFTL template = new ServiceFTL();
        template.setBasePackage(ConfigUtils.getBasePackage());
        String bussiness = ConfigUtils.getBussiness();
        String bussinessPart;
        if (StringUtils.isBlank(bussiness)) {
            bussinessPart = "";
        } else {
            bussinessPart = "." + bussiness;
        }
        template.setBussinessPart(bussinessPart);
        template.setModelName(modelName);
        template.setModelCn(modelCn);
        template.setClassDocEnd(ConfigUtils.getClassDocEnd());
        String derivedService = ConfigUtils.getCommonService();
        template.setDerivedServiceRef(derivedService);
        String[] parts = derivedService.split("\\.");
        template.setDerivedServiceName(parts[parts.length - 1]);
        template.setPageRef(ConfigUtils.getPage());
        template.setPageParamRef(ConfigUtils.getPageParam());
        String serviceContent = FreeMarkerUtil.format(true, "service.ftl", template);
        if (ConfigUtils.getOverWrite()) {
            FileUtils.write(new File(ConfigUtils.getServicePath() + modelName + "Service.java"),
                    serviceContent, StandardCharsets.UTF_8);
        } else {
            File f = new File(ConfigUtils.getServicePath() + modelName + "Service.java");
            if (f.exists()) {
                f = FileMoveUtils.renameFile(f, 1);
            }
            FileUtils.write(f, serviceContent, StandardCharsets.UTF_8);
        }
    }

    @SneakyThrows
    private static void serviceImpl(String modelName, String modelCn) {
        ServiceImplFTL template = new ServiceImplFTL();
        template.setBasePackage(ConfigUtils.getBasePackage());
        String bussiness = ConfigUtils.getBussiness();
        String bussinessPart;
        if (StringUtils.isBlank(bussiness)) {
            bussinessPart = "";
        } else {
            bussinessPart = "." + bussiness;
        }
        template.setBussinessPart(bussinessPart);
        template.setModelName(modelName);
        template.setModelCn(modelCn);
        template.setClassDocEnd(ConfigUtils.getClassDocEnd());
        String derivedServiceImpl = ConfigUtils.getCommonServiceImpl();
        template.setDerivedServiceRef(ConfigUtils.getCommonService());
        template.setDerivedServiceImplRef(derivedServiceImpl);
        String[] parts = derivedServiceImpl.split("\\.");
        template.setDerivedServiceImplName(parts[parts.length - 1]);
        template.setPageRef(ConfigUtils.getPage());
        template.setPageParamRef(ConfigUtils.getPageParam());
        template.setServiceExceptionRef(ConfigUtils.getServiceException());
        String serviceImplContent = FreeMarkerUtil.format(true, "service-impl.ftl", template);
        if (ConfigUtils.getOverWrite()) {
            FileUtils.write(new File(ConfigUtils.getServiceImplPath() + modelName + "ServiceImpl.java"),
                    serviceImplContent, StandardCharsets.UTF_8);
        } else {
            File f = new File(ConfigUtils.getServiceImplPath() + modelName + "ServiceImpl.java");
            if (f.exists()) {
                f = FileMoveUtils.renameFile(f, 1);
            }
            FileUtils.write(f, serviceImplContent, StandardCharsets.UTF_8);
        }
    }

}
