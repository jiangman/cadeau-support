package com.spldeolin.cadeau.support.util;

import static com.spldeolin.cadeau.support.util.ConstantUtils.sep;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FreeMarkerUtil {

    /**
     * 根据ftl文件和数据，生成字符内容。
     *
     * @param isTemplate 这个ftl文件是否是模板文件
     * @param ftlFile ftl文件
     * @param datas 数据
     * @return 格式化后的内容
     */
    public static String format(boolean isTemplate, String ftlFile,
            Object datas) throws IOException, TemplateException {
        String extraPath = "";
        if (!isTemplate) {
            extraPath = "temp";
        }
        if (!ftlFile.endsWith(".ftl")) {
            ftlFile += ".ftl";
        }
        Version version = new Version("2.3.23");
        Configuration cfg = new Configuration(version);
        String folderPath = System.getProperty("user.dir") + sep + "src" + sep + "main" + sep + "resources" + sep
                + "freemarker-template" + sep + extraPath + sep;
        cfg.setDirectoryForTemplateLoading(new File(folderPath));
        Template template = cfg.getTemplate(ftlFile, "utf-8");
        try (StringWriter out = new StringWriter()) {
            template.process(datas, out);
            out.flush();
            return out.getBuffer().toString();
        }
    }

}
