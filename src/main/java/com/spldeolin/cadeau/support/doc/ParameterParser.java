package com.spldeolin.cadeau.support.doc;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.cadeau.support.doc.helper.FieldDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.JsonTypeHelper;
import com.spldeolin.cadeau.support.doc.helper.ParameterHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeHelper;
import com.spldeolin.cadeau.support.util.JsonFormatUtil;
import com.spldeolin.cadeau.support.util.Nulls;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.comments.Comment;
import japa.parser.ast.type.Type;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/11
 */
@Log4j2
public class ParameterParser {

    public static void parseParameter(MarkdownDocFTL ftl, MethodDeclaration requestMethod) {
        List<Parameter> parameters = requestMethod.getParameters();
        if (parameters == null || parameters.size() == 0) {
            ftl.setParamShow(false);
            return;
        }
        Map<String, String> descs = parseParameterDesc(requestMethod);
        ftl.setParamShow(true);
        ftl.setParamBodyShow(false);
        List<MarkdownDocFTL.PField> pFields = Lists.newArrayList();
        StringBuilder url = new StringBuilder(ftl.getHttpUrl());
        int loopCount = 0;
        for (Parameter parameter : parameters) {
            // 非请求体
            if (ParameterHelper.isRequestParam(parameter) || ParameterHelper.isPathVariable(parameter)) {
                // 说明
                parseNonBodyField(pFields, parameter, descs, loopCount, url);
            }
            // 请求体
            if (ParameterHelper.isRequestBody(parameter)) {
                // 开启显示
                ftl.setParamBodyShow(true);
                // sampleJson
                generateBodySampleJson(ftl, parameter);
                // 说明

                /*
                    @see ReturnParser.parseReturnFields()
                 */

                // 获取参数类型
                Type rawParameterType = ParameterHelper.getParameterType(parameter);
                Type genericParameterType = TypeHelper.getGenericType(rawParameterType);
                // TODO 暂时不做简单类型BODY的说明
                if (TypeHelper.isSimpleType(genericParameterType)) {
                    continue;
                }
                TypeDeclaration parameterType = SampleJsonParser.getTypeFromTypeName(
                        TypeHelper.getTypeName(genericParameterType));
                List<MarkdownDocFTL.PField> bodyFields = Lists.newArrayList();
                parseBodyField(bodyFields, parameterType.getMembers(), "", false);
                pFields.addAll(bodyFields);
            }
            loopCount++;
        }
        ftl.setHttpUrl(url.toString());
        ftl.setParamFields(pFields);
    }

    private static void parseBodyField(List<MarkdownDocFTL.PField> pFields, List<BodyDeclaration> members,
            String prefix, boolean ignoreUpdatedAt) {
        if (members != null) {
            for (BodyDeclaration bodyDeclaration : members) {
                if (bodyDeclaration instanceof FieldDeclaration) {
                    FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
                    if (FieldDeclarationHelper.isSerialVersionUID(fieldDeclaration)
                            || FieldDeclarationHelper.hasJsonIgnore(fieldDeclaration)) {
                        continue;
                    }
                    if (ignoreUpdatedAt && FieldDeclarationHelper.isUpdatedAt(fieldDeclaration)) {
                        continue;
                    }
                    MarkdownDocFTL.PField pField = new MarkdownDocFTL.PField();
                    pField.setParamRequired("必传");
                    pField.setParamPlace("body");
                    boolean isListOrSet = FieldDeclarationHelper.isListOrSet(fieldDeclaration);
                    boolean isSimpleType = FieldDeclarationHelper.isSimpleType(fieldDeclaration);
                    String parameterName = FieldDeclarationHelper.getFieldName(fieldDeclaration);
                    if (StringUtils.isNotBlank(prefix)) {
                        parameterName = prefix + "." + parameterName;
                    }
                    pField.setParamName(parameterName);
                    if (isSimpleType) {
                        pField.setParamType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(
                                FieldDeclarationHelper.getFieldType(fieldDeclaration)));
                    } else if (isListOrSet) {
                        Type genericType = FieldDeclarationHelper.getGenericType(fieldDeclaration);
                        if (TypeHelper.isSimpleType(genericType)) {
                            pField.setParamType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(TypeHelper.getTypeName(
                                    genericType)) + " Array");
                        } else {
                            pField.setParamType("Object Array");
                        }
                    } else {
                        pField.setParamType("Object");
                    }
                    pField.setParamDesc(FieldDeclarationHelper.getFieldDesc(fieldDeclaration));
                    pFields.add(pField);
                    if (!isSimpleType) {
                        String fieldTypeName;
                        if (isListOrSet) {
                            fieldTypeName = TypeHelper.getTypeName(FieldDeclarationHelper.getGenericType(
                                    fieldDeclaration));
                        } else {
                            fieldTypeName = FieldDeclarationHelper.getFieldType(fieldDeclaration);
                        }
                        TypeDeclaration fieldType = SampleJsonParser.getTypeFromTypeName(fieldTypeName);
                        parseBodyField(pFields, fieldType.getMembers(), parameterName, true);
                    }
                }
            }
        }
    }

    private static void parseNonBodyField(List<MarkdownDocFTL.PField> pFields, Parameter parameter,
            Map<String, String> descs, int loopCount, StringBuilder url) {
        MarkdownDocFTL.PField pField = new MarkdownDocFTL.PField();
        String parameterName = ParameterHelper.getParameterName(parameter);
        Type parameterType = ParameterHelper.getParameterType(parameter);
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
        String paramDesc = descs.get(parameterName);
        if (StringUtils.isBlank(paramDesc)) {
            paramDesc = ParameterHelper.getDescription(parameter);
        }
        if (StringUtils.isBlank(paramDesc)) {
            paramDesc = "　";
        }
        pField.setParamDesc(paramDesc);
        // paramRequired
        if (ParameterHelper.isRequiredFalse(parameter) || ParameterHelper.isAssignedDefaultValue(parameter)) {
            pField.setParamRequired("非必传");
        } else {
            pField.setParamRequired("必传");
        }
        pFields.add(pField);
        // 在URL后追加?a=&b=
        if (ParameterHelper.isRequestParam(parameter)) {
            if (loopCount == 0) {
                url.append("?");
            } else {
                url.append("&");
            }
            url.append(parameterName);
            url.append("=");
        }
    }

    private static void generateBodySampleJson(MarkdownDocFTL ftl, Parameter parameter) {
        // ftl.paramBodyJson
        String sampleJson;
        Type rawParameterType = ParameterHelper.getParameterType(parameter);
        Type genericReturnType = TypeHelper.getGenericType(rawParameterType);
        if (TypeHelper.isSimpleType(genericReturnType)) {
            sampleJson = TypeHelper.sampleValueBySimpleType(genericReturnType);
        } else {
            TypeDeclaration parameterTypeDeclaration = SampleJsonParser.getTypeFromTypeName(
                    TypeHelper.getTypeName(genericReturnType));
            StringBuilder sb = new StringBuilder(400);
            SampleJsonParser.analysisField(sb, parameterTypeDeclaration, false);
            sampleJson = sb.toString();
            // 修剪掉多余的逗号
            sampleJson = JsonFormatUtil.trim(sampleJson);
        }
        // 包裹List或Page
        sampleJson = ReturnParser.wrapArrayOrPage(sampleJson, rawParameterType);
        // 美化JSON
        sampleJson = JsonFormatUtil.formatJson(sampleJson);
        ftl.setParamBodyJson(sampleJson);
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
                for (int i = 2; i < nodeLength; i++) {
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
