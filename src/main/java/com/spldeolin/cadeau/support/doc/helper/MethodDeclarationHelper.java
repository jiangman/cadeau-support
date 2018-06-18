package com.spldeolin.cadeau.support.doc.helper;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.comments.Comment;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.type.Type;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/17
 */
@Log4j2
public class MethodDeclarationHelper {

    public static String getName(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getName();
    }

    public static Type getReturnType(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getType();
    }

    public static String getReturnTypeName(MethodDeclaration methodDeclaration) {
        Type type = methodDeclaration.getType();
        return TypeHelper.getTypeName(type);
    }

    public static String getAuthor(MethodDeclaration methodDeclaration) {
        return TypeDeclarationHelper.getAuthor(methodDeclaration.getComment());
    }

    /**
     * 获取声明在方法上的JavaDoc
     */
    public static String getDescription(MethodDeclaration methodDeclaration) {
        Comment comment = methodDeclaration.getComment();
        String result = TypeDeclarationHelper.getDescription(comment);
        if (StringUtils.isBlank(result)) {
            result = getName(methodDeclaration);
        }
        return result;
    }

    public static String getFirstLineDecription(MethodDeclaration methodDeclaration) {
        String description = getDescription(methodDeclaration);
        String[] descriptionLines = description.split("\n");
        if (descriptionLines.length > 0) {
            return descriptionLines[0];
        }
        return getName(methodDeclaration);
    }

    /**
     * MethodDeclaration是否有RequestMapping、GetMapping、PostMapping等注解
     */
    public static boolean hasRequestMapping(MethodDeclaration methodDeclaration) {
        for (AnnotationExpr annotation : methodDeclaration.getAnnotations()) {
            if (StringUtils.equalsAny(annotation.getName().getName(), "RequestMapping", "GetMapping", "PostMapping",
                    "PutMapping", "DeleteMapping")) {
                return true;
            }
        }
        return false;
    }

    public static String getMethodMapping(MethodDeclaration methodDeclaration) {
        List<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
        String mapping = AnnotationHelper.getAnnotationProperty(annotations, "RequestMapping", "value");
        if ("".equals(mapping)) {
            mapping = AnnotationHelper.getAnnotationProperty(annotations, "GetMapping", "value");
        }
        if ("".equals(mapping)) {
            mapping = AnnotationHelper.getAnnotationProperty(annotations, "PostMapping", "value");
        }
        if ("".equals(mapping)) {
            mapping = AnnotationHelper.getAnnotationProperty(annotations, "PutMapping", "value");
        }
        if ("".equals(mapping)) {
            mapping = AnnotationHelper.getAnnotationProperty(annotations, "DeleteMapping", "value");
        }
        // 不会空时确保以"/"开头
        if (!"".equals(mapping) && !mapping.startsWith("/")) {
            mapping = "/" + mapping;
        }
        return mapping;
    }

    /**
     * 获取请求方法的请求动词
     */
    public static String getMethodHttpMethod(MethodDeclaration methodDeclaration) {
        for (AnnotationExpr annotation : methodDeclaration.getAnnotations()) {
            String annotationName = annotation.getName().getName();
            if ("GetMapping".equals(annotationName)) {
                return "GET";
            }
            if ("PostMapping".equals(annotationName)) {
                return "POST";
            }
            if ("PutMapping".equals(annotationName)) {
                return "PUT";
            }
            if ("DeleteMapping".equals(annotationName)) {
                return "DELETE";
            }
            if ("RequestMapping".equals(annotationName)) {
                if (annotation instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr annotationEx = (NormalAnnotationExpr) annotation;
                    List<MemberValuePair> pairs = annotationEx.getPairs();
                    if (pairs != null) {
                        for (MemberValuePair pair : pairs) {
                            if (pair.getName().equals("method")) {
                                Expression expression = pair.getValue();
                                if (expression instanceof FieldAccessExpr) {
                                    FieldAccessExpr expressionEx = (FieldAccessExpr) expression;
                                    return expressionEx.getField();
                                }
                            }
                        }
                    }
                }
            }
        }
        throw new RuntimeException("impossible");
    }

}
