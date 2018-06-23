/*
        TODO @JsonProperty
        TODO javadoc
        TODO “说明”
 */

package com.spldeolin.cadeau.support.doc;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.doc.helper.FieldDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeHelper;
import com.spldeolin.cadeau.support.util.StringCaseUtil;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
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

    private static List<File> files;

    static {
        // 加载所有Java
        files = Lists.newArrayList(FileUtils.iterateFiles(new File(DocConfig.basePackagePath), new String[] {"java"},
                true));
        // 文件名重名检查
        JavaLoader.sameFilenameCheck(files);
    }

    @SneakyThrows
    public static void analysisField(StringBuilder sb, TypeDeclaration typeDeclaration, boolean ignoreUpdatedAt) {
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
                        File fieldTypeJavaFile = filterFileByFieldTypeName(files,
                                TypeHelper.getTypeName(TypeHelper.getGenericType(fieldType)));
                        CompilationUnit unit;
                        try (FileInputStream in = new FileInputStream(fieldTypeJavaFile)) {
                            unit = JavaParser.parse(in);
                        }
                        TypeDeclaration modelType = unit.getTypes().get(0);
                        StringBuilder sbEx = new StringBuilder(400);
                        analysisField(sbEx, modelType, true);
                        sb.append(sbEx);
                    }
                    sb.append("]");
                    sb.append(",");
                } else {
                    // private User user; 的情况，找到java文件，递归
                    File fieldTypeJavaFile = filterFileByFieldTypeName(files, TypeHelper.getTypeName(fieldType));
                    CompilationUnit unit;
                    try (FileInputStream in = new FileInputStream(fieldTypeJavaFile)) {
                        unit = JavaParser.parse(in);
                    }
                    TypeDeclaration modelType = unit.getTypes().get(0);
                    StringBuilder sbEx = new StringBuilder(400);
                    analysisField(sbEx, modelType, true);

                    sb.append(sbEx);
                    sb.append(",");
                }
            }
        }
        sb.append("}");
    }

    public static TypeDeclaration getTypeFromTypeName(String typeName) {
        List<TypeDeclaration> typeDeclarations = JavaLoader.loadJavasAsType(DocConfig.basePackagePath);
        return typeDeclarations.stream().filter(t -> t.getName().equals(typeName)).findFirst().orElseThrow(
                () -> new RuntimeException("找不到" + typeName + "，原因由于它内部有Lambda、它是内部类、或是其他原因"));
    }

    private static File filterFileByFieldTypeName(List<File> files, String fieldName) {
        String filename = StringCaseUtil.upperFirstChar(fieldName);
        return files.stream().filter(
                file -> FilenameUtils.getBaseName(file.getName()).equals(filename)).findFirst().orElseThrow(
                () -> new RuntimeException("文件" + filename + ".java不存在"));
    }

}