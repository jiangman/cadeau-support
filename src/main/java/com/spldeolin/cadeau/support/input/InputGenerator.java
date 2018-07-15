package com.spldeolin.cadeau.support.input;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.util.ConfigUtil;
import com.spldeolin.cadeau.support.util.FileMoveUtil;
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
        List<InputFTL> inputFTLs = new ArrayList<>();
        // 获取fields并排序
        List<Map.Entry<String, DtoFieldFTL>> entries = new ArrayList<>(
                DTOFieldFTLHolder.getInstance().getFields().entrySet());
        entries.sort((entry1, entry2) -> {
            int index1 = Integer.parseInt(indexFromEntry(entry1));
            int index2 = Integer.parseInt(indexFromEntry(entry2));
            return index1 - index2;
        });
        // 遍历fields，作成inputFTLs
        for (Map.Entry<String, DtoFieldFTL> entry : entries) {
            String index = indexFromEntry(entry);
            String model = modelFromEntry(entry);
            InputFTL inputFTL = new InputFTL();
            inputFTL.setIndex(Integer.parseInt(index));
            inputFTL.setInputPackage(ConfigUtil.getInputPackage());
            inputFTL.setTextOption(ConfigUtil.getTextOption());
            inputFTL.setDescription("“" + getModelCnsByModel(model) + "”Input类");
            inputFTL.setAuthor(ConfigUtil.getAuthor());
            inputFTL.setDate(ConfigUtil.getDate());
            inputFTL.setClassDocEnd(ConfigUtil.getClassDocEnd());
            inputFTL.setModelPackage(ConfigUtil.getModelPackage());
            inputFTL.setModel(model);
            inputFTLs.add(inputFTL);
        }
        // inputFTLs去重
        inputFTLs = inputFTLs.stream().distinct().collect(Collectors.toList());
        // 重新遍历inputFTLs，选择属于自己的field
        for (InputFTL inputFTL : inputFTLs) {
            List<DtoFieldFTL> fields = Lists.newArrayList();
            List<String> stringFieldNames = Lists.newArrayList();
            for (Map.Entry<String, DtoFieldFTL> entry : entries) {
                if (inputFTL.getModel().equals(modelFromEntry(entry))) {
                    fields.add(entry.getValue());
                }
                if ("String".equals(entry.getValue().getType())) {
                    stringFieldNames.add(entry.getValue().getName());
                }
            }
            inputFTL.setFields(fields);
            inputFTL.setStringFieldNames(stringFieldNames);
        }
        // 根据inputFTLs，生成Java文件
        for (InputFTL inputFTL : inputFTLs) {
            String content = FreeMarkerUtil.format(true, "input-template.ftl", inputFTL);
            if (ConfigUtil.getOverWrite()) {
                FileUtils.write(new File(ConfigUtil.getInputPath() + inputFTL.getModel() + "Input.java"),
                        content, StandardCharsets.UTF_8);
            } else {
                File f = new File(ConfigUtil.getInputPath() + inputFTL.getModel() + "Input.java");
                if (f.exists()) {
                    f = FileMoveUtil.renameFile(f, 1);
                }
                FileUtils.write(f, content, StandardCharsets.UTF_8);
            }
        }
    }

    private static String getModelCnsByModel(String model) {
        int index = ArrayUtils.indexOf(ConfigUtil.getTableNames(), StringCaseUtil.camelToSnake(model));
        return ConfigUtil.getModelCns()[index];
    }

    private static String indexFromEntry(Map.Entry<String, DtoFieldFTL> entry) {
        return entry.getKey().split("#")[0].split("_")[0];
    }

    private static String modelFromEntry(Map.Entry<String, DtoFieldFTL> entry) {
        return entry.getKey().split("#")[0].split("_")[1];
    }

}