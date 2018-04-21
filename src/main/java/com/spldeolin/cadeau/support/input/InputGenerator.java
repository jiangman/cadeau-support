package com.spldeolin.cadeau.support.input;

import static com.spldeolin.cadeau.support.util.ConstantUtil.ftlPath;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import com.spldeolin.cadeau.support.util.ConfigUtil;
import com.spldeolin.cadeau.support.util.FileMoveUtil;
import com.spldeolin.cadeau.support.util.FileParseUtil;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import com.spldeolin.cadeau.support.util.StringCaseUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class InputGenerator {

    @SneakyThrows
    public static void input() {
        File ftlFolder = new File(ftlPath);
        Iterator<File> iterator = FileUtils.iterateFiles(ftlFolder, new String[] {"ftl"}, true);
        List<InputFTL> inputTemplates = new ArrayList<>();
        // 遍历field片段文件
        while (iterator.hasNext()) {
            File ftl = iterator.next();
            String index = FileParseUtil.fileName(ftl).split("#")[0].split("_")[0];
            String model = FileParseUtil.fileName(ftl).split("#")[0].split("_")[1];
            inputTemplates.add(
                    new InputFTL().setIndex(Integer.parseInt(index)).setInputPackage(
                            ConfigUtil.getInputPackage()).setTextOption(
                            ConfigUtil.getTextOption()).setDescription("“" +
                            getModelCnsByModel(model) + "”Input类").setModelPackage(
                            ConfigUtil.getModelPackage()).setModel(model));
        }
        // 去重与排序
        inputTemplates = inputTemplates.stream().distinct().collect(Collectors.toList());
        Collections.sort(inputTemplates);
        // 重新遍历组装inputTemplate
        for (InputFTL inputTemplate : inputTemplates) {
            List<String> fieldFileNames = new ArrayList<>();
            iterator = FileUtils.iterateFiles(ftlFolder, new String[] {"ftl"}, true);
            while (iterator.hasNext()) {
                File ftl = iterator.next();
                String fileName = FileParseUtil.fileName(ftl);
                if (inputTemplate.getModel().equals(fileName.split("#")[0].split("_")[1])) {
                    fieldFileNames.add(fileName);
                }
            }
            inputTemplate.setFieldFileNames(fieldFileNames);
        }
        // 生成Java文件
        for (InputFTL inputTemplate : inputTemplates) {
            String content = FreeMarkerUtil.format(true, "input-template.ftl", inputTemplate);
            if (ConfigUtil.getOverWrite()) {
                FileUtils.write(new File(ConfigUtil.getInputPath() + inputTemplate.getModel() + "Input.java"),
                        content, StandardCharsets.UTF_8);
            } else {
                File f = new File(ConfigUtil.getInputPath() + inputTemplate.getModel() + "Input.java");
                if (f.exists()) {
                    f = FileMoveUtil.renameFile(f, 1);
                }
                FileUtils.write(f, content, StandardCharsets.UTF_8);
            }
        }
        // 删除ftl文件夹
        FileUtils.deleteDirectory(ftlFolder);
    }

    private static String getModelCnsByModel(String model) {
        int index = ArrayUtils.indexOf(ConfigUtil.getTableNames(), StringCaseUtil.camelToSnake(model));
        return ConfigUtil.getModelCns()[index];
    }

}