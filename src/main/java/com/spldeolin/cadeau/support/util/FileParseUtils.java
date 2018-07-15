package com.spldeolin.cadeau.support.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.codec.digest.DigestUtils;
import lombok.extern.log4j.Log4j2;

/**
 * MultipartFile类型与File类型文件的解析工具类。
 */
@Log4j2
public class FileParseUtils {

    /**
     * 文件全名<br>
     * e.g.: HelloWorld.java
     */
    public static String fileFullName(File file) {
        return file.getName();
    }

    /**
     * 文件名<br>
     * e.g.: HelloWorld
     */
    public static String fileName(File file) {
        String fileFullName = fileFullName(file);
        return fileFullName.substring(0, fileFullName.lastIndexOf("."));
    }

    /**
     * 文件拓展名<br>
     * e.g.: java
     */
    public static String fileExtension(File file) {
        String fileFullName = fileFullName(file);
        return fileFullName.substring(fileFullName.lastIndexOf(".") + 1);
    }

    /**
     * 文件的md5值<br>
     * e.g.: b3af409bb8423187c75e6c7f5b683908
     */
    public static String md5(File file) throws IOException {
        return DigestUtils.md5Hex(new FileInputStream(file));
    }

    /**
     * 文件名追加后缀的文件全名
     * e.g.: HelloWorld-backup.java
     * p.s.: 本方法会自动规范到以中划线分割文件名与文件后缀。
     */
    public static String md5AddSuffix(File file, String suffix) throws IOException {
        String fileName = md5(file);
        String fileExtension = fileExtension(file);
        return fileName + "-" + suffix + "." + fileExtension;
    }

}
