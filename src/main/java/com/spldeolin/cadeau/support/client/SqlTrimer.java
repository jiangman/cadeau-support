package com.spldeolin.cadeau.support.client;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import lombok.SneakyThrows;

public class SqlTrimer {

    @SneakyThrows
    public static void main(String[] args) {
        String regex = "AUTO_INCREMENT=[\\d]+ ";
        File file = new File("C:\\Users\\deoli\\Desktop\\orderingtest_dubbo.sql");

        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        content = content.replaceAll(regex, "");
        FileUtils.write(file, content, StandardCharsets.UTF_8);

    }

}
