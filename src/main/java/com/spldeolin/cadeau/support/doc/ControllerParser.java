package com.spldeolin.cadeau.support.doc;

import static com.google.common.collect.Lists.newArrayList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import lombok.extern.log4j.Log4j2;

/**
 * 解析所有控制器，生成FTL
 *
 * @author deoli 2018/06/11
 */
@Log4j2
public class ControllerParser {

    private static final String br = System.lineSeparator();

    public static List<MarkdownDocFTL> parseController() {
        // 获取所有controller包下的java文件，解析成TypeDeclaration
        List<TypeDeclaration> typeDeclarations = JavaLoader.loadJavasAsType(DocConfig.baseControllerPackagePath);
        // 过滤掉非控制器和ErrorController
        typeDeclarations.removeIf(type -> !hasControllerAnnotation(type) || implementsErrorController(type));
        // 遍历每个控制器的每个请求方法
        List<MarkdownDocFTL> ftls = newArrayList();
        for (TypeDeclaration controller : typeDeclarations) {
            for (MethodDeclaration requestMethod : listRequsetMethod(controller)) {
                log.info(controller.getName() + "#" + requestMethod.getName());
                MarkdownDocFTL ftl = new MarkdownDocFTL();
                ftl.setDirectoryName(getDescription(controller));
                ftl.setCommonDesc(getDescription(requestMethod));
                ftl.setHttpUrl(getControllerMapping(controller) + getMethodMapping(requestMethod));
                ftl.setHttpMethod(getMethodHttpMethod(requestMethod));
                // TODO paramShow, paramJson, paramFields交给ParameterParser解析
                // returnShow, returnJson, isRetrunSimpleType
                ReturnParser.parseReturn(ftl, requestMethod);
                // returnFields
                ReturnParser.parseReturnFields(ftl, requestMethod);
                ftlSetAuthorAndDate(ftl, controller, requestMethod);
                ftls.add(ftl);
            }
        }
        ftls.forEach(log::info);
        return ftls;
    }

    /**
     * TypeDeclaration是否含有@Controller或@RestController注解
     */
    private static boolean hasControllerAnnotation(TypeDeclaration typeDeclaration) {
        for (AnnotationExpr annotationExpr : typeDeclaration.getAnnotations()) {
            String annotationName = annotationExpr.getName().getName();
            if (StringUtils.equalsAny(annotationName, "RestController", "Controller")) {
                return true;
            }
        }
        return false;
    }

    /**
     * TypeDeclaration是否实现了ErrorController
     */
    private static boolean implementsErrorController(TypeDeclaration typeDeclaration) {
        if (typeDeclaration instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration typeEx = (ClassOrInterfaceDeclaration) typeDeclaration;
            List<ClassOrInterfaceType> interfaceTypes = typeEx.getImplements();
            if (interfaceTypes != null) {
                for (ClassOrInterfaceType interfaceType : typeEx.getImplements()) {
                    if (interfaceType.getName().equals("ErrorController")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<MethodDeclaration> listRequsetMethod(TypeDeclaration typeDeclaration) {
        List<BodyDeclaration> bodyDeclarations = typeDeclaration.getMembers();
        List<MethodDeclaration> result = newArrayList();
        for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
            if (bodyDeclaration instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
                if (hasRequestMapping(methodDeclaration)) {
                    result.add(methodDeclaration);
                }
            }
        }
        return result;
    }

    /**
     * MethodDeclaration是否有RequestMapping、GetMapping、PostMapping等注解
     */
    private static boolean hasRequestMapping(MethodDeclaration methodDeclaration) {
        for (AnnotationExpr annotation : methodDeclaration.getAnnotations()) {
            if (StringUtils.equalsAny(annotation.getName().getName(), "RequestMapping", "GetMapping", "PostMapping",
                    "PutMapping", "DeleteMapping")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取声明在类上的@Description注解的值
     */
    private static String getDescription(TypeDeclaration typeDeclaration) {
        String description = getDescription(typeDeclaration.getAnnotations());
        if (StringUtils.isBlank(description)) {
            description = typeDeclaration.getName().replace("Controller", "");
        }
        return description;
    }

    /**
     * 获取声明在方法上的@Description注解的值
     */
    private static String getDescription(MethodDeclaration methodDeclaration) {
        /*
            想办法解析comment的话，似乎可以少一个@Description注解
            methodDeclaration.getComment().getContent().split("\n")[1].trim().replace("* ", "");
         */
        String description = getDescription(methodDeclaration.getAnnotations());
        if (StringUtils.isBlank(description)) {
            description = methodDeclaration.getName();
        }
        return description;
    }

    /**
     * 获取声明在field上的@Description注解的值
     */
    private static String getDescription(FieldDeclaration fieldDeclaration) {
        return getDescription(fieldDeclaration.getAnnotations());
    }

    /**
     * （这个方法封装了其他getDescription方法的共通代码）
     */
    private static String getDescription(List<AnnotationExpr> annotations) {
        for (AnnotationExpr annotation : annotations) {
            if (annotation.getName().getName().equals("Description")) {
                if (annotation instanceof SingleMemberAnnotationExpr) {
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
        return "";
    }

    /**
     * 获取声明在类（控制器）上的@RequestMapping注解value属性的值，
     * 没有声明则返回""，否则返回值将会确保以"/"开头
     */
    private static String getControllerMapping(TypeDeclaration typeDeclaration) {
        String mapping = getAnnotationProperty(typeDeclaration.getAnnotations(), "RequestMapping", "value");
        // 不会空时确保以"/"开头
        if (!"".equals(mapping) && !mapping.startsWith("/")) {
            mapping = "/" + mapping;
        }
        return mapping;
    }

    private static String getMethodMapping(MethodDeclaration methodDeclaration) {
        List<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
        String mapping = getAnnotationProperty(annotations, "RequestMapping", "value");
        if ("".equals(mapping)) {
            mapping = getAnnotationProperty(annotations, "GetMapping", "value");
        }
        if ("".equals(mapping)) {
            mapping = getAnnotationProperty(annotations, "PostMapping", "value");
        }
        if ("".equals(mapping)) {
            mapping = getAnnotationProperty(annotations, "PutMapping", "value");
        }
        if ("".equals(mapping)) {
            mapping = getAnnotationProperty(annotations, "DeleteMapping", "value");
        }
        // 不会空时确保以"/"开头
        if (!"".equals(mapping) && !mapping.startsWith("/")) {
            mapping = "/" + mapping;
        }
        return mapping;
    }

    /**
     * 在AnnotationExpr列表中（可以来自类、方法、field、参数等），
     * 获取指定注解的指定属性的值，
     * 未指定或是指定的内容为空白，将会返回""
     */
    public static String getAnnotationProperty(List<AnnotationExpr> annotations, String annotationName,
            String propertyName) {
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
        return "";
    }

    /**
     * 获取请求方法的请求动词
     */
    private static String getMethodHttpMethod(MethodDeclaration methodDeclaration) {
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

    private static void ftlSetAuthorAndDate(MarkdownDocFTL ftl, TypeDeclaration typeDeclaration,
            MethodDeclaration methodDeclaration) {
        String author = "佚名";
        String controllerAuthor = getAnnotationProperty(typeDeclaration.getAnnotations(), "Author", "value");
        String methodAuthor = getAnnotationProperty(methodDeclaration.getAnnotations(), "Author", "value");
        if (StringUtils.isNotBlank(controllerAuthor)) {
            author = controllerAuthor;
        }
        if (StringUtils.isNotBlank(methodAuthor)) {
            author = methodAuthor;
        }
        String date = new SimpleDateFormat("yyyy/M/d").format(new Date());
        String controllerDate = getAnnotationProperty(typeDeclaration.getAnnotations(), "Author", "date");
        String methodDate = getAnnotationProperty(methodDeclaration.getAnnotations(), "Author", "date");
        if (StringUtils.isNotBlank(controllerDate)) {
            date = controllerDate;
        }
        if (StringUtils.isNotBlank(methodDate)) {
            date = methodDate;
        }
        ftl.setCommonDeveloper(author);
        ftl.setCommonDate(date);
    }

}
