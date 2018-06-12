/*
        TODO @JsonProperty
        TODO javadoc
        TODO “说明”
 */

package com.spldeolin.cadeau.support.doc;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import com.spldeolin.cadeau.support.util.StringCaseUtil;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/10
 */
@Log4j2
public class SampleJsonGenerator {

    private static List<File> files;

    @SneakyThrows
    public static void main(String[] args) {
        String basePackagePath = "C:\\java-development\\projects-repo\\deolin-projects\\beginning-mind\\src\\main\\java\\com\\spldeolin\\beginningmind\\core";
        files = newArrayList(FileUtils.iterateFiles(new File(basePackagePath), new String[] {"java"}, true));

        sameNameCheck();
        JavaLoader.loadJavasAsType(
                "C:\\java-development\\projects-repo\\deolin-projects\\beginning-mind\\src\\main\\java\\com\\spldeolin\\beginningmind\\core\\controller");

        // 打开文件
        File srcFile = new File(
                "C:\\java-development\\projects-repo\\deolin-projects\\beginning-mind\\src\\main\\java\\com\\spldeolin\\beginningmind\\core\\controller\\SecurityRoleController.java");
        CompilationUnit unit;
        try (FileInputStream in = new FileInputStream(srcFile)) {
            unit = JavaParser.parse(in);
        }

        TypeDeclaration modelType = unit.getTypes().get(0);
        StringBuilder sb = new StringBuilder(400);

        analysisField(sb, modelType, false);

        log.info(sb.toString());
    }

    @SneakyThrows
    public static void analysisField(StringBuilder sb, TypeDeclaration typeDeclaration, boolean ignoreUpdatedAt) {
        sb.append("{");
        for (BodyDeclaration bodyDeclaration : typeDeclaration.getMembers()) {
            if (bodyDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
                if (hasJsonIgnore(fieldDeclaration)) {
                    continue;
                }
                if (isSerialVersionUID(fieldDeclaration)) {
                    continue;
                }
                if (ignoreUpdatedAt && isUpdatedAt(fieldDeclaration)) {
                    continue;
                }
                Type fieldType = fieldDeclaration.getType();
                String fieldName = fieldNameByField(fieldDeclaration);
                sb.append(wrapDoubleQuotes(fieldName));
                sb.append(":");
                if (isSimpleType(fieldType)) {
                    sb.append(sampleValueBySimpleType(fieldType));
                    sb.append(",");
                } else if (isListOrSet(fieldType)) {
                    sb.append("[");
                    Type genericType = getGenericType(fieldType);
                    if (isSimpleType(genericType)) {
                        sb.append(sampleValueBySimpleType(genericType));
                    } else {
                        // private List<User> users; 的情况，找到java文件，递归
                        File fieldTypeJavaFile = filterFileByFieldTypeName(files,
                                getTypeName(getGenericType(fieldType)));
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
                    File fieldTypeJavaFile = filterFileByFieldTypeName(files, getTypeName(fieldType));
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
        // 修剪掉多余的逗号
        String json = sb.toString();
        json = json.replace(",]", "]");
        json = json.replace(",}", "}");
        sb = new StringBuilder(json);
    }

    private static String fieldNameByField(FieldDeclaration fieldDeclaration) {
        return fieldDeclaration.getVariables().get(0).getId().getName();
    }

    private static boolean hasJsonIgnore(FieldDeclaration fieldDeclaration) {
        List<AnnotationExpr> annotations = fieldDeclaration.getAnnotations();
        if (annotations != null) {
            for (AnnotationExpr annotation : annotations) {
                if (annotation.getName().getName().equals("JsonIgnore")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSerialVersionUID(FieldDeclaration fieldDeclaration) {
        return fieldNameByField(fieldDeclaration).equals("serialVersionUID");
    }

    private static boolean isUpdatedAt(FieldDeclaration fieldDeclaration) {
        return fieldNameByField(fieldDeclaration).equals("updatedAt");
    }

    private static boolean isSimpleType(Type fieldType) {
        List<SimpleType> simpleTypes = newArrayList(SimpleType.values());
        List<String> simpleTypeNames = simpleTypes.stream().map(SimpleType::getName).collect(Collectors.toList());
        return typeJudgement(fieldType, simpleTypeNames.toArray(new String[] {}));
    }

    private static boolean isListOrSet(Type fieldType) {
        return typeJudgement(fieldType, "List", "Set");
    }

    public static String getTypeName(Type fieldType) {
        if (fieldType instanceof ReferenceType) {
            ReferenceType fieldTypeEx = (ReferenceType) fieldType;
            Type fieldTypeExType = fieldTypeEx.getType();
            if (fieldTypeExType instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType fieldTypeExTypeEx = (ClassOrInterfaceType) fieldTypeExType;
                return fieldTypeExTypeEx.getName();
            }
        }
        // 泛型会进入这个分支
        if (fieldType instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType fieldTypeExTypeEx = (ClassOrInterfaceType) fieldType;
            return fieldTypeExTypeEx.getName();
        }
        // 基本数据类型会进入这个分支
        if (fieldType instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) fieldType;
            return primitiveType.getType().name();
        }
        return null;
    }

    private static Type getGenericType(Type fieldType) {
        if (fieldType instanceof ReferenceType) {
            ReferenceType fieldTypeEx = (ReferenceType) fieldType;
            Type fieldTypeExType = fieldTypeEx.getType();
            if (fieldTypeExType instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType fieldTypeExTypeEx = (ClassOrInterfaceType) fieldTypeExType;
                Object arg = fieldTypeExTypeEx.getTypeArgs().get(0);
                if (arg instanceof ReferenceType) {
                    ReferenceType argEx = (ReferenceType) arg;
                    Type argExType = argEx.getType();
                    return argExType;
                }
            }
        }
        return null;
    }

    public static boolean typeJudgement(Type fieldType, String... typeNames) {
        return StringUtils.equalsAny(getTypeName(fieldType), typeNames);
    }

    private static String wrapDoubleQuotes(String string) {
        return "\"" + string + "\"";
    }

    private static String sampleValueBySimpleType(Type fieldType) {
        String actualTypeName = getTypeName(fieldType);
        String sampleValue = "null";
        for (SimpleType simpleType : SimpleType.values()) {
            if (simpleType.getName().equals(actualTypeName)) {
                sampleValue = simpleType.getSampleValue();
                for (SimpleType.JsonString jsonString : SimpleType.JsonString.values()) {
                    if (jsonString.getName().equals(actualTypeName)) {
                        sampleValue = wrapDoubleQuotes(sampleValue);
                        break;
                    }
                }
                break;
            }
        }
        return sampleValue;
    }

    private static File filterFileByFieldTypeName(List<File> files, String fieldName) {
        String filename = StringCaseUtil.upperFirstChar(fieldName);
        return files.stream().filter(
                file -> FilenameUtils.getBaseName(file.getName()).equals(filename)).findFirst().orElseThrow(
                () -> new RuntimeException("文件" + filename + ".java存在"));
    }

    private static void sameNameCheck() {
        for (File file : files) {
            String filename = FilenameUtils.getBaseName(file.getName());
            boolean same = false;
            for (File file2 : files) {
                if (FilenameUtils.getBaseName(file2.getName()).equals(filename)) {
                    if (same) {
                        throw new RuntimeException("暂时不支持存在同名文件的项目");
                    } else {
                        same = true;
                    }
                }
            }
        }
    }

}