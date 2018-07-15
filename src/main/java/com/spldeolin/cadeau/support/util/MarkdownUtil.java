package com.spldeolin.cadeau.support.util;

import java.util.Collections;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataSet;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/07/15
 */
@UtilityClass
@Log4j2
public class MarkdownUtil {

    public static String toHtml(String markdownContent) {
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        options.set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        Node document = parser.parse(markdownContent);
        String htmlContent = renderer.render(document);
        return htmlContent;
    }

}
