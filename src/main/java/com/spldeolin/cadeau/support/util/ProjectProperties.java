package com.spldeolin.cadeau.support.util;

import static com.spldeolin.cadeau.support.util.ConstantUtils.br;
import static com.spldeolin.cadeau.support.util.ConstantUtils.mavenJava;
import static com.spldeolin.cadeau.support.util.ConstantUtils.mavenRes;
import static com.spldeolin.cadeau.support.util.ConstantUtils.sep;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class ProjectProperties {

    private static final ProjectProperties instance = new ProjectProperties();

    static {
        instance.initDefaultProperties();
        instance.loadPropertiesFile();
        instance.figureValues();
    }

    private ProjectProperties() {
    }

    public static ProjectProperties instance() {
        return instance;
    }

    private Properties props;

    /**
     * 作者
     */
    private String author;

    /**
     * 生成日期
     */
    private String date;

    /**
     * 表名数组
     */
    private String[] tableNames;

    /**
     * 模型中文名数组
     */
    private String[] modelCns;

    /**
     * 是否生成控制层和Input类
     */
    private Boolean generateControllerAndInput;

    /**
     * 数据库URL
     */
    private String mysqlUrl;

    /**
     * 数据库名
     */
    private String mysqlDatabase;

    /**
     * 数据库用户名
     */
    private String mysqlUsername;

    /**
     * 数据库密码
     */
    private String mysqlPassword;

    /**
     * 文件重名时是否覆盖
     */
    private Boolean overWrite;

    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 基础包名
     */
    private String basePackage;

    /**
     * mapper.xml文件夹的Reference
     */
    private String mapperFolder;

    /**
     * 通用Service接口的Reference
     */
    private String commonService;

    /**
     * 通用ServiceImpl抽象类的Reference
     */
    private String commonServiceImpl;

    /**
     * 通用Mapper接口的Reference
     */
    private String commonMapper;

    /**
     * TextOption的Reference
     */
    private String option;

    /**
     * Page的Reference
     */
    private String page;

    /**
     * PageParam的Reference
     */
    private String pageParam;

    /**
     * ServiceException的Reference
     */
    private String serviceException;

    /**
     * ControllerAspectPreprocess的Reference
     */
    private String controllerAspectPreprocess;

    /**
     * 表注释数组
     */
    private String[] tableComments;

    /**
     * 最终类级注释tag
     */
    private String classDocEnd;

    // 最终包名 start

    private String daoPackage;

    private String mapperPackage;

    private String modelPackage;

    private String inputPackage;

    private String servicePackage;

    private String serviceImplPackage;

    private String controllerPackage;

    // 最终包名 end

    // 最终路径 start

    private String daoPath4mbg;

    private String mapperPath4mbg;

    private String modelPath4mbg;

    private String inputPath;

    private String servicePath;

    private String serviceImplPath;

    private String controllerPath;

    // 最终路径 end

    private void initDefaultProperties() {
        date = Times.toString(LocalDate.now(), "yyyy/M/d");
        mapperPackage = "mapper";
    }

    @SneakyThrows
    private void loadPropertiesFile() {
        // 读取文件
        props = new Properties();
        props.load(new InputStreamReader(
                FileUtils.openInputStream(new File(System.getProperty("user.dir") + mavenRes + "project.properties")),
                StandardCharsets.UTF_8));
        String replaceTo = props.getProperty("replace-to");
        if (StringUtils.isNotBlank(replaceTo)) {
            props.load(new InputStreamReader(FileUtils.openInputStream(new File(replaceTo)), StandardCharsets.UTF_8));
        }

        String authorProp = props.getProperty("author");
        if (StringUtils.isBlank(authorProp)) {
            log.error("author 未指定");
            System.exit(0);
        } else {
            author = authorProp;
        }

        // names
        String tableNamesProp = props.getProperty("table-names");
        String modelCnsProp = props.getProperty("model-cns");
        if (StringUtils.isAnyBlank(tableNamesProp, modelCnsProp)) {
            log.error("tableNames, modelCns 未完全指定");
            System.exit(0);
        }
        String[] a1 = tableNamesProp.split("\\+");
        String[] a2 = modelCnsProp.split("\\+");
        if (a1.length != a2.length) {
            log.error("table-names, model-cns 格式非法");
            System.exit(0);
        }
        tableNames = a1;
        modelCns = a2;

        String generateControllerAndInputProp = props.getProperty("generate-controller-and-input");
        if (StringUtils.isBlank(generateControllerAndInputProp)) {
            log.error("generate-controller-and-input 未指定");
            System.exit(0);
        } else {
            generateControllerAndInput = BooleanUtils.toBoolean(generateControllerAndInputProp);
        }

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
        String controllerAspectPreprocessProp = props.getProperty("controller-aspect-preprocess");
        if (StringUtils.isBlank(controllerAspectPreprocessProp) ||
                !FileExistsUtils.referenceExist(projectPath, controllerAspectPreprocessProp)) {
            log.error("controller-aspect-preprocess 文件不存在");
            System.exit(0);
        }
        controllerAspectPreprocess = controllerAspectPreprocessProp;

    }

    private void figureValues() {
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
