package com.spldeolin.cadeau.support.doc;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.doc.helper.FieldDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.JsonTypeHelper;
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
        if (isSimpleType(returnTypeName)) {
            sampleJson = sampleValueBySimpleTypeName(returnTypeName);
            ftl.setIsRetrunSimpleType(true);
        } else {
            ftl.setIsRetrunSimpleType(false);
            TypeDeclaration returnType = getReturnType(returnTypeName);
            StringBuilder sb = new StringBuilder(400);
            SampleJsonGenerator.analysisField(sb, returnType, false);
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
        if (isSimpleType(returnTypeName)) {
            return;
        }
        TypeDeclaration returnType = getReturnType(returnTypeName);
        List<MarkdownDocFTL.RField> rFields = Lists.newArrayList();
        for (BodyDeclaration bodyDeclaration : returnType.getMembers()) {
            if (bodyDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
                if (FieldDeclarationHelper.isSerialVersionUID(fieldDeclaration)
                        || FieldDeclarationHelper.hasJsonIgnore(fieldDeclaration)) {
                    continue;
                }
                MarkdownDocFTL.RField rField = new MarkdownDocFTL.RField();
                boolean isSimpleType = true; // todo 判断fieldDeclaration是否是简单类型
                rField.setReturnName(FieldDeclarationHelper.getFieldName(fieldDeclaration));
                if (isSimpleType) {
                    rField.setReturnType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(
                            FieldDeclarationHelper.getFieldType(fieldDeclaration)));
                } else {
                    rField.setReturnType("Object");
                }
                rField.setReturnDesc(FieldDeclarationHelper.getFieldDesc(fieldDeclaration));
                rFields.add(rField);
                // todo 递归rFields
            }
        }
        ftl.setReturnFields(rFields);
    }

    private static boolean isSimpleType(String typeName) {
        List<SimpleType> simpleTypes = newArrayList(SimpleType.values());
        List<String> simpleTypeNames = simpleTypes.stream().map(SimpleType::getName).collect(Collectors.toList());
        return typeJudgement(typeName, simpleTypeNames.toArray(new String[] {}));
    }

    public static boolean typeJudgement(String typeName, String... typeNames) {
        return StringUtils.equalsAny(typeName, typeNames);
    }

    /**
     *
     */
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
                                    String result = SampleJsonGenerator.getTypeName(type);
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

    private static TypeDeclaration getReturnType(String returnTypeName) {
        List<TypeDeclaration> typeDeclarations = JavaLoader.loadJavasAsType(DocConfig.basePackagePath);
        return typeDeclarations.stream().filter(t -> t.getName().equals(returnTypeName)).findFirst().orElseThrow(
                () -> new RuntimeException(returnTypeName + "不存在或是无法解析"));
    }

    private static String sampleValueBySimpleTypeName(String typeName) {
        String sampleValue = "null";
        for (SimpleType simpleType : SimpleType.values()) {
            if (simpleType.getName().equals(typeName)) {
                sampleValue = simpleType.getSampleValue();
                for (SimpleType.JsonString jsonString : SimpleType.JsonString.values()) {
                    if (jsonString.getName().equals(typeName)) {
                        sampleValue = wrapDoubleQuotes(sampleValue);
                        break;
                    }
                }
                break;
            }
        }
        return sampleValue;
    }

    private static String wrapDoubleQuotes(String string) {
        return "\"" + string + "\"";
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

    // JsonType
    //private static String typeNameToJsonType() {}

}
