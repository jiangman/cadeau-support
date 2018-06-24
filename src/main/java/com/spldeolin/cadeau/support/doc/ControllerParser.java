package com.spldeolin.cadeau.support.doc;

import static com.google.common.collect.Lists.newArrayList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.spldeolin.cadeau.support.doc.helper.MethodDeclarationHelper;
import com.spldeolin.cadeau.support.doc.helper.TypeDeclarationHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import lombok.extern.log4j.Log4j2;

/**
 * 解析所有控制器，生成FTL
 *
 * @author Deolin 2018/06/11
 */
@Log4j2
public class ControllerParser {

    public static List<MarkdownDocFTL> parseController() {
        List<MarkdownDocFTL> ftls = newArrayList();
        log.info("开始解析...");
        // 获取所有控制器
        List<TypeDeclaration> typeDeclarations = JavaLoader.loadJavasAsTypes(DocConfig.controllerPackagePath);
        typeDeclarations.removeIf(type -> !TypeDeclarationHelper.hasControllerAnnotation(type) ||
                TypeDeclarationHelper.implementsErrorController(type));
        // 遍历每个请求方法
        for (TypeDeclaration controller : typeDeclarations) {
            for (MethodDeclaration requestMethod : listRequsetMethod(controller)) {
                MarkdownDocFTL ftl = new MarkdownDocFTL();
                String commonLog = "[" + controller.getName() + "] [" + requestMethod.getName() + "]";
                log.info("开始解析请求方法... " + commonLog);
                // 基本
                ftl.setDirectoryName(TypeDeclarationHelper.getFirstLineDescription(controller).replace('/', '-'));
                ftl.setFileName(MethodDeclarationHelper.getFirstLineDecription(requestMethod).replace('/', '-'));
                ftl.setCommonDesc(MethodDeclarationHelper.getDescription(requestMethod));
                ftl.setHttpUrl(TypeDeclarationHelper.getControllerMapping(controller) +
                        MethodDeclarationHelper.getMethodMapping(requestMethod));
                ftl.setHttpMethod(MethodDeclarationHelper.getMethodHttpMethod(requestMethod));
                // 参数
                try {
                    ParameterParser.parseParameter(ftl, requestMethod);
                    if (ftl.getIsBodySimpleType() && StringUtils.isBlank(ftl.getBodyDesc())) {
                        ftl.setDisplayBodyInfo(false);
                    } else {
                        ftl.setDisplayBodyInfo(true);
                    }
                } catch (Exception e) {
                    log.error("无法解析" + requestMethod.getName() + "的参数，跳过", e);
                    ftl.setParamShow(false);
                }
                // 返回值
                try {
                    // returnShow, returnJson, isRetrunSimpleType
                    ReturnParser.parseReturn(ftl, requestMethod);
                    // returnFields
                    ReturnParser.parseReturnFields(ftl, requestMethod);
                    if (ftl.getIsRetrunSimpleType() && StringUtils.isBlank(ftl.getReturnDesc())) {
                        ftl.setDisplayReturnInfo(false);
                    } else {
                        ftl.setDisplayReturnInfo(true);
                    }
                } catch (Exception e) {
                    log.error("无法解析" + requestMethod.getName() + "的返回值，跳过", e);
                    ftl.setReturnShow(false);
                }
                ftlSetAuthor(ftl, controller, requestMethod);
                ftls.add(ftl);
                log.info("...解析完毕 " + commonLog);
            }
        }
        ftls.forEach(log::info);

        return ftls;
    }

    private static List<MethodDeclaration> listRequsetMethod(TypeDeclaration typeDeclaration) {
        List<MethodDeclaration> result = newArrayList();
        List<BodyDeclaration> bodyDeclarations = typeDeclaration.getMembers();
        if (bodyDeclarations != null) {
            for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
                if (bodyDeclaration instanceof MethodDeclaration) {
                    MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
                    if (MethodDeclarationHelper.hasRequestMapping(methodDeclaration)) {
                        result.add(methodDeclaration);
                    }
                }
            }
        }
        return result;
    }

    private static void ftlSetAuthor(MarkdownDocFTL ftl, TypeDeclaration typeDeclaration,
            MethodDeclaration methodDeclaration) {
        String author = MethodDeclarationHelper.getAuthor(methodDeclaration);
        if (StringUtils.isBlank(author)) {
            author = TypeDeclarationHelper.getAuthor(typeDeclaration);
        }
        if (StringUtils.isBlank(author)) {
            author = "佚名 " + new SimpleDateFormat("yyyy/M/d HH:mm:ss").format(new Date());
        }
        ftl.setCommonDeveloper(author);
    }

}
