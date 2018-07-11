package com.spldeolin.cadeau.support.persistence;

import java.util.List;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

public class AnnotationAddPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * Intercepts base record class generation
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        addDataAnnotation(topLevelClass, introspectedTable);
        return true;
    }

    /**
     * Intercepts primary key class generation
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        addDataAnnotation(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
        FullyQualifiedJavaType fieldType = field.getType();
        topLevelClass.addImportedType(new FullyQualifiedJavaType("com.fasterxml.jackson.annotation.JsonIgnore"));
        if ("insertedAt".equals(field.getName())) {
            field.addAnnotation("@JsonIgnore");
        }
        if ("deletionFlag".equals(field.getName())) {
            field.addAnnotation("@JsonIgnore");
        }
        // 文件类型（比较少见）
        if ("byte[]".equals(fieldType.getShortName())) {
            field.addAnnotation("@JsonIgnore");
        }
        return true;
    }

    /**
     * Intercepts "record with blob" class generation
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addDataAnnotation(topLevelClass, introspectedTable);
        return true;
    }

    /**
     * Prevents all getters from being generated.
     * See SimpleModelGenerator
     */
    @Override
    public boolean modelGetterMethodGenerated(Method method,
            TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {
        return false;
    }

    /**
     * Prevents all setters from being generated
     * See SimpleModelGenerator
     */
    @Override
    public boolean modelSetterMethodGenerated(Method method,
            TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {
        return false;
    }

    /**
     * Adds the @Data lombok import and annotation to the class
     */
    protected void addDataAnnotation(TopLevelClass clazz, IntrospectedTable table) {
        // import语句
        clazz.addImportedType(new FullyQualifiedJavaType("lombok.AllArgsConstructor"));
        clazz.addImportedType(new FullyQualifiedJavaType("lombok.Builder"));
        clazz.addImportedType(new FullyQualifiedJavaType("lombok.Data"));
        clazz.addImportedType(new FullyQualifiedJavaType("lombok.NoArgsConstructor"));
        clazz.addImportedType(new FullyQualifiedJavaType("lombok.experimental.Accessors"));
        // 类级注解
        clazz.addAnnotation("@Data");
        clazz.addAnnotation("@NoArgsConstructor");
        clazz.addAnnotation("@AllArgsConstructor");
        clazz.addAnnotation("@Builder");
        clazz.addAnnotation("@Accessors(chain = true)");
    }

}
