package com.spldeolin.cadeau.support.doc;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.doc.helper.FieldDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.type.Type;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/10
 */
@Log4j2
public class SampleJsonParser {

    static {
        // 加载所有Java
        List<File> files = Lists.newArrayList(
                FileUtils.iterateFiles(new File(DocConfig.basePackagePath), new String[] {"java"}, true));
        // 文件名重名检查
        JavaLoader.sameFilenameCheck(files);
    }

    private static List<TypeDeclaration> recursiveTypes = Lists.newArrayList();

    public static void clearRecursiveTypes() {
        recursiveTypes = Lists.newArrayList();
    }

    @SneakyThrows
    public static void analysisField(StringBuilder sb, TypeDeclaration typeDeclaration, boolean ignoreUpdatedAt) {
        recursiveTypes.add(typeDeclaration);
        sb.append("{");
        for (BodyDeclaration bodyDeclaration : typeDeclaration.getMembers()) {
            if (bodyDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
                if (FieldDeclarationHelper.hasJsonIgnore(fieldDeclaration)) {
                    continue;
                }
                if (FieldDeclarationHelper.isSerialVersionUID(fieldDeclaration)) {
                    continue;
                }
                if (ignoreUpdatedAt && FieldDeclarationHelper.isUpdatedAt(fieldDeclaration)) {
                    continue;
                }
                Type fieldType = fieldDeclaration.getType();
                String fieldName = FieldDeclarationHelper.getFieldName(fieldDeclaration);
                sb.append(TypeHelper.wrapDoubleQuotes(fieldName));
                sb.append(":");
                if (TypeHelper.isSimpleType(fieldType)) {
                    sb.append(TypeHelper.sampleValueBySimpleType(fieldType));
                    sb.append(",");
                } else if (TypeHelper.isListOrSet(fieldType)) {
                    sb.append("[");
                    Type genericType = TypeHelper.getGenericType(fieldType);
                    if (TypeHelper.isSimpleType(genericType)) {
                        sb.append(TypeHelper.sampleValueBySimpleType(genericType));
                    } else {
                        // private List<User> users; 的情况，找到java文件，递归
                        String typeName = TypeHelper.getTypeName(TypeHelper.getGenericType(fieldType));
                        TypeDeclaration modelType = JavaLoader.loadClassByClassName(DocConfig.basePackagePath,
                                typeName);
                        StringBuilder sbEx = new StringBuilder(400);
                        if (!existInRecursiveTypes(modelType)) {
                            analysisField(sbEx, modelType, true);
                        }
                        sb.append(sbEx);
                    }
                    sb.append("]");
                    sb.append(",");
                } else {
                    // private User user; 的情况，找到java文件，递归
                    String typeName = TypeHelper.getTypeName(TypeHelper.getGenericType(fieldType));
                    TypeDeclaration modelType = JavaLoader.loadClassByClassName(DocConfig.basePackagePath,
                            typeName);
                    StringBuilder sbEx = new StringBuilder(400);
                    if (!existInRecursiveTypes(modelType)) {
                        analysisField(sbEx, modelType, true);
                    }

                    sb.append(sbEx);
                    sb.append(",");
                }
            }
        }
        sb.append("}");
    }

    public static TypeDeclaration getTypeFromTypeName(String typeName) {
        List<TypeDeclaration> typeDeclarations = JavaLoader.loadJavasAsTypes(DocConfig.basePackagePath);
        return typeDeclarations.stream().filter(t -> t.getName().equals(typeName)).findFirst().orElseThrow(
                () -> new RuntimeException("找不到" + typeName + "，原因是读取失败或是未考虑到的简单类型"));
    }

    private static boolean existInRecursiveTypes(TypeDeclaration type) {
        for (TypeDeclaration recursiveType : recursiveTypes) {
            if (type.getName().equals(recursiveType.getName())) {
                return true;
            }
        }
        return false;
    }

}