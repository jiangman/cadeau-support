package com.spldeolin.cadeau.support.doc;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.doc.exception.ReturnParseException;
import com.spldeolin.cadeau.support.doc.helper.FieldDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.JsonTypeHelper;
import com.spldeolin.cadeau.support.doc.helper.MethodDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeHelper;
import com.spldeolin.cadeau.support.util.JsonFormatUtil;
import com.spldeolin.cadeau.support.util.Nulls;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.comments.Comment;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/11
 */
@Log4j2
public class ReturnParser {

    /**
     * generate json sample
     */
    public static void parseReturn(MarkdownDocFTL ftl, MethodDeclaration requestMethod) {
        // 获取返回值
        Type rawReturnType = MethodDeclarationHelper.getReturnType(requestMethod);
        // 返回值是void
        if (rawReturnType instanceof VoidType) {
            return;
        }
        ftl.setReturnShow(true);
        ftl.setReturnDesc(parseReturnDesc(requestMethod));
        // 生成返回值示例
        String sampleJson;
        Type genericReturnType = TypeHelper.getGenericType(rawReturnType);
        if (TypeHelper.isSimpleType(genericReturnType)) {
            sampleJson = TypeHelper.sampleValueBySimpleType(genericReturnType);
            ftl.setIsRetrunSimpleType(true);
        } else {
            String genericReturnTypeName = TypeHelper.getTypeName(genericReturnType);
            if (StringUtils.equalsAny(genericReturnTypeName, "Object", "RequestResult")) {
                throw new ReturnParseException("方法签名的返回类型为" + genericReturnTypeName + "，无法得知运行期类型");
            }
            TypeDeclaration returnTypeDeclaration = SampleJsonParser.getTypeFromTypeName(genericReturnTypeName);
            StringBuilder sb = new StringBuilder(400);
            SampleJsonParser.analysisField(sb, returnTypeDeclaration, false);
            sampleJson = sb.toString();
            // 修剪掉多余的逗号
            sampleJson = JsonFormatUtil.trim(sampleJson);
        }
        // 包裹List或Page
        sampleJson = wrapArrayOrPage(sampleJson, rawReturnType);
        // 美化JSON
        sampleJson = JsonFormatUtil.formatJson(sampleJson);
        ftl.setReturnJson(sampleJson);
    }

    public static void parseReturnFields(MarkdownDocFTL ftl, MethodDeclaration requestMethod) {
        // 获取返回值类型
        Type rawReturnType = MethodDeclarationHelper.getReturnType(requestMethod);
        // 返回值是void
        if (rawReturnType instanceof VoidType) {
            return;
        }
        Type genericReturnType = TypeHelper.getGenericType(rawReturnType);
        if (TypeHelper.isSimpleType(genericReturnType)) {
            return;
        }
        TypeDeclaration returnType = SampleJsonParser.getTypeFromTypeName(TypeHelper.getTypeName(genericReturnType));
        List<MarkdownDocFTL.RField> rFields = Lists.newArrayList();
        generateRField(rFields, returnType.getMembers(), "", false);
        ftl.setReturnFields(rFields);
    }

    private static void generateRField(List<MarkdownDocFTL.RField> rFields, List<BodyDeclaration> members,
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
                    MarkdownDocFTL.RField rField = new MarkdownDocFTL.RField();
                    boolean isListOrSet = FieldDeclarationHelper.isListOrSet(fieldDeclaration);
                    boolean isSimpleType = FieldDeclarationHelper.isSimpleType(fieldDeclaration);
                    String returnName = FieldDeclarationHelper.getFieldName(fieldDeclaration);
                    if (StringUtils.isNotBlank(prefix)) {
                        returnName = prefix + "." + returnName;
                    }
                    rField.setReturnName(returnName);
                    if (isSimpleType) {
                        rField.setReturnType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(
                                FieldDeclarationHelper.getFieldType(fieldDeclaration)));
                    } else if (isListOrSet) {
                        Type genericType = FieldDeclarationHelper.getGenericType(fieldDeclaration);
                        if (TypeHelper.isSimpleType(genericType)) {
                            rField.setReturnType(JsonTypeHelper.getJsonTypeFromJavaSimpleType(TypeHelper.getTypeName(
                                    genericType)) + " Array");
                        } else {
                            rField.setReturnType("Object Array");
                        }
                    } else {
                        rField.setReturnType("Object");
                    }
                    rField.setReturnDesc(FieldDeclarationHelper.getFieldDesc(fieldDeclaration));
                    rFields.add(rField);
                    if (!isSimpleType) {
                        String fieldTypeName;
                        if (isListOrSet) {
                            fieldTypeName = TypeHelper.getTypeName(FieldDeclarationHelper.getGenericType(
                                    fieldDeclaration));
                        } else {
                            fieldTypeName = FieldDeclarationHelper.getFieldType(fieldDeclaration);
                        }
                        TypeDeclaration fieldType = SampleJsonParser.getTypeFromTypeName(fieldTypeName);
                        generateRField(rFields, fieldType.getMembers(), returnName, true);
                    }
                }
            }
        }
    }

    public static String wrapArrayOrPage(String json, Type returnType) {
        boolean isArray = TypeHelper.isListOrSet(returnType);
        boolean isPage = TypeHelper.isPage(returnType);
        if (isArray) {
            return "[" + json + "]";
        }
        if (isPage) {
            return "{\"pageNo\":1,\"hasPreviousPage\":false,\"hasNextPage\":true,\"entitiesInPage\":[" + json +
                    "],\"pagesCount\":9,\"total\":65535}";
        }
        return json;
    }

    private static String parseReturnDesc(MethodDeclaration requestMethod) {
        Comment comment = requestMethod.getComment();
        if (comment == null) {
            return null;
        }
        List<String> commentLines = newArrayList(Nulls.toEmpty(comment.getContent()).split("\n"));
        for (String commentLine : commentLines) {
            commentLine = commentLine.replaceFirst("\\*", "").trim();
            String[] nodes = commentLine.split(" ");
            if (!commentLine.startsWith("@return ")) {
                continue;
            }
            return commentLine.replace("@return ", "");
        }
        return null;
    }

}
