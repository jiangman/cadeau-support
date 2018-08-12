package com.spldeolin.cadeau.support.input;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.util.FileMoveUtils;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import com.spldeolin.cadeau.support.util.ProjectProperties;
import com.spldeolin.cadeau.support.util.StringCaseUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class InputGenerator {

    @SneakyThrows
    public static void input() {
        ProjectProperties properties = ProjectProperties.instance();

        if (!properties.getGenerateControllerAndInput()) {
            log.info("不生成Input类");
            return;
        }

        List<InputFTL> inputFTLs = new ArrayList<>();
        // 获取fields并排序
        List<Entry<String, DtoFieldFTL>> entries = new ArrayList<>(
                DTOFieldFTLHolder.getInstance().getFields().entrySet());
        entries.sort((entry1, entry2) -> {
            int index1 = Integer.parseInt(indexFromEntry(entry1));
            int index2 = Integer.parseInt(indexFromEntry(entry2));
            return index1 - index2;
        });
        // 遍历fields，作成inputFTLs
        for (Entry<String, DtoFieldFTL> entry : entries) {
            String index = indexFromEntry(entry);
            String model = modelFromEntry(entry);
            InputFTL inputFTL = new InputFTL();
            inputFTL.setIndex(Integer.parseInt(index));
            inputFTL.setInputPackage(properties.getInputPackage());
            inputFTL.setOption(properties.getOption());
            inputFTL.setControllerAspectPreprocess(properties.getControllerAspectPreprocess());
            inputFTL.setDescription("“" + getModelCnsByModel(model) + "”Input类");
            inputFTL.setAuthor(properties.getAuthor());
            inputFTL.setDate(properties.getDate());
            inputFTL.setClassDocEnd(properties.getClassDocEnd());
            inputFTL.setModelPackage(properties.getModelPackage());
            inputFTL.setModel(model);
            inputFTLs.add(inputFTL);
        }
        // inputFTLs去重
        inputFTLs = inputFTLs.stream().distinct().collect(Collectors.toList());
        // 重新遍历inputFTLs，选择属于自己的field
        for (InputFTL inputFTL : inputFTLs) {
            List<DtoFieldFTL> fields = Lists.newArrayList();
            List<String> stringFieldNames = Lists.newArrayList();
            for (Entry<String, DtoFieldFTL> entry : entries) {
                if (inputFTL.getModel().equals(modelFromEntry(entry))) {
                    fields.add(entry.getValue());
                }
            }
            inputFTL.setFields(fields);
            inputFTL.setStringFieldNames(stringFieldNames);
        }

        // string field
        for (InputFTL inputFTL : inputFTLs) {
            List<String> stringFieldNames = Lists.newArrayList();
            for (DtoFieldFTL fieldFTL : inputFTL.getFields()) {
                if ("String".equals(fieldFTL.getType())) {
                    stringFieldNames.add(fieldFTL.getName());
                }
            }
            inputFTL.setStringFieldNames(stringFieldNames);
            if (stringFieldNames.size() == 0) {
                inputFTL.setHasStringField(false);
            } else {
                inputFTL.setHasStringField(true);
            }
        }

        // 根据inputFTLs，生成Java文件
        for (InputFTL inputFTL : inputFTLs) {
            String content = FreeMarkerUtil.format(true, "input-template.ftl", inputFTL);
            if (properties.getOverWrite()) {
                FileUtils.write(new File(properties.getInputPath() + inputFTL.getModel() + "Input.java"),
                        content, StandardCharsets.UTF_8);
            } else {
                File f = new File(properties.getInputPath() + inputFTL.getModel() + "Input.java");
                if (f.exists()) {
                    f = FileMoveUtils.renameFile(f, 1);
                }
                FileUtils.write(f, content, StandardCharsets.UTF_8);
            }
        }
    }

    private static String getModelCnsByModel(String model) {
        ProjectProperties properties = ProjectProperties.instance();
        int index = ArrayUtils.indexOf(properties.getTableNames(), StringCaseUtils.camelToSnake(model));
        return properties.getModelCns()[index];
    }

    private static String indexFromEntry(Map.Entry<String, DtoFieldFTL> entry) {
        return entry.getKey().split("#")[0].split("_")[0];
    }

    private static String modelFromEntry(Map.Entry<String, DtoFieldFTL> entry) {
        return entry.getKey().split("#")[0].split("_")[1];
    }

}