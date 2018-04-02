package com.spldeolin.cadeau.support.util;

import static com.spldeolin.cadeau.support.util.ConstantUtil.mavenJava;
import static com.spldeolin.cadeau.support.util.ConstantUtil.mavenRes;
import static com.spldeolin.cadeau.support.util.ConstantUtil.sep;

import java.io.File;

public class FileExistsUtil {

    public static boolean referenceExist(String projectPath, String reference) {
        String fullPath = projectPath + mavenJava + reference.replace('.', sep);
        return new File(fullPath + "").exists() || new File(fullPath + ".java").exists() || new File(
                fullPath + ".xml").exists();
    }

    public static boolean resourceExist(String projectPath, String reference) {
        String fullPath = projectPath + mavenRes + reference.replace('.', sep);
        return new File(fullPath + "").exists() || new File(fullPath + ".java").exists() || new File(
                fullPath + ".xml").exists();
    }

}
