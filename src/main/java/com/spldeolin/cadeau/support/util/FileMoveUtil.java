package com.spldeolin.cadeau.support.util;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import lombok.extern.log4j.Log4j2;

/**
 * 文件移动工具类
 */
@Log4j2
public class FileMoveUtil {

    public static void move(File srcFile, File destFolder, boolean overWrite) {
        if (overWrite) {
            moveOverWrite(srcFile, destFolder);
        } else {
            moveNotOverWrite(srcFile, destFolder);
        }
    }

    /**
     * 移动文件，如果目标文件夹中已存在与源文件同名的文件，则重命名后移动。如果目标文件夹不存在，则创建。
     *
     * @param srcFile 待移动源文件
     * @param destFolder 移动目标文件夹
     */
    public static void moveNotOverWrite(File srcFile, File destFolder) {
        if (!srcFile.exists()) {
            log.error("源文件[" + srcFile.getPath() + "]不存在，无法移动");
            throw new RuntimeException();
        }
        // 目标文件存在则重命名
        File destFile = new File(destFolder.getPath() + File.separator + srcFile.getName());
        if (destFile.exists()) {
            destFile = renameFile(destFile, 1);
        }
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }
        try {
            FileUtils.copyFile(srcFile, destFile);
            srcFile.delete();
        } catch (IOException e) {
            log.error("checked", e);
            return;
        }
        if (srcFile.getName().equals(destFile.getName())) {
            log.info("文件[" + srcFile.getPath() + "]移动到了文件夹[" + destFolder.getPath() + "]");
        } else {
            log.info(
                    "文件[" + srcFile.getPath() + "]移动到了文件夹[" + destFolder.getPath() + "]，并重命名为[" + destFile.getName() + "]");
        }
    }

    public static void moveOverWrite(File srcFile, File destFolder) {
        if (!srcFile.exists()) {
            log.error("源文件[" + srcFile.getPath() + "]不存在，无法移动。");
            throw new RuntimeException();
        }
        // 目标文件存在则删除
        File destFile = new File(destFolder.getPath() + File.separator + srcFile.getName());
        if (destFile.exists()) {
            destFile.delete();
        }
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }
        try {
            FileUtils.copyFile(srcFile, destFile);
            srcFile.delete();
        } catch (IOException e) {
            log.error("checked", e);
            return;
        }
        log.info("文件[" + srcFile.getPath() + "]覆盖了文件[" + destFile.getPath() + "]");
    }

    // 递归重命名文件
    public static File renameFile(File file, int startSuffix) {
        File newfile = new File(file.getPath() + "." + startSuffix);
        if (newfile.exists()) {
            newfile = renameFile(file, startSuffix + 1);
        }
        return newfile;
    }

}
