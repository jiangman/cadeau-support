package com.spldeolin.cadeau.support.doc.helper;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/17
 */
@Log4j2
public class AnnotationHelper {

    /**
     * 在AnnotationExpr列表中（可以来自类、方法、field、参数等），
     * 获取指定注解的指定属性的值，
     * 未指定或是指定的内容为空白，将会返回""
     */
    public static String getAnnotationProperty(List<AnnotationExpr> annotations, String annotationName,
            String propertyName) {
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
}
