package com.spldeolin.cadeau.support.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.spldeolin.cadeau.support.util.ConfigUtil;
import com.spldeolin.cadeau.support.util.FileMoveUtil;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import com.spldeolin.cadeau.support.util.SingularAndPluralUtil;
import com.spldeolin.cadeau.support.util.StringCaseUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ControllerGenerator {

    public static void controller() {
        String[] tableNames = ConfigUtil.getTableNames();
        String[] modelCns = ConfigUtil.getModelCns();
        for (int i = 0; i < tableNames.length; i++) {
            String modelName = StringCaseUtil.snakeToUpperCamel(tableNames[i]);
            String modelCn = modelCns[i];
            ControllerFTL template = new ControllerFTL().setBasePackage(ConfigUtil.getBasePackage()).setClassDocEnd(
                    ConfigUtil.getClassDocEnd()).setControllerExtraAnnotationPackage(
                    ConfigUtil.getControllerExtraAnnotationPackage()).setRequestResult(
                    ConfigUtil.getRequestResult()).setValidableList(ConfigUtil.getValidableList());
            String bussiness = ConfigUtil.getBussiness();
            String bussinessPart;
            if (StringUtils.isBlank(bussiness)) {
                bussinessPart = "";
            } else {
                bussinessPart = "." + bussiness;
            }

            template.setBussinessPart(bussinessPart).setModelName(modelName).setModelCn(modelCn).setModelSnake(
                    SingularAndPluralUtil.pluralize(StringCaseUtil.camelToSnake(modelName)));
            String controllerContent = FreeMarkerUtil.format(true, "controller.ftl", template);
            try {
                if (ConfigUtil.getOverWrite()) {
                    FileUtils.write(new File(ConfigUtil.getControllerPath() + modelName + "Controller.java"),
                            controllerContent, StandardCharsets.UTF_8);
                } else {
                    File f = new File(ConfigUtil.getControllerPath() + modelName + "Controller.java");
                    if (f.exists()) {
                        f = FileMoveUtil.renameFile(f, 1);
                    }
                    FileUtils.write(f, controllerContent, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                log.error("checked", e);
                throw new RuntimeException();
            }
        }
    }

}
