package com.spldeolin.cadeau.support.doc;

import static com.spldeolin.cadeau.support.util.ConstantUtils.sep;

import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/11
 */
@Log4j2
public class DocConfig {

    public static final String TEMP_DIRECTORY_PATH = System.getProperty("user.dir") + sep + "doc-temp" + sep;

    static String basePackagePath = "C:\\java-development\\projects-repo\\women-projects\\sf-sparepart-end\\src\\main\\java\\com\\womenhz\\sfsparepart";

    static String controllerPackagePath = "C:\\java-development\\projects-repo\\women-projects\\sf-sparepart-end\\src\\main\\java\\com\\womenhz\\sfsparepart\\controller";

    static String apiKey = "6f73675a637d1215cb237a68f053c0181418538293";

    static String apiToken = "0a37fe2f77d7e83c2b31cc0fa67ae4c6675739166";

}
