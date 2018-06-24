package com.spldeolin.cadeau.support.doc;

import static com.spldeolin.cadeau.support.util.ConstantUtil.sep;

import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/11
 */
@Log4j2
public class DocConfig {

    public static final String TEMP_DIRECTORY_PATH = System.getProperty("user.dir") + sep + "doc-temp" + sep;

    static String basePackagePath = "C:\\java-development\\projects-repo\\women-projects\\船友-core\\src\\main\\java\\com\\womenhz\\shipmate\\core";

    static String controllerPackagePath = "C:\\java-development\\projects-repo\\women-projects\\船友-core\\src\\main\\java\\com\\womenhz\\shipmate\\core\\controller";

    static String apiKey = "2569e9546844d494566c0f343ceebd35801538712";

    static String apiToken = "5ef962f61d4b581c5337bb7e163077991964715205";

}
