package com.spldeolin.cadeau.support.util;

import static com.spldeolin.cadeau.support.util.ConstantUtils.br;
import static com.spldeolin.cadeau.support.util.ConstantUtils.mavenJava;
import static com.spldeolin.cadeau.support.util.ConstantUtils.mavenRes;
import static com.spldeolin.cadeau.support.util.ConstantUtils.sep;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class ConfigUtils {

    @Getter
    @Setter
    private static Properties props;

    /**
     * 作者
     */
    @Getter
    @Setter
    private static String author;

    /**
     * 生成日期
     */
    @Getter
    @Setter
    private static String date;

    /**
     * 表名数组
     */
    @Getter
    @Setter
    private static String[] tableNames;

    /**
     * 模型中文名数组
     */
    @Getter
    @Setter
    private static String[] modelCns;

    /**
     * 数据库URL
     */
    @Getter
    @Setter
    private static String mysqlUrl;

    /**
     * 数据库名
     */
    @Getter
    @Setter
    private static String mysqlDatabase;

    /**
     * 数据库用户名
     */
    @Getter
    @Setter
    private static String mysqlUsername;

    /**
     * 数据库密码
     */
    @Getter
    @Setter
    private static String mysqlPassword;

    /**
     * 文件重名时是否覆盖
     */
    @Getter
    @Setter
    private static Boolean overWrite;

    /**
     * 项目路径
     */
    @Getter
    @Setter
    private static String projectPath;

    /**
     * 基础包名
     */
    @Getter
    @Setter
    private static String basePackage;

    /**
     * mapper.xml文件夹的Reference
     */
    @Getter
    @Setter
    private static String mapperFolder;

    /**
     * 通用Service接口的Reference
     */
    @Getter
    @Setter
    private static String commonService;

    /**
     * 通用ServiceImpl抽象类的Reference
     */
    @Getter
    @Setter
    private static String commonServiceImpl;

    /**
     * 通用Mapper接口的Reference
     */
    @Getter
    @Setter
    private static String commonMapper;

    /**
     * TextOption的Reference
     */
    @Getter
    @Setter
    private static String option;

    /**
     * Page的Reference
     */
    @Getter
    @Setter
    private static String page = "";

    /**
     * PageParam的Reference
     */
    @Getter
    @Setter
    private String pageParam = "";

    /**
     * ServiceException的Reference
     */
    @Getter
    @Setter
    private static String serviceException = "";

    /**
     * 表注释数组
     */
    @Getter
    @Setter
    private static String[] tableComments;

    /**
     * 最终类级注释tag
     */
    @Getter
    @Setter
    private static String classDocEnd;

    // 最终包名 start

    @Getter
    @Setter
    private static String daoPackage;

    @Getter
    @Setter
    private static String mapperPackage;

    @Getter
    @Setter
    private static String modelPackage;

    @Getter
    @Setter
    private static String inputPackage;

    @Getter
    @Setter
    private static String servicePackage;

    @Getter
    @Setter
    private static String serviceImplPackage;

    @Getter
    @Setter
    private static String controllerPackage;

    // 最终包名 end

    // 最终路径 start

    @Getter
    @Setter
    private static String daoPath4mbg;

    @Getter
    @Setter
    private static String mapperPath4mbg;

    @Getter
    @Setter
    private static String modelPath4mbg;

    @Getter
    @Setter
    private static String inputPath;

    @Getter
    @Setter
    private static String servicePath;

    @Getter
    @Setter
    private static String serviceImplPath;

    @Getter
    @Setter
    private static String controllerPath;

    // 最终路径 end

    static {
        initDefaultProperties();
        loadPropertiesFile();
        figureValues();
    }

    private static void initDefaultProperties() {
        date = Times.toString(LocalDateTime.now());
        mapperPackage = "mapper";
    }

    @SneakyThrows
    private static void loadPropertiesFile() {
        // 读取文件
        props = new Properties();
        props.load(new InputStreamReader(ConfigUtils.class.getClassLoader().getResourceAsStream("project.properties"),
                StandardCharsets.UTF_8));
        String replaceTo = props.getProperty("replace-to");
        if (StringUtils.isNotBlank(replaceTo)) {
            props.load(FileUtils.openInputStream(new File(replaceTo)));
        }

        String authorProp = props.getProperty("author");
        if (StringUtils.isBlank(authorProp)) {
            log.error("author 未指定");
            System.exit(0);
        } else {
            author = authorProp;
        }

        // names
        String tableNames = props.getProperty("table-names");
        String modelCns = props.getProperty("model-cns");
        if (StringUtils.isAnyBlank(tableNames, modelCns)) {
            log.error("tableNames, modelCns 未完全指定");
            System.exit(0);
        }
        String[] a1 = tableNames.split("\\+");
        String[] a2 = modelCns.split("\\+");
        if (a1.length != a2.length) {
            log.error("table-names, model-cns 格式非法");
            System.exit(0);
        }
        ConfigUtils.tableNames = a1;
        ConfigUtils.modelCns = a2;

        // mysql
        String mysqlIpProp = props.getProperty("mysql-ip");
        String mysqlPortProp = props.getProperty("mysql-port");
        String mysqlUsernameProp = props.getProperty("mysql-username");
        String mysqlPasswordProp = props.getProperty("mysql-password");
        String mysqlDatabaseProp = props.getProperty("mysql-database");
        if (StringUtils
                .isAnyBlank(mysqlIpProp, mysqlPortProp, mysqlUsernameProp, mysqlPasswordProp, mysqlDatabaseProp)) {
            log.error("mysql-* 未完全指定");
            System.exit(0);
        }
        mysqlUrl = "jdbc:mysql://" + mysqlIpProp + ":" + mysqlPortProp + "/" + mysqlDatabaseProp + "?useSSL=false";
        mysqlUsername = mysqlUsernameProp;
        mysqlPassword = mysqlPasswordProp;
        mysqlDatabase = mysqlDatabaseProp;

        // over-write
        String overWriteProp = props.getProperty("over-write");
        if (StringUtils.isBlank(overWriteProp)) {
            log.info("over-write 未指定");
            overWrite = true;
        } else {
            overWrite = BooleanUtils.toBoolean(overWriteProp);
        }

        // project-path
        String projectPathProp = props.getProperty("project-path");
        if (!new File(projectPathProp).exists()) {
            log.error("project-path 目录不存在");
            System.exit(0);
        }
        projectPath = projectPathProp;

        // base-package
        String basePackageProp = props.getProperty("base-package");
        if (StringUtils.isBlank(basePackageProp) || !FileExistsUtils.referenceExist(projectPath, basePackageProp)) {
            log.error("base-package 目录不存在");
            System.exit(0);
        }
        basePackage = basePackageProp;

        // api
        String commonMapperProp = props.getProperty("common-mapper");
        if (StringUtils.isBlank(commonMapperProp) || !FileExistsUtils.referenceExist(projectPath, commonMapperProp)) {
            log.error("common-mapper 文件不存在");
            System.exit(0);
        } else {
            commonMapper = commonMapperProp;
        }
        String commonServiceProp = props.getProperty("common-service");
        if (StringUtils.isBlank(commonServiceProp) ||
                !FileExistsUtils.referenceExist(projectPath, commonServiceProp)) {
            log.error("common-service 文件不存在");
            System.exit(0);
        } else {
            commonService = commonServiceProp;
        }
        String commonServiceImplProp = props.getProperty("common-service-impl");
        if (StringUtils.isBlank(commonServiceImplProp) ||
                !FileExistsUtils.referenceExist(projectPath, commonServiceImplProp)) {
            log.error("common-service-impl 文件不存在");
            System.exit(0);
        }
        commonServiceImpl = commonServiceImplProp;
        String optionProp = props.getProperty("option");
        if (StringUtils.isBlank(optionProp) || !FileExistsUtils.referenceExist(projectPath, optionProp)) {
            log.error("option 文件不存在");
            System.exit(0);
        }
        option = optionProp;
        String pageProp = props.getProperty("page");
        if (StringUtils.isBlank(pageProp) || !FileExistsUtils.referenceExist(projectPath, pageProp)) {
            log.error("page 文件不存在");
            System.exit(0);
        }
        page = pageProp;
        String pageParamProp = props.getProperty("page-param");
        if (StringUtils.isBlank(pageParamProp) || !FileExistsUtils
                .referenceExist(projectPath, pageParamProp)) {
            log.error("page-param 文件不存在");
            System.exit(0);
        }
        pageParam = pageParamProp;
        String serviceExceptionProp = props.getProperty("service-exception");
        if (StringUtils.isBlank(serviceExceptionProp) ||
                !FileExistsUtils.referenceExist(projectPath, serviceExceptionProp)) {
            log.error("service-exception 文件不存在");
            System.exit(0);
        }
        serviceException = serviceExceptionProp;
    }

    private static void figureValues() {
        // tag
        classDocEnd = " *" + br + " * @author ${author} ${date}" + br + " */";
        classDocEnd = classDocEnd.replace("${author}", author).replace("${date}", date);

        // table comment
        tableComments = new String[tableNames.length];
        for (String tableName : tableNames) {
            ArrayUtils.add(tableComments, JdbcUtils.getTableCommtents(tableName));
        }

        log.info("项目路径 " + projectPath);

        // final package
        daoPackage = basePackage + ".dao";
        log.info("包名 " + daoPackage);
        modelPackage = basePackage + ".model";
        log.info("包名 " + modelPackage);
        inputPackage = basePackage + ".input";
        log.info("包名 " + inputPackage);
        servicePackage = basePackage + ".service";
        log.info("包名 " + servicePackage);
        serviceImplPackage = basePackage + ".service.impl";
        log.info("包名 " + serviceImplPackage);
        controllerPackage = basePackage + ".controller";
        log.info("包名 " + controllerPackage);

        // final path
        daoPath4mbg = projectPath + mavenJava;
        mapperPath4mbg = projectPath + mavenRes;
        modelPath4mbg = projectPath + mavenJava;
        inputPath = projectPath + mavenJava + inputPackage.replace('.', sep) + sep;
        servicePath = projectPath + mavenJava + servicePackage.replace('.', sep) + sep;
        serviceImplPath = projectPath + mavenJava + serviceImplPackage.replace('.', sep) + sep;
        controllerPath = projectPath + mavenJava + controllerPackage.replace('.', sep) + sep;
    }

}
