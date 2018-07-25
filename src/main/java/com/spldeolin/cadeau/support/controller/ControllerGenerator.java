package com.spldeolin.cadeau.support.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import com.spldeolin.cadeau.support.util.ConfigUtils;
import com.spldeolin.cadeau.support.util.FileMoveUtils;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import com.spldeolin.cadeau.support.util.StringCaseUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class ControllerGenerator {

    @SneakyThrows
    public static void controller() {
        String[] tableNames = ConfigUtils.getTableNames();
        String[] modelCns = ConfigUtils.getModelCns();
        for (int i = 0; i < tableNames.length; i++) {
            String modelName = StringCaseUtils.snakeToUpperCamel(tableNames[i]);
            String modelCn = modelCns[i];
            ControllerFTL template = new ControllerFTL();
            template.setBasePackage(ConfigUtils.getBasePackage());
            template.setClassDocEnd(ConfigUtils.getClassDocEnd());
            template.setModelName(modelName);
            template.setModelCn(modelCn);
            template.setPageRef(ConfigUtils.getPage());
            template.setPageParamRef(ConfigUtils.getPageParam());
            String controllerContent = FreeMarkerUtil.format(true, "controller.ftl", template);
            if (ConfigUtils.getOverWrite()) {
                FileUtils.write(new File(ConfigUtils.getControllerPath() + modelName + "Controller.java"),
                        controllerContent, StandardCharsets.UTF_8);
            } else {
                File f = new File(ConfigUtils.getControllerPath() + modelName + "Controller.java");
                if (f.exists()) {
                    f = FileMoveUtils.renameFile(f, 1);
                }
                FileUtils.write(f, controllerContent, StandardCharsets.UTF_8);
            }
        }
    }

}
