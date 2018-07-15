package com.spldeolin.cadeau.support.util;

import static com.spldeolin.cadeau.support.util.ConstantUtils.mavenJava;
import static com.spldeolin.cadeau.support.util.ConstantUtils.mavenRes;
import static com.spldeolin.cadeau.support.util.ConstantUtils.sep;

import java.io.File;

public class FileExistsUtils {

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
