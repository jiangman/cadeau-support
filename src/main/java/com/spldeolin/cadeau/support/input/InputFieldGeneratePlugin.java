package com.spldeolin.cadeau.support.input;

import static com.spldeolin.cadeau.support.util.ConstantUtils.ftlPath;
import static com.spldeolin.cadeau.support.util.JdbcUtils.getColumnType;

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
import com.spldeolin.cadeau.support.util.ProjectProperties;
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
        // 忽略insertedAt，deletionFlag和byte[]类型字段
        if (StringUtils.equalsAny(fieldName, "insertedAt", "deletionFlag") || "byte[]".equals(fieldType.getShortName())) {
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
        List<String> invalidAnnotations = new ArrayList<>();
        String enumStr = enumStr(introspectedColumn, introspectedTable);
        // @Length
        if (enumStr == null && new FullyQualifiedJavaType("java.lang.String").equals(fieldType)) {
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
        if (enumStr != null) {
            invalidAnnotations.add("@TextOption({" + enumStr.replaceAll("'", "\"").replaceAll(",", " ,") + "})");
        }
        // @Mobile
        if (fieldName.contains("mobile")) {
            invalidAnnotations.add("@Mobile");
        }
        // @Email
        if (fieldName.contains("email")) {
            invalidAnnotations.add("@Email");
        }
        dtoFieldFTL.setInvalidAnnotations(invalidAnnotations);
        // 存入Holder
        DTOFieldFTLHolder.getInstance().getFields().put(fieldIndex + "_" + topLevelClass.getType().getShortName() + "#"
                + field.getName(), dtoFieldFTL);
        synchronized (this) {
            fieldIndex++;
        }
        return true;
    }

    /**
     * 如果是enum，则返回可选值，否则返回null
     *
     * @return e.g.   "('male','female')"
     */
    private String enumStr(IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        ProjectProperties properties = ProjectProperties.instance();
        if (introspectedColumn.getJdbcType() == 1) {
            // JdbcType为1时，Mysql类型是char或enum，通过查information_schema表获取真正的类型，如果是enum，追加@TextOption
            String columnType = getColumnType(properties.getMysqlDatabase(),
                    introspectedTable.getFullyQualifiedTable().toString(),
                    introspectedColumn.getActualColumnName());
            if (StringUtils.startsWithIgnoreCase(columnType, "enum")) {
                String enumStr = StringUtils.removeStartIgnoreCase(columnType, "enum(");
                enumStr = StringUtils.removeEndIgnoreCase(enumStr, ")");
                return enumStr;
            }
        }
        return null;
    }

}
