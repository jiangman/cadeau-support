package com.spldeolin.cadeau.support.doc.helper;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
import com.spldeolin.cadeau.support.constant.Abbreviation;
import com.spldeolin.cadeau.support.util.Nulls;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.NameExpr;
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
        // todo 考虑JsonProperty
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
        List<String> commentLines = newArrayList(
                Nulls.toEmpty(fieldDeclaration.getComment().getContent()).split("\n"));
        List<String> informativeCommentLines = newArrayList();
        for (String commentLine : commentLines) {
            commentLine = commentLine.replaceFirst("\\*", "").trim();
            if (StringUtils.isNotEmpty(commentLine) && !commentLine.startsWith("@")) {
                informativeCommentLines.add(commentLine);
            }
        }
        return Joiner.on(Abbreviation.br).join(informativeCommentLines);
    }

    public static boolean isSerialVersionUID(FieldDeclaration fieldDeclaration) {
        return "serialVersionUID".equals(getFieldName(fieldDeclaration));
    }

    public static boolean hasJsonIgnore(FieldDeclaration fieldDeclaration) {
        List<AnnotationExpr> annotationExprs = fieldDeclaration.getAnnotations();
        if (annotationExprs != null) {
            for (AnnotationExpr annotationExpr : annotationExprs) {
                NameExpr nameExpr = annotationExpr.getName();
                if (nameExpr != null && "JsonIgnore".equals(nameExpr.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
