package com.spldeolin.cadeau.support.doc.helper;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import com.spldeolin.cadeau.support.util.Nulls;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.comments.Comment;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/17
 */
@UtilityClass
@Log4j2
public class TypeDeclarationHelper {

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

}
