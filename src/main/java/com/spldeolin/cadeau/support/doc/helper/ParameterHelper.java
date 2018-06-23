package com.spldeolin.cadeau.support.doc.helper;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.comments.Comment;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.type.Type;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/20
 */
@Log4j2
public class ParameterHelper {

    public static String getParameterName(Parameter parameter) {
        String result = parameter.getId().getName();
        List<AnnotationExpr> annotations = parameter.getAnnotations();
        if (isRequestParam(parameter)) {
            String requestParamValue = AnnotationHelper.getAnnotationProperty(annotations, "RequestParam", "value");
            if (StringUtils.isNotBlank(requestParamValue)) {
                result = requestParamValue;
            }
        }
        if (isPathVariable(parameter)) {
            String pathVariableValue = AnnotationHelper.getAnnotationProperty(annotations, "PathVariable", "value");
            if (StringUtils.isNotBlank(pathVariableValue)) {
                result = pathVariableValue;
            }
        }
        return result;
    }

    public static boolean isPathVariable(Parameter parameter) {
        List<AnnotationExpr> annotations = parameter.getAnnotations();
        if (annotations != null) {
            for (AnnotationExpr annotation : annotations) {
                if ("PathVariable".equals(annotation.getName().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRequestBody(Parameter parameter) {
        List<AnnotationExpr> annotations = parameter.getAnnotations();
        if (annotations != null) {
            for (AnnotationExpr annotation : annotations) {
                if ("RequestBody".equals(annotation.getName().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRequestParam(Parameter parameter) {
        return !isRequestBody(parameter) && !isPathVariable(parameter);
    }

    public static Type getParameterType(Parameter parameter) {
        return parameter.getType();
    }

    public static String getParameterTypeName(Parameter parameter) {
        return TypeHelper.getTypeName(getParameterType(parameter));
    }

    public static String getDescription(Parameter parameter) {
        Comment comment = parameter.getComment();
        String result = TypeDeclarationHelper.getDescription(comment);
        if (StringUtils.isBlank(result)) {
            result = "";
        }
        return result;
    }

    public static String getFirstLineDecription(Parameter parameter) {
        String description = getDescription(parameter);
        String[] descriptionLines = description.split("\n");
        if (descriptionLines.length > 0) {
            return descriptionLines[0];
        }
        return "";
    }

    public static boolean isRequiredFalse(Parameter parameter) {
        return "false".equals(AnnotationHelper.getAnnotationProperty(parameter.getAnnotations(),
                "RequestParam", "required"));
    }

    public static boolean isAssignedDefaultValue(Parameter parameter) {
        List<AnnotationExpr> annotations = parameter.getAnnotations();
        if (annotations != null) {
            for (AnnotationExpr annotation : annotations) {
                if (annotation.getName().getName().equals("RequestParam")) {
                    if (annotation instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr annotationEx = (NormalAnnotationExpr) annotation;
                        List<MemberValuePair> pairs = annotationEx.getPairs();
                        if (pairs != null) {
                            for (MemberValuePair pair : pairs) {
                                if (pair.getName().equals("defaultValue")) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isNotCustomType(Parameter parameter) {
        return (StringUtils.equalsAnyIgnoreCase(ParameterHelper.getParameterTypeName(parameter), "HttpServletRequest",
                "HttpServletResponse", "HttpRequest", "HttpResponse", "HttpSession"));
    }

}
