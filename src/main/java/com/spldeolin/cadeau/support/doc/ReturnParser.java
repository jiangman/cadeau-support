package com.spldeolin.cadeau.support.doc;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.doc.helper.FieldDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.JsonTypeHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeHelper;
import com.spldeolin.cadeau.support.util.JsonFormatUtil;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.type.Type;
import lombok.extern.log4j.Log4j2;

/**
 * @author deoli 2018/06/11
 */
@Log4j2
public class ReturnParser {

    /**
     * generate json sample
     */
    public static void parseReturn(MarkdownDocFTL ftl, MethodDeclaration requestMethod) {
        // 获取返回值类型
        String returnTypeName = getReturnTypeName(requestMethod);
        // 没有返回值
        if ("".equals(returnTypeName)) {
            ftl.setReturnShow(false);
            return;
        }
        ftl.setReturnShow(true);
        // 生成返回值示例
        String sampleJson;
        if (TypeHelper.isSimpleType(returnTypeName)) {
            sampleJson = TypeHelper.sampleValueBySimpleType(returnTypeName);
            ftl.setIsRetrunSimpleType(true);
        } else {
            ftl.setIsRetrunSimpleType(false);
            TypeDeclaration returnType = getTypeFromTypeName(returnTypeName);
            StringBuilder sb = new StringBuilder(400);
            SampleJsonParser.analysisField(sb, returnType, false);
            sampleJson = sb.toString();
        }
        sampleJson = wrapArrayOrPage(sampleJson, requestMethod);
        // 修剪掉多余的逗号
        sampleJson = sampleJson.replace(",]", "]");
        sampleJson = sampleJson.replace(",}", "}");
        // 格式化JSON
        sampleJson = JsonFormatUtil.formatJson(sampleJson);
        ftl.setReturnJson(sampleJson);
    }

    public static void parseReturnFields(MarkdownDocFTL ftl, MethodDeclaration requestMethod) {
        // 获取返回值类型
        String returnTypeName = getReturnTypeName(requestMethod);
        // 没有返回值
        if ("".equals(returnTypeName)) {
            return;
        }
        // TODO 暂时不做简单类型返回值的说明
        if (TypeHelper.isSimpleType(returnTypeName)) {
            return;
        }
        TypeDeclaration returnType = getTypeFromTypeName(returnTypeName);
        List<MarkdownDocFTL.RField> rFields = Lists.newArrayList();
        generateRField(rFields, returnType.getMembers(), "", false);
        ftl.setReturnFields(rFields);
    }

    private static void generateRField(List<MarkdownDocFTL.RField> rFields, List<BodyDeclaration> members,
            String prefix, boolean ignoreUpdatedAt) {
        if (members != null) {
            for (BodyDeclaration bodyDeclaration : members) {
                if (bodyDeclaration instanceof FieldDeclaration) {
                    FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
                    if (FieldDeclarationHelper.isSerialVersionUID(fieldDeclaration)
                            || FieldDeclarationHelper.hasJsonIgnore(fieldDeclaration)) {
                        continue;
                    }
                    if (ignoreUpdatedAt && FieldDeclarationHelper.isUpdatedAt(fieldDeclaration)) {
                        continue;
                    }
                    MarkdownDocFTL.RField rField = new MarkdownDocFTL.RField();
                    boolean isListOrSet = FieldDeclarationHelper.isListOrSet(fieldDeclaration);
                    boolean isSimpleType = FieldDeclarationHelper.isSimpleType(fieldDeclaration);
                    String returnName = FieldDeclarationHelper.getFieldName(fieldDeclaration);
                    if (StringUtils.isNotBlank(prefix)) {
                        returnName = prefix + "." + returnName;
                    }
                    rField.setReturnName(returnName);
                    if (isSimpleType) {
                        rField.setReturnType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(
                                FieldDeclarationHelper.getFieldType(fieldDeclaration)));
                    } else if (isListOrSet) {
                        Type genericType = FieldDeclarationHelper.getGenericType(fieldDeclaration);
                        if (TypeHelper.isSimpleType(genericType)) {
                            rField.setReturnType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(TypeHelper.getTypeName(
                                    genericType)) + " Array");
                        } else {
                            rField.setReturnType("Object Array");
                        }
                    } else {
                        rField.setReturnType("Object");
                    }
                    rField.setReturnDesc(FieldDeclarationHelper.getFieldDesc(fieldDeclaration));
                    rFields.add(rField);
                    if (!isSimpleType) {
                        String fieldTypeName;
                        if (isListOrSet) {
                            fieldTypeName = TypeHelper.getTypeName(FieldDeclarationHelper.getGenericType(
                                    fieldDeclaration));
                        } else {
                            fieldTypeName = FieldDeclarationHelper.getFieldType(fieldDeclaration);
                        }
                        TypeDeclaration fieldType = getTypeFromTypeName(fieldTypeName);
                        generateRField(rFields, fieldType.getMembers(), returnName, true);
                    }
                }
            }
        }
    }

    private static String getReturnTypeName(MethodDeclaration requestMethod) {
        for (AnnotationExpr annotation : requestMethod.getAnnotations()) {
            if (annotation.getName().getName().equals("ReturnStruction")) {
                // NormalAnnotationExpr代表注解内声明了多个属性
                if (annotation instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr annotationEx = (NormalAnnotationExpr) annotation;
                    List<MemberValuePair> pairs = annotationEx.getPairs();
                    if (pairs != null) {
                        for (MemberValuePair pair : pairs) {
                            if (pair.getName().equals("type")) {
                                Expression expression = pair.getValue();
                                if (expression instanceof ClassExpr) {
                                    ClassExpr expressionEx = (ClassExpr) expression;
                                    Type type = expressionEx.getType();
                                    String result = TypeHelper.getTypeName(type);
                                    if (StringUtils.isNotBlank(result)) {
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    private static TypeDeclaration getTypeFromTypeName(String typeName) {
        List<TypeDeclaration> typeDeclarations = JavaLoader.loadJavasAsType(DocConfig.basePackagePath);
        return typeDeclarations.stream().filter(t -> t.getName().equals(typeName)).findFirst().orElseThrow(
                () -> new RuntimeException(typeName + "不存在或是无法解析"));
    }

    private static String wrapArrayOrPage(String json, MethodDeclaration requestMethod) {
        String isArray = ControllerParser.getAnnotationProperty(requestMethod.getAnnotations(), "ReturnStruction",
                "isArray");
        String isPage = ControllerParser.getAnnotationProperty(requestMethod.getAnnotations(), "ReturnStruction",
                "isPage");
        if ("true".equals(isArray) && "true".equals(isPage)) {
            log.warn(requestMethod.getName() + "同时指定了isArray与isPage为true，解析跳过");
            return json;
        }
        if ("true".equals(isArray)) {
            return "[" + json + "]";
        }
        if ("true".equals(isPage)) {
            return "{\"pageNo\":1,\"hasPreviousPage\":false,\"hasNextPage\":true,\"entitiesInPage\":" + json +
                    ",\"pagesCount\":9,\"total\":65535}";
        }
        return json;
    }

}
