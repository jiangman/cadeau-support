package com.spldeolin.cadeau.support.doc;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.type.Type;
import lombok.extern.log4j.Log4j2;

/**
 * @author deoli 2018/06/11
 */
@Log4j2
public class ReturnParser {

    public static void parserReturn(MarkdownDocFTL ftl, MethodDeclaration requestMethod) {
        // 获取返回值类型
        String returnTypeName = getReturnTypeName(requestMethod);
        if ("".equals(returnTypeName)) {
            return;
        }
        TypeDeclaration returnType = getReturnType(returnTypeName);
        // 生成返回值示例
        String sampleJson;
        if (isSimpleType(returnType)) {
            sampleJson = sampleValueBySimpleType(returnType);
        } else {
            StringBuilder sb = new StringBuilder(400);
            SampleJsonGenerator.analysisField(sb, returnType, false);
            // 美化JSON TODO 为了方便看日志，先注释掉
            //sampleJson = JsonFormatUtil.formatJson(sb.toString());
            sampleJson = sb.toString();
        }
        ftl.setReturnJson(sampleJson);
    }

    private static boolean isSimpleType(TypeDeclaration typeDeclaration) {
        List<SimpleType> simpleTypes = newArrayList(SimpleType.values());
        List<String> simpleTypeNames = simpleTypes.stream().map(SimpleType::getName).collect(Collectors.toList());
        return typeJudgement(typeDeclaration, simpleTypeNames.toArray(new String[] {}));
    }

    public static boolean typeJudgement(TypeDeclaration typeDeclaration, String... typeNames) {
        return StringUtils.equalsAny(typeDeclaration.getName(), typeNames);
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

    private static String sampleValueBySimpleType(TypeDeclaration typeDeclaration) {
        String actualTypeName = typeDeclaration.getName();
        String sampleValue = "null";
        for (SimpleType simpleType : SimpleType.values()) {
            if (simpleType.getName().equals(actualTypeName)) {
                sampleValue = simpleType.getSampleValue();
                for (SimpleType.JsonString jsonString : SimpleType.JsonString.values()) {
                    if (jsonString.getName().equals(actualTypeName)) {
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

}
