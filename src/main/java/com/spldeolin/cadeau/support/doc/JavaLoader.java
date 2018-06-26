package com.spldeolin.cadeau.support.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.google.common.collect.Lists;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/11
 */
@Log4j2
public class JavaLoader {

    private static final Map<String, List<TypeDeclaration>> typeDeclarationHolder = new ConcurrentHashMap<>();

    public static void sameFilenameCheck(List<File> javas) {
        for (File java : javas) {
            String filename = FilenameUtils.getBaseName(java.getName());
            boolean same = false;
            for (File java2 : javas) {
                if (FilenameUtils.getBaseName(java2.getName()).equals(filename)) {
                    if (same) {
                        throw new RuntimeException("暂时不支持存在同名文件的项目");
                    } else {
                        same = true;
                    }
                }
            }
        }
    }

    /**
     * 读取指定目录下的所有Java文件，转化为TypeDeclaration的集合
     */
    public static List<TypeDeclaration> loadJavasAsTypes(String directoryPath) {
        List<TypeDeclaration> result = typeDeclarationHolder.get(directoryPath);
        // 命中缓存
        if (result != null) {
            return result;
        }
        log.info("开始读取Java... [" + directoryPath + "]");
        Iterator<File> javas = FileUtils.iterateFiles(new File(directoryPath), new String[] {"java"}, true);
        result = Lists.newArrayList();
        while (javas.hasNext()) {
            File java = javas.next();
            try {
                result.addAll(loadJavaAsTypes(java.getPath()));
            } catch (Exception e) {
                log.warn("读取失败，跳过 [" + FilenameUtils.getBaseName(java.getName()) + ".java]");
            }
        }
        log.info("...读取完毕");
        // 保存缓存
        typeDeclarationHolder.put(directoryPath, result);
        return result;
    }

    /**
     * 读取指定Java文件，将其声明的所有类（包括内部类）转化为TypeDeclaration的集合
     */
    public static List<TypeDeclaration> loadJavaAsTypes(String javaFilePath) throws IOException, ParseException {
        File srcFile = new File(javaFilePath);
        String filename = FilenameUtils.getName(javaFilePath);
        CompilationUnit unit;
        try (FileInputStream in = new FileInputStream(srcFile)) {
            unit = JavaParser.parse(in);
        }
        List<TypeDeclaration> typeDeclarations = unit.getTypes();
        List<TypeDeclaration> result = Lists.newArrayList(typeDeclarations);

        for (int i = 0; i < typeDeclarations.size(); i++) {
            TypeDeclaration typeDeclaration = typeDeclarations.get(i);
            String className = typeDeclaration.getName();
            if (i != 0) {
                log.info("发现非public类 [" + filename + "] [" + className + "]");
            }
            List<BodyDeclaration> bodies = typeDeclaration.getMembers();
            if (bodies != null) {
                for (BodyDeclaration body : bodies) {
                    if (body instanceof ClassOrInterfaceDeclaration) {
                        ClassOrInterfaceDeclaration inner = (ClassOrInterfaceDeclaration) body;
                        log.info("发现内部类 [" + filename + "] [" + className + "] [" + inner.getName() + "]");
                        result.add(inner);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 通过路径名和类名定位到指定类（普通类、内部类或非default类）
     */
    public static TypeDeclaration loadClassByClassName(String directoryPath, String className) {
        List<TypeDeclaration> types = loadJavasAsTypes(directoryPath);
        for (TypeDeclaration type : types) {
            if (type.getName().equals(className)) {
                return type;
            }
        }
        throw new RuntimeException("无法定位到指定类");
    }

}
