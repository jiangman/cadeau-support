package com.spldeolin.cadeau.support.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import com.spldeolin.cadeau.support.util.ProjectProperties;
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
        ProjectProperties properties = ProjectProperties.instance();
        String[] tableNames = properties.getTableNames();
        String[] modelCns = properties.getModelCns();
        for (int i = 0; i < tableNames.length; i++) {
            String modelName = StringCaseUtils.snakeToUpperCamel(tableNames[i]);
            String modelCn = modelCns[i];
            service(modelName, modelCn);
            serviceImpl(modelName, modelCn);
        }
    }

    @SneakyThrows
    private static void service(String modelName, String modelCn) {
        ProjectProperties properties = ProjectProperties.instance();
        ServiceFTL template = new ServiceFTL();
        template.setBasePackage(properties.getBasePackage());
        template.setModelName(modelName);
        template.setModelCn(modelCn);
        template.setClassDocEnd(properties.getClassDocEnd());
        String derivedService = properties.getCommonService();
        template.setDerivedServiceRef(derivedService);
        String[] parts = derivedService.split("\\.");
        template.setDerivedServiceName(parts[parts.length - 1]);
        template.setPageRef(properties.getPage());
        template.setPageParamRef(properties.getPageParam());
        String serviceContent = FreeMarkerUtil.format(true, "service.ftl", template);
        if (properties.getOverWrite()) {
            FileUtils.write(new File(properties.getServicePath() + modelName + "Service.java"),
                    serviceContent, StandardCharsets.UTF_8);
        } else {
            File f = new File(properties.getServicePath() + modelName + "Service.java");
            if (f.exists()) {
                f = FileMoveUtils.renameFile(f, 1);
            }
            FileUtils.write(f, serviceContent, StandardCharsets.UTF_8);
        }
    }

    @SneakyThrows
    private static void serviceImpl(String modelName, String modelCn) {
        ProjectProperties properties = ProjectProperties.instance();
        ServiceImplFTL template = new ServiceImplFTL();
        template.setBasePackage(properties.getBasePackage());
        template.setModelName(modelName);
        template.setModelCn(modelCn);
        template.setClassDocEnd(properties.getClassDocEnd());
        String derivedServiceImpl = properties.getCommonServiceImpl();
        template.setDerivedServiceRef(properties.getCommonService());
        template.setDerivedServiceImplRef(derivedServiceImpl);
        String[] parts = derivedServiceImpl.split("\\.");
        template.setDerivedServiceImplName(parts[parts.length - 1]);
        template.setPageRef(properties.getPage());
        template.setPageParamRef(properties.getPageParam());
        template.setServiceExceptionRef(properties.getServiceException());
        String serviceImplContent = FreeMarkerUtil.format(true, "service-impl.ftl", template);
        if (properties.getOverWrite()) {
            FileUtils.write(new File(properties.getServiceImplPath() + modelName + "ServiceImpl.java"),
                    serviceImplContent, StandardCharsets.UTF_8);
        } else {
            File f = new File(properties.getServiceImplPath() + modelName + "ServiceImpl.java");
            if (f.exists()) {
                f = FileMoveUtils.renameFile(f, 1);
            }
            FileUtils.write(f, serviceImplContent, StandardCharsets.UTF_8);
        }
    }

}
