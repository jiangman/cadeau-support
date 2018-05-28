package com.spldeolin.cadeau.support.client;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import lombok.SneakyThrows;

public class SqlTrimer {

    @SneakyThrows
    public static void main(String[] args) {
        File file = new File("");

        String regex = "AUTO_INCREMENT=[\\d]+ ";
        if (file.isDirectory()) {
            Iterator<File> files = FileUtils.iterateFiles(file, new String[] {"sql"}, false);
            while (files.hasNext()) {
                File f = files.next();
                String content = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
                content = content.replaceAll(regex, "");
                FileUtils.write(f, content, StandardCharsets.UTF_8);
            }
        } else {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            content = content.replaceAll(regex, "");
            FileUtils.write(file, content, StandardCharsets.UTF_8);
        }
    }

}
