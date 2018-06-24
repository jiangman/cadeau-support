package com.spldeolin.cadeau.support.doc;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.util.FreeMarkerUtil;
import com.spldeolin.cadeau.support.util.Https;
import com.spldeolin.cadeau.support.util.Jsons;
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
        log.info("开始发送到ShowDoc...");
        List<ShowDocDTO> showDocDTOs = Lists.newArrayList();
        for (int i = 0; i < ftls.size(); i++) {
            MarkdownDocFTL ftl = ftls.get(i);
            String content = FreeMarkerUtil.format(true, "markdown-doc.ftl", ftl);
            ShowDocDTO showDocDTO = new ShowDocDTO();
            showDocDTO.setApi_key(DocConfig.apiKey);
            showDocDTO.setApi_token(DocConfig.apiToken);
            showDocDTO.setCat_name("未分类");
            showDocDTO.setCat_name_sub(ftl.getDirectoryName());
            showDocDTO.setPage_title(ftl.getFileName());
            showDocDTO.setPage_content(content);
            //showDocDTO.setS_number(i);
            showDocDTOs.add(showDocDTO);
        }
        showDocDTOs.stream().map(ShowDocDTO::getCat_name_sub).forEach(log::info);
        int size = showDocDTOs.size();
        for (int i = 0; i < size; i++) {
            ShowDocDTO showDocDTO = showDocDTOs.get(i);
            Thread.sleep(500);
            String resp = Https.postForm(url, showDocDTO);
            String commonLog = "(" + (i + 1) + "/" + size + ") [" + showDocDTO.getCat_name() + "] [" +
                    showDocDTO.getCat_name_sub() + "] [" + showDocDTO.getPage_title() + "]";
            if ("0".equals(Jsons.getValue(resp, "error_code"))) {
                log.info("发送成功 " + commonLog);
            } else {
                log.error("发送失败，跳过 " + commonLog);
            }
        }
        log.info("...发送完毕");
    }

}
