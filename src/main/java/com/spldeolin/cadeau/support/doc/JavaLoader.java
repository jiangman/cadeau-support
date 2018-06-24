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

    public static List<TypeDeclaration> loadJavasAsType(String directoryPath) {
        List<TypeDeclaration> result = typeDeclarationHolder.get(directoryPath);
        // 命中缓存
        if (result != null) {
            return result;
        }
        log.info("读取[..." + directoryPath.substring(64, directoryPath.length()) + "]目录下的Java文件...");
        Iterator<File> javas = FileUtils.iterateFiles(new File(directoryPath), new String[] {"java"}, true);
        result = Lists.newArrayList();
        while (javas.hasNext()) {
            File java = javas.next();
            try {
                result.add(loadJavaAsType(java.getPath()));
            } catch (Exception e) {
                log.warn("[" + FilenameUtils.getBaseName(java.getName()) + "] 无法读取成TypeDeclaration，跳过");
            }
        }
        // 保存缓存
        typeDeclarationHolder.put(directoryPath, result);
        return result;
    }

    public static TypeDeclaration loadJavaAsType(String javaFilePath) throws IOException, ParseException {
        File srcFile = new File(javaFilePath);
        CompilationUnit unit;
        try (FileInputStream in = new FileInputStream(srcFile)) {
            unit = JavaParser.parse(in);
        }
        TypeDeclaration typeDeclaration = unit.getTypes().get(0);
        return typeDeclaration;
    }

}
