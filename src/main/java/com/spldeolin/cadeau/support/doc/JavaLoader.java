package com.spldeolin.cadeau.support.doc;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.google.common.collect.Lists;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.TypeDeclaration;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * @author deoli 2018/06/11
 */
@Log4j2
public class JavaLoader {

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

    public static List<File> loadJavas(String directoryPath) {
        Iterator<File> javas = FileUtils.iterateFiles(new File(directoryPath), new String[] {"java"}, true);
        return Lists.newArrayList(javas);
    }

    public static List<TypeDeclaration> loadJavasAsType(String directoryPath) {
        Iterator<File> javas = FileUtils.iterateFiles(new File(directoryPath), new String[] {"java"}, true);
        List<TypeDeclaration> typeDeclarations = Lists.newArrayList();
        while (javas.hasNext()) {
            File java = javas.next();
            try {
                typeDeclarations.add(getJava(java.getPath()));
            } catch (Exception e) {
                //log.info(e.getMessage());
                log.error(FilenameUtils.getBaseName(java.getName()) + "无法解析成TypeDeclaration");
            }
        }
        return typeDeclarations;
    }

    @SneakyThrows
    public static TypeDeclaration getJava(String javaFilePath) {
        File srcFile = new File(javaFilePath);
        CompilationUnit unit;
        try (FileInputStream in = new FileInputStream(srcFile)) {
            unit = JavaParser.parse(in);
        }
        TypeDeclaration typeDeclaration = unit.getTypes().get(0);
        return typeDeclaration;
    }

}
