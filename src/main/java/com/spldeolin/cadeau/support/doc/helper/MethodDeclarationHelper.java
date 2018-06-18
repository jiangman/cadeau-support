package com.spldeolin.cadeau.support.doc.helper;

import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.type.Type;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/17
 */
@Log4j2
public class MethodDeclarationHelper {

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

}
