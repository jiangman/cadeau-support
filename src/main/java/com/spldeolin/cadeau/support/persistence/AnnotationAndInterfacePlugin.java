package com.spldeolin.cadeau.support.persistence;

import java.util.List;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import com.spldeolin.cadeau.support.util.ProjectProperties;

public class AnnotationAndInterfacePlugin extends PluginAdapter {

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
        add(topLevelClass, introspectedTable);
        return true;
    }

    /**
     * Intercepts primary key class generation
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        add(topLevelClass, introspectedTable);
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
        add(topLevelClass, introspectedTable);
        return true;
    }

    /**
     * Prevents all getters from being generated. See SimpleModelGenerator
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
     * Prevents all setters from being generated See SimpleModelGenerator
     */
    @Override
    public boolean modelSetterMethodGenerated(Method method,
            TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {
        return false;
    }

    private void add(TopLevelClass clazz, IntrospectedTable table) {
        addAnnotations(clazz);
        addInterfaces(clazz);
        addFieldEnum(clazz);
        addSerialVersionUID(clazz, table);
    }

    /**
     * Adds the @Data lombok import and annotation to the class
     */
    private void addAnnotations(TopLevelClass clazz) {
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

    private void addInterfaces(TopLevelClass clazz) {
        ProjectProperties properties = ProjectProperties.instance();
        FullyQualifiedJavaType idGetable = new FullyQualifiedJavaType(properties.getIdGetable());
        FullyQualifiedJavaType serializable = new FullyQualifiedJavaType("java.io.Serializable");

        clazz.addImportedType(idGetable);
        clazz.addImportedType(serializable);

        clazz.addSuperInterface(idGetable);
        clazz.addSuperInterface(serializable);
    }

    private void addSerialVersionUID(TopLevelClass clazz, IntrospectedTable table) {
        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setStatic(true);
        field.setFinal(true);
        field.setType(new FullyQualifiedJavaType("long"));
        field.setName("serialVersionUID");
        field.setInitializationString("1L");

        if (table.getTargetRuntime() == TargetRuntime.MYBATIS3_DSQL) {
            context.getCommentGenerator().addFieldAnnotation(field, table, clazz.getImportedTypes());
        } else {
            context.getCommentGenerator().addFieldComment(field, table);
        }

        clazz.addField(field);
    }

    private void addFieldEnum(TopLevelClass clazz) {
        String enumName = "Property";
        InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(enumName));
        innerEnum.setVisibility(JavaVisibility.PUBLIC);
        innerEnum.setStatic(true);
        innerEnum.setFinal(true);

        for (Field field : clazz.getFields()) {
            innerEnum.addEnumConstant(field.getName() + "(\"" + field.getName() + "\")");
        }

        innerEnum.addField(new Field("fieldName", new FullyQualifiedJavaType("java.lang.String")));

        Method constructor = new Method();
        constructor.setConstructor(true);
        constructor.setName(enumName);
        constructor.addParameter(new Parameter(new FullyQualifiedJavaType("java.lang.String"), "fieldName"));
        constructor.addBodyLine("this.fieldName = fieldName;");
        innerEnum.addMethod(constructor);

        clazz.addInnerEnum(innerEnum);
    }

}
