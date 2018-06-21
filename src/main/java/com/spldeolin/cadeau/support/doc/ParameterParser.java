package com.spldeolin.cadeau.support.doc;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.doc.helper.JsonTypeHelper;
import com.spldeolin.cadeau.support.doc.helper.ParameterHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeHelper;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
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
                    pField.setParamPlace("QUERY");
                } else {
                    pField.setParamPlace("PATH");
                }
                // paramType
                if (TypeHelper.isSimpleType(parameterType)) {
                    String parameterTypeName = TypeHelper.getTypeName(parameterType);
                    pField.setParamType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(parameterTypeName));
                }
                // paramDesc
                pField.setParamDesc(ParameterHelper.getDescription(parameter));
                // paramRequired TODO required=false 或是 指定了defaultValue（即便是""）
                if (ParameterHelper.isRequiredFalse(parameter) || ParameterHelper.isAssignedDefaultValue(parameter)) {
                    pField.setParamRequired("必传");
                } else {
                    pField.setParamRequired("非必传");
                }
            }
            if (ParameterHelper.isRequestBody(parameter)) {

            }
        }
    }

}
