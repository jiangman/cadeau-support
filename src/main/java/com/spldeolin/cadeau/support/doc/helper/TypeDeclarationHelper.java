package com.spldeolin.cadeau.support.doc.helper;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.spldeolin.cadeau.support.util.Nulls;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.comments.Comment;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/17
 */
@UtilityClass
@Log4j2
public class TypeDeclarationHelper {

    public static String getName(TypeDeclaration typeDeclaration) {
        return typeDeclaration.getName();
    }

    public static String getAuthor(TypeDeclaration typeDeclaration) {
        return getAuthor(typeDeclaration.getComment());
    }

    public static String getAuthor(Comment comment) {
        if (comment == null) {
            return "";
        }
        List<String> commentLines = newArrayList(Nulls.toEmpty(comment.getContent()).split("\n"));
        for (String commentLine : commentLines) {
            commentLine = commentLine.replaceFirst("\\*", "").trim();
            if (commentLine.startsWith("@author ")) {
                return commentLine.replace("@author ", "");
            }
        }
        return "";
    }

    /**
     * 获取声明在类上的JavaDoc
     */
    public static String getDescription(TypeDeclaration typeDeclaration) {
        // 获取comment
        Comment comment = typeDeclaration.getComment();
        String result = getDescription(comment);
        // 获取Swagger注解
        if (StringUtils.isBlank(result)) {
            result = AnnotationHelper.getAnnotationProperty(typeDeclaration.getAnnotations(), "Api", "description");
        }
        // 获取方法名
        if (StringUtils.isBlank(result)) {
            result = getName(typeDeclaration);
        }
        return result;
    }

    /**
     * 获取声明在类上的JavaDoc的第一行
     */
    public static String getFirstLineDescription(TypeDeclaration typeDeclaration) {
        String description = getDescription(typeDeclaration);
        String[] descriptionLines = description.split("\n");
        if (descriptionLines.length > 0) {
            return descriptionLines[0];
        }
        return getName(typeDeclaration);
    }

    public static String getDescription(Comment comment) {
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
        return Joiner.on("\n").join(informativeCommentLines);
    }

    /**
     * TypeDeclaration是否实现了ErrorController
     */
    public static boolean implementsErrorController(TypeDeclaration typeDeclaration) {
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

    /**
     * TypeDeclaration是否含有@Controller或@RestController注解
     */
    public static boolean hasControllerAnnotation(TypeDeclaration typeDeclaration) {
        for (AnnotationExpr annotationExpr : typeDeclaration.getAnnotations()) {
            String annotationName = annotationExpr.getName().getName();
            if (StringUtils.equalsAny(annotationName, "RestController", "Controller")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取声明在类（控制器）上的@RequestMapping注解value属性的值，
     * 没有声明则返回""，否则返回值将会确保以"/"开头
     */
    public static String getControllerMapping(TypeDeclaration typeDeclaration) {
        String mapping = AnnotationHelper.getAnnotationProperty(typeDeclaration.getAnnotations(), "RequestMapping",
                "value");
        // 不为空时确保以"/"开头
        if (!"".equals(mapping) && !mapping.startsWith("/")) {
            mapping = "/" + mapping;
        }
        // 只有一个"/"时当作空
        if ("/".equals(mapping)) {
            mapping = "";
        }
        return mapping;
    }

}
