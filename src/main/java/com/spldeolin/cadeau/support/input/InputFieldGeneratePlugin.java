package com.spldeolin.cadeau.support.input;

import static com.spldeolin.cadeau.support.util.ConstantUtil.ftlPath;
import static com.spldeolin.cadeau.support.util.JdbcUtil.getColumnType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import com.spldeolin.cadeau.support.util.ConfigUtil;
import com.spldeolin.cadeau.support.util.StringCaseUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class InputFieldGeneratePlugin extends PluginAdapter {

    private int fieldIndex = 0;

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
        String fieldName = field.getName();
        FullyQualifiedJavaType fieldType = field.getType();
        // 忽略insertedAt，isDeleted和byte[]类型字段
        if (StringUtils.equalsAny(fieldName, "insertedAt", "isDeleted") || "byte[]".equals(fieldType.getShortName())) {
            return true;
        }
        File folder = new File(ftlPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        DtoFieldFTL dtoFieldFTL = new DtoFieldFTL();
        dtoFieldFTL.setType(fieldType.getShortName());
        dtoFieldFTL.setName(fieldName);
        dtoFieldFTL.setJavadoc(introspectedColumn.getRemarks());
        // @JsonProperty
        if (StringCaseUtil.hasUpperCase(fieldName)) {
            dtoFieldFTL.setNameSnake(introspectedColumn.getActualColumnName());
        }
        List<String> invalidAnnotations = new ArrayList<>();
        // @Size
        if (new FullyQualifiedJavaType("java.lang.String").equals(fieldType)) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType("javax.validation.constraints.Size"));
            invalidAnnotations.add("@Size(max = " + introspectedColumn.getLength() + ")");
        }
        // @Digits
        if (new FullyQualifiedJavaType("java.math.BigDecimal").equals(fieldType) ||
                new FullyQualifiedJavaType("java.lang.Double").equals(fieldType) ||
                new FullyQualifiedJavaType("java.lang.Float").equals(fieldType)) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType("javax.validation.constraints.Digits"));
            invalidAnnotations.add(
                    "@Digits(integer = " + (introspectedColumn.getLength() - introspectedColumn.getScale()) +
                            ", fraction = " + introspectedColumn.getScale() + ")");
        }
        // @TextOption
        if (introspectedColumn.getJdbcType() == 1) {
            // JdbcType为1时，Mysql类型是char或enum，通过查information_schema表获取真正的类型，如果是enum，追加@TextOption
            String columnType = getColumnType(ConfigUtil.getMysqlDatabase(),
                    introspectedTable.getFullyQualifiedTable().toString(),
                    introspectedColumn.getActualColumnName());
            if (StringUtils.startsWithIgnoreCase(columnType, "enum")) {
                String enumStr = StringUtils.removeStartIgnoreCase(columnType, "enum(");
                enumStr = StringUtils.removeEndIgnoreCase(enumStr, ")");
                invalidAnnotations.add("@TextOption({" + enumStr.replaceAll("'", "\"").replaceAll(",", " ,") + "})");
            }
        }
        dtoFieldFTL.setInvalidAnnotations(invalidAnnotations);
        //try {
        //    String content = FreeMarkerUtil.format(true, "input-field-template.ftl", dtoFieldFTL);
        //    FileUtils.write(
        //            new File(ftlPath + fieldIndex + "_" + topLevelClass.getType().getShortName() + "#"
        //                    + field.getName() + ".ftl"), content, StandardCharsets.UTF_8);
        //} catch (IOException e) {
        //    log.error("checked", e);
        //    throw new RuntimeException();
        //}
        // 存入Holder
        DTOFieldFTLHolder.getInstance().getFields().put(fieldIndex + "_" + topLevelClass.getType().getShortName() + "#"
                + field.getName(), dtoFieldFTL);
        synchronized (this) {
            fieldIndex++;
        }
        return true;
    }

}
