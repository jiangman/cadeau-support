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
        // 方法签名没有返回值时
        List<Parameter> parameters = requestMethod.getParameters();
        if (parameters == null || parameters.size() == 0) {
            return;
        }
        List<MarkdownDocFTL.PField> pFields = Lists.newArrayList();
        List<MarkdownDocFTL.BField> bFields = Lists.newArrayList();
        StringBuilder url = new StringBuilder(ftl.getHttpUrl());
        // 解析方法的JavaDoc的@param注释
        Map<String, String> paramTagDescs = parseParamTagDesc(requestMethod);
        // 遍历方法签名的参数
        int loopCount = 0;
        for (Parameter parameter : parameters) {
            if (ParameterHelper.isNotCustomType(parameter)) {
                continue;
            }
            // 非请求体
            if (ParameterHelper.isRequestParam(parameter) || ParameterHelper.isPathVariable(parameter)) {
                // 开启显示
                ftl.setParamShow(true);
                // 条目说明
                parseNonBodyField(pFields, parameter, paramTagDescs, loopCount, url);
            }
            // 请求体
            if (ParameterHelper.isRequestBody(parameter)) {
                // 开启显示
                ftl.setBodyShow(true);
                // 请求体说明
                ftl.setBodyDesc(paramTagDescs.get(ParameterHelper.getParameterName(parameter)));
                // JSON示例
                generateBodySampleJson(ftl, parameter);
                // 条目说明
                Type rawParameterType = ParameterHelper.getParameterType(parameter);
                Type genericParameterType = TypeHelper.getGenericType(rawParameterType);
                if (TypeHelper.isSimpleType(genericParameterType)) {
                    ftl.setIsBodySimpleType(true);
                    continue;
                }
                TypeDeclaration parameterType = SampleJsonParser.getTypeFromTypeName(
                        TypeHelper.getTypeName(genericParameterType));
                List<MarkdownDocFTL.BField> bodyFields = Lists.newArrayList();
                parseBodyField(bodyFields, parameterType.getMembers(), "", false);
                bFields.addAll(bodyFields);
            }
            loopCount++;
        }
        ftl.setHttpUrl(url.toString());
        ftl.setParamFields(pFields);
        ftl.setBodyFields(bFields);
    }

    private static void parseBodyField(List<MarkdownDocFTL.BField> bFields, List<BodyDeclaration> members,
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
                    MarkdownDocFTL.BField bField = new MarkdownDocFTL.BField();
                    if (FieldDeclarationHelper.isNotNullOrNotEmptyOrNotBlank(fieldDeclaration)) {
                        bField.setBodyRequired("必传");
                    } else {
                        bField.setBodyRequired("非必传");
                    }
                    boolean isListOrSet = FieldDeclarationHelper.isListOrSet(fieldDeclaration);
                    boolean isSimpleType = FieldDeclarationHelper.isSimpleType(fieldDeclaration);
                    String parameterName = FieldDeclarationHelper.getFieldName(fieldDeclaration);
                    if (StringUtils.isNotBlank(prefix)) {
                        parameterName = prefix + "." + parameterName;
                    }
                    bField.setBodyName(parameterName);
                    if (isSimpleType) {
                        bField.setBodyType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(
                                FieldDeclarationHelper.getFieldType(fieldDeclaration)));
                    } else if (isListOrSet) {
                        Type genericType = FieldDeclarationHelper.getGenericType(fieldDeclaration);
                        if (TypeHelper.isSimpleType(genericType)) {
                            bField.setBodyType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(TypeHelper.getTypeName(
                                    genericType)) + " Array");
                        } else {
                            bField.setBodyType("Object Array");
                        }
                    } else {
                        bField.setBodyType("Object");
                    }
                    bField.setBodyDesc(FieldDeclarationHelper.getFieldDesc(fieldDeclaration));
                    bFields.add(bField);
                    if (!TypeHelper.isSimpleType(FieldDeclarationHelper.getGenericType(fieldDeclaration))) {
                        String fieldTypeName;
                        if (isListOrSet) {
                            fieldTypeName = TypeHelper.getTypeName(FieldDeclarationHelper.getGenericType(
                                    fieldDeclaration));
                        } else {
                            fieldTypeName = FieldDeclarationHelper.getFieldType(fieldDeclaration);
                        }
                        TypeDeclaration fieldType = SampleJsonParser.getTypeFromTypeName(fieldTypeName);
                        parseBodyField(bFields, fieldType.getMembers(), parameterName, true);
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
        // ftl.bodyJson
        String sampleJson;
        Type rawParameterType = ParameterHelper.getParameterType(parameter);
        Type genericReturnType = TypeHelper.getGenericType(rawParameterType);
        if (TypeHelper.isSimpleType(genericReturnType)) {
            sampleJson = TypeHelper.sampleValueBySimpleType(genericReturnType);
        } else {
            TypeDeclaration parameterTypeDeclaration = SampleJsonParser.getTypeFromTypeName(
                    TypeHelper.getTypeName(genericReturnType));
            StringBuilder sb = new StringBuilder(400);
            SampleJsonParser.clearRecursiveTypes();
            SampleJsonParser.analysisField(sb, parameterTypeDeclaration, false);
            sampleJson = sb.toString();
            // 修剪掉多余的逗号
            sampleJson = JsonFormatUtil.trim(sampleJson);
        }
        // 包裹List或Page
        sampleJson = ReturnParser.wrapArrayOrPage(sampleJson, rawParameterType);
        // 美化JSON
        sampleJson = JsonFormatUtil.formatJson(sampleJson);
        ftl.setBodyJson(sampleJson);
    }

    private static Map<String, String> parseParamTagDesc(MethodDeclaration requestMethod) {
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
            if (nodeLength < 3 || !commentLine.startsWith("@param")) {
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
