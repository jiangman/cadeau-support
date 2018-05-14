package com.spldeolin.cadeau.support.util;

import java.io.File;

public class ConstantUtil {

    public final static char sep = File.separatorChar;

    public final static String br = System.getProperty("line.separator");

    public final static String mavenJava = sep + "src" + sep + "main" + sep + "java" + sep;

    public final static String mavenRes = sep + "src" + sep + "main" + sep + "resources" + sep;

    public final static String ftlPath = System.getProperty("user.dir") + mavenRes + "freemarker-template" + sep +
            "temp" + sep;

}
