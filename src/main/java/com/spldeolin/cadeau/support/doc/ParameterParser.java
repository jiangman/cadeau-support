package com.spldeolin.cadeau.support.doc;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.cadeau.support.doc.helper.JsonTypeHelper;
import com.spldeolin.cadeau.support.doc.helper.MethodDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.ParameterHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeHelper;
import com.spldeolin.cadeau.support.util.Nulls;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.comments.Comment;
import japa.parser.ast.type.Type;
import lombok.extern.log4j.Log4j2;

/**
 * @author deoli 2018/06/11
 */
@Log4j2
public class ParameterParser {

    public static void parseParameter(MarkdownDocFTL ftl, MethodDeclaration requestMethod) {
        List<Parameter> parameters = requestMethod.getParameters();
        if (parameters == null) {
            ftl.setParamShow(false);
            return;
        }
        ftl.setParamShow(true);
        ftl.setParamBodyShow(false);
        List<MarkdownDocFTL.PField> pFields = Lists.newArrayList();
        for (Parameter parameter : parameters) {
            if (ParameterHelper.isRequestParam(parameter) || ParameterHelper.isPathVariable(parameter)) {
                String parameterName = ParameterHelper.getParameterName(parameter);
                Type parameterType = ParameterHelper.getParameterType(parameter);
                MarkdownDocFTL.PField pField = new MarkdownDocFTL.PField();
                // paramName
                pField.setParamName(parameterName);
                // paramPlace
                if (ParameterHelper.isRequestParam(parameter)) {
                    pField.setParamPlace("query");
                } else {
                    pField.setParamPlace("path");
                }
                // paramType
                if (TypeHelper.isSimpleType(parameterType)) {
                    String parameterTypeName = TypeHelper.getTypeName(parameterType);
                    pField.setParamType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(parameterTypeName));
                }
                // paramDesc
                pField.setParamDesc(ParameterHelper.getDescription(parameter));
                parseParameterDesc(requestMethod);
                // paramRequired
                if (ParameterHelper.isRequiredFalse(parameter) || ParameterHelper.isAssignedDefaultValue(parameter)) {
                    pField.setParamRequired("非必传");
                } else {
                    pField.setParamRequired("必传");
                }
                pFields.add(pField);
            }
            if (ParameterHelper.isRequestBody(parameter)) {

            }

        }
        ftl.setParamFields(pFields);
    }

    private static Map<String, String> parseParameterDesc(MethodDeclaration requestMethod) {
        Map<String, String> result = Maps.newHashMap();
        Comment comment = requestMethod.getComment();
        if (comment == null) {
            return result;
        }
        List<String> commentLines = newArrayList(Nulls.toEmpty(comment.getContent()).split("\n"));
        for (String commentLine : commentLines) {
            commentLine = commentLine.replaceFirst("\\*", "").trim();
            String[] nodes = commentLine.split(" ");
            int nodeLength = nodes.length;
            if (nodeLength < 3) {
                continue;
            }
            StringBuilder desc = new StringBuilder();
            if (nodeLength == 3) {
                desc = new StringBuilder(nodes[2]);
            }
            if (nodeLength > 3) {
                for (int i = 2; i < nodeLength; i ++) {
                    if (i != 2) {
                        desc.append(" ");
                    }
                    desc.append(nodes[i]);
                }
            }
            result.put(nodes[1], desc.toString());
        }
        return result;
    }
}
