package com.spldeolin.cadeau.support.doc;

import static com.spldeolin.cadeau.support.constant.Abbreviation.sep;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import freemarker.template.TemplateException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/11
 */
@Log4j2
public class DocGenerator {

    public static void main(String[] args) {
        // 预读取
        JavaLoader.loadJavasAsTypes(DocConfig.controllerPackagePath);
        JavaLoader.loadJavasAsTypes(DocConfig.basePackagePath);

        // 解析目标控制器，生成freemarker实体对象
        List<MarkdownDocFTL> ftls = ControllerParser.parseController();

        // 生成文件
        for (MarkdownDocFTL ftl : ftls) {
            File mdFile = new File(DocConfig.TEMP_DIRECTORY_PATH + ftl.getDirectoryName() + sep + ftl.getFileName() + ".md");
            try {
                String ftlContent = FreeMarkerUtil.format(true, "markdown-doc.ftl", ftl);
                FileUtils.writeStringToFile(mdFile, ftlContent, StandardCharsets.UTF_8);
            } catch (IOException | TemplateException e) {
                log.error("格式化失败，跳过 [" + ftl.getHttpUrl() + "]", e);
            }
        }

        // 发送到ShowDoc
        //log.info("发送到ShowDoc");
        //ShowDocSender.sendToShowDocByFTLs(ftls);

        // 删除临时文件
        //log.info("删除临时文件");
        //try {
        //    FileUtils.deleteDirectory(new File(DocConfig.TEMP_DIRECTORY_PATH));
        //} catch (IOException ignored) {}

        log.info("正常结束");
    }

}
