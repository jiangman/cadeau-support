package com.spldeolin.cadeau.support.doc;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import com.spldeolin.cadeau.support.util.Https;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/24
 */
@Log4j2
public class ShowDocSender {

    private static final String url = "https://www.showdoc.cc/server/api/item/updateByApi";

    @SneakyThrows
    public static void sendToShowDocByFTLs(List<MarkdownDocFTL> ftls) {
        List<ShowDocDTO> showDocDTOs = Lists.newArrayList();
        for (int i = 0; i < ftls.size(); i++) {
            MarkdownDocFTL ftl = ftls.get(i);
            String content = FreeMarkerUtil.format(true, "markdown-doc.ftl", ftl);
            ShowDocDTO showDocDTO = new ShowDocDTO();
            showDocDTO.setApi_key(DocConfig.apiKey);
            showDocDTO.setApi_token(DocConfig.apiToken);
            showDocDTO.setCat_name_sub(ftl.getDirectoryName());
            showDocDTO.setPage_title(ftl.getFileName());
            showDocDTO.setPage_content(content);
            showDocDTO.setS_number(i);
            showDocDTOs.add(showDocDTO);
        }
        for (ShowDocDTO showDocDTO : showDocDTOs) {
            Thread.sleep(500);
            Https.postForm(url, showDocDTO);
        }
    }

}
