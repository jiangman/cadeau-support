package com.spldeolin.cadeau.support.doc;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * @author deoli 2018/06/11
 */
@Log4j2
public class DocGenerator {

    @SneakyThrows
    public static void main(String[] args) {
        List<MarkdownDocFTL> ftls = ControllerParser.parseController();
        for (MarkdownDocFTL ftl : ftls) {
            try {
                String ftlContent = FreeMarkerUtil.format(true, "markdown-doc.ftl", ftl);
                FileUtils.writeStringToFile(new File("C:\\Users\\Deolin\\Desktop\\doc\\" + ftl.getDirectoryName() +
                                "\\" + ftl.getCommonDesc() + ".md"),
                        ftlContent, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("啊" + ftl);
            }
        }
        // 发送到ShowDoc
    }

}
