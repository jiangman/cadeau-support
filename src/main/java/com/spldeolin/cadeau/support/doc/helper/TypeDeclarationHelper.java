package com.spldeolin.cadeau.support.doc.helper;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
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
        Comment comment = typeDeclaration.getComment();
        String result = getDescription(comment);
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

}
