package com.spldeolin.cadeau.support.doc.helper;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.spldeolin.cadeau.support.util.Nulls;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.comments.Comment;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/16
 */
@UtilityClass
@Log4j2
public class FieldDeclarationHelper {

    public static String getFieldName(FieldDeclaration fieldDeclaration) {
        // 优先考虑@JsonProperty的值
        String jsonPropertyValue = getAnnotationProperty(fieldDeclaration, "JsonProperty", "value");
        if (StringUtils.isNotBlank(jsonPropertyValue)) {
            return jsonPropertyValue;
        }
        // 其次考虑Field本身的名称
        List<VariableDeclarator> variableDeclarators = fieldDeclaration.getVariables();
        if (variableDeclarators != null && variableDeclarators.size() > 0) {
            VariableDeclarator variableDeclarator = variableDeclarators.get(0);
            return variableDeclarator.getId().getName();
        }
        return "";
    }

    public static String getFieldType(FieldDeclaration fieldDeclaration) {
        Type type = fieldDeclaration.getType();
        ReferenceType typeEx = (ReferenceType) type;
        Type typeEx2 = typeEx.getType();
        if (typeEx2 instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType typeEx3 = (ClassOrInterfaceType) typeEx2;
            return typeEx3.getName();
        }
        return "";
    }

    public static String getFieldDesc(FieldDeclaration fieldDeclaration) {
        Comment comment = fieldDeclaration.getComment();
        if (comment == null) {
            return "";
        }
        List<String> commentLines = newArrayList(Nulls.toEmpty(comment.getContent()).split("\n"));
        List<String> informativeCommentLines = newArrayList();
        for (String commentLine : commentLines) {
            commentLine = commentLine.replaceFirst("\\*", "").trim();
            if (StringUtils.isNotEmpty(commentLine) && !commentLine.startsWith("@")) {
                informativeCommentLines.add(commentLine);
            }
        }
        if (informativeCommentLines.size() == 0) {
            return "";
        } else {
            return informativeCommentLines.get(0);
        }
    }

    public static boolean isSerialVersionUID(FieldDeclaration fieldDeclaration) {
        return "serialVersionUID".equals(getFieldName(fieldDeclaration));
    }

    public static boolean isUpdatedAt(FieldDeclaration fieldDeclaration) {
        return "updatedAt".equals(getFieldName(fieldDeclaration));
    }

    public static boolean hasJsonIgnore(FieldDeclaration fieldDeclaration) {
        return hasAnnotation(fieldDeclaration, "JsonIgnore");
    }

    public static boolean hasAnnotation(FieldDeclaration fieldDeclaration, String annotationName) {
        List<AnnotationExpr> annotationExprs = fieldDeclaration.getAnnotations();
        if (annotationExprs != null) {
            for (AnnotationExpr annotationExpr : annotationExprs) {
                NameExpr nameExpr = annotationExpr.getName();
                if (nameExpr != null && annotationName.equals(nameExpr.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 在AnnotationExpr列表中（可以来自类、方法、field、参数等），
     * 获取指定注解的指定属性的值，
     * 未指定或是指定的内容为空白，将会返回""
     */
    public static String getAnnotationProperty(FieldDeclaration fieldDeclaration, String annotationName,
            String propertyName) {
        List<AnnotationExpr> annotations = fieldDeclaration.getAnnotations();
        if (annotations != null) {
            for (AnnotationExpr annotation : annotations) {
                if (annotation.getName().getName().equals(annotationName)) {
                    // NormalAnnotationExpr代表注解内声明了多个属性
                    if (annotation instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr annotationEx = (NormalAnnotationExpr) annotation;
                        List<MemberValuePair> pairs = annotationEx.getPairs();
                        if (pairs != null) {
                            for (MemberValuePair pair : pairs) {
                                if (pair.getName().equals(propertyName)) {
                                    Expression expression = pair.getValue();
                                    if (expression instanceof StringLiteralExpr) {
                                        StringLiteralExpr expressionEx = (StringLiteralExpr) expression;
                                        String result = expressionEx.getValue();
                                        if (StringUtils.isNotBlank(result)) {
                                            return result;
                                        }
                                    }
                                    if (expression instanceof BooleanLiteralExpr) {
                                        BooleanLiteralExpr expressionEx = (BooleanLiteralExpr) expression;
                                        return String.valueOf(expressionEx.getValue());
                                    }
                                }
                            }
                        }
                    }
                    // SingleMemberAnnotationExpr代表注解内声明了一个属性，当需要获取的属性是"value"时，单属性注解也需要考虑
                    if ("value".equals(propertyName) && annotation instanceof SingleMemberAnnotationExpr) {
                        SingleMemberAnnotationExpr annotationEx = (SingleMemberAnnotationExpr) annotation;
                        Expression expression = annotationEx.getMemberValue();
                        if (expression instanceof StringLiteralExpr) {
                            StringLiteralExpr expressionEx = (StringLiteralExpr) expression;
                            String result = expressionEx.getValue();
                            if (StringUtils.isNotBlank(result)) {
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    public static boolean isSimpleType(FieldDeclaration fieldDeclaration) {
        return TypeHelper.isSimpleType(fieldDeclaration.getType());
    }

    public static boolean isListOrSet(FieldDeclaration fieldDeclaration) {
        return TypeHelper.isListOrSet(fieldDeclaration.getType());
    }

    public static Type getGenericType(FieldDeclaration fieldDeclaration) {
        return TypeHelper.getGenericType(fieldDeclaration.getType());
    }

}
