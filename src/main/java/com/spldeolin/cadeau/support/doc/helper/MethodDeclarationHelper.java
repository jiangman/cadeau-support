package com.spldeolin.cadeau.support.doc.helper;

import org.apache.commons.lang3.StringUtils;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.comments.Comment;
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

}
