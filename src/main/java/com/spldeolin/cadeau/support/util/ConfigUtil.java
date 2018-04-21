package com.spldeolin.cadeau.support.util;

import static com.spldeolin.cadeau.support.util.ConstantUtil.br;
import static com.spldeolin.cadeau.support.util.ConstantUtil.generatorTag;
import static com.spldeolin.cadeau.support.util.ConstantUtil.mavenJava;
import static com.spldeolin.cadeau.support.util.ConstantUtil.mavenRes;
import static com.spldeolin.cadeau.support.util.ConstantUtil.sep;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class ConfigUtil {

    private static Properties props = new Properties();

    /**
     * 作者
     */
    private static String author = "Deolin";

    /**
     * 生成日期
     */
    private static String date;

    /**
     * 业务模块
     */
    private static String bussiness = "";

    /**
     * 表名数组
     */
    private static String[] tableNames;

    /**
     * 模型中文名数组
     */
    private static String[] modelCns;

    /**
     * 数据库URL
     */
    private static String mysqlUrl = "jdbc:mysql://localhost:3306/information_schema?useSSL=false";

    /**
     * 数据库名
     */
    private static String mysqlDatabase = "information_schema";

    /**
     * 数据库用户名
     */
    private static String mysqlUsername = "root";

    /**
     * 数据库密码
     */
    private static String mysqlPassword = "root";

    /**
     * 文件重名时是否覆盖
     */
    private static Boolean overWrite = false;

    /**
     * 项目路径
     */
    private static String projectPath = System.getProperty("user.dir") + sep;

    /**
     * 基础包名
     */
    private static String basePackage = "com.spldeolin.cadeau.demo";

    /**
     * mapper.xml文件夹的Reference
     */
    private static String mapperFolder = "mapper";

    /**
     * 通用Service接口的Reference
     */
    private static String derivedService = "";

    /**
     * 通用ServiceImpl抽象类的Reference
     */
    private static String derivedServiceImpl = "";

    /**
     * 通用Mapper接口的Reference
     */
    private static String derivedMapper = "tk.mybatis.mapper.inherited.Mapper";

    /**
     * 控制层拓展注解的包名
     */
    private static String controllerExtraAnnotationPackage = "";

    /**
     * RequestResult的Reference
     */
    private static String requestResult = "";

    /**
     * ValidableList的Reference
     */
    private static String validableList = "";

    /**
     * TextOption的Reference
     */
    private static String textOption = "";

    /**
     * 表注释数组
     */
    private static String[] tableComments;

    /**
     * 最终类级注释tag
     */
    private static String classDocEnd = " *" + br + " * @author ${author} ${date}" + br + generatorTag + br + " */";

    // 最终包名 start

    private static String daoPackage = "${basePackage}.dao${bussinessPart}";

    private static String mapperPackage = "mapper${bussinessPart}";

    private static String modelPackage = "${basePackage}.model${bussinessPart}";

    private static String inputPackage = "${basePackage}.input${bussinessPart}";

    private static String servicePackage = "${basePackage}.service${bussinessPart}";

    private static String serviceImplPackage = "${basePackage}.service.impl${bussinessPart}";

    private static String controllerPackage = "${basePackage}.controller${bussinessPart}";

    // 最终包名 end

    // 最终路径 start

    private static String daoPath4mbg;

    private static String mapperPath4mbg;

    private static String modelPath4mbg;

    private static String inputPath;

    private static String servicePath;

    private static String serviceImplPath;

    private static String controllerPath;

    // 最终路径 end

    static {
        readProps();
        initValues();
        figureValues();
    }

    public static void assign() {}

    @SneakyThrows
    private static void readProps() {
        log.info("读取配置文件");
        props.load(new InputStreamReader(ConfigUtil.class.getClassLoader().getResourceAsStream("生成配置.properties"),
                StandardCharsets.UTF_8));
        log.info("读取配置文件完成");
    }

    private static void initValues() {
        log.info("初始化配置");
        // author date
        String author = props.getProperty("author");
        if (StringUtils.isBlank(author)) {
            log.info("\t“作者”未指定，使用缺省配置");
        } else {
            ConfigUtil.author = author;
        }
        String date = props.getProperty("date");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
        if (StringUtils.isBlank(date)) {
            log.info("\t“生成日期”未指定，使用缺省配置");
            ConfigUtil.date = sdf.format(new Date());
        } else {
            try {
                sdf.parse(date);
                ConfigUtil.date = date;
            } catch (ParseException e) {
                log.info("\t“生成日期”无法解析，使用缺省配置");
                ConfigUtil.date = sdf.format(new Date());
            }
        }
        // bussiness
        String bussiness = props.getProperty("bussiness");
        if (StringUtils.isBlank(bussiness)) {
            log.info("\t“业务模块”未指定，使用缺省配置");
        } else {
            ConfigUtil.bussiness = bussiness;
        }
        // names
        String tableNames = props.getProperty("tableNames");
        String modelCns = props.getProperty("modelCns");
        if (StringUtils.isAnyBlank(tableNames, modelCns)) {
            log.error("表名或模型名未完全指定。");
            throw new RuntimeException();
        }
        String[] a1 = tableNames.split("\\+");
        String[] a2 = modelCns.split("\\+");
        if (a1.length != a2.length) {
            log.error("表名与模型名个数不完全一致。");
            throw new RuntimeException();
        }
        ConfigUtil.tableNames = a1;
        ConfigUtil.modelCns = a2;
        // mysql
        String mysqlIp = props.getProperty("mysql-ip");
        String mysqlPort = props.getProperty("mysql-port");
        String mysqlUsername = props.getProperty("mysql-username");
        String mysqlPassword = props.getProperty("mysql-password");
        String mysqlDatabase = props.getProperty("mysql-database");
        if (StringUtils.isAnyBlank(mysqlIp, mysqlPort, mysqlUsername, mysqlPassword)) {
            log.info("\t“数据库IP”、“端口”、“用户名”、“密码”未完全指定，使用缺省配置。");
        } else {
            mysqlUrl = mysqlUrl.replace("localhost", mysqlIp).replace("3306", mysqlPort);
            ConfigUtil.mysqlUsername = mysqlUsername;
            ConfigUtil.mysqlPassword = mysqlPassword;
        }
        if (StringUtils.isBlank(mysqlDatabase)) {
            log.info("\t“数据库名”未指定，使用缺省配置。");
        } else {
            mysqlUrl = mysqlUrl.replace("information_schema", mysqlDatabase);
            ConfigUtil.mysqlDatabase = mysqlDatabase;
        }
        // over-write
        String overWrite = props.getProperty("over-write");
        if (StringUtils.isBlank(overWrite)) {
            log.info("\t“文件重名时是否覆盖”未指定，使用缺省配置");
        } else {
            if (!"true".equalsIgnoreCase(overWrite)) {
                log.info("\t“文件重名时是否覆盖”不是true，使用缺省配置");
            } else {
                ConfigUtil.overWrite = true;
            }
        }
        // path package
        String projectPath = props.getProperty("project-path");
        if (!new File(projectPath).exists()) {
            log.info("\t“项目路径”未指定或是路径不存在，使用缺省配置");
        } else {
            ConfigUtil.projectPath = projectPath;
        }
        String basePackage = props.getProperty("base-package");
        if (StringUtils.isBlank(basePackage) || !FileExistsUtil.referenceExist(ConfigUtil.projectPath, basePackage)) {
            log.info("\t“基础包名”未指定或是路径不存在，使用缺省配置");
        } else {
            ConfigUtil.basePackage = basePackage;
        }
        String mapperFolder = props.getProperty("mapper-folder");
        if (StringUtils.isBlank(mapperFolder) || !FileExistsUtil.resourceExist(ConfigUtil.projectPath, mapperFolder)) {
            log.info("\t“mapper.xml文件夹”未指定或是路径不存在，使用缺省配置");
        } else {
            ConfigUtil.mapperFolder = mapperFolder;
        }
        // component
        String derivedService = props.getProperty("derived-service");
        if (StringUtils.isBlank(derivedService) ||
                !FileExistsUtil.referenceExist(ConfigUtil.projectPath, derivedService)) {
            log.info("\t“通用Service接口”未指定或是路径不存在，缺省依赖Cadeau Library");
            ConfigUtil.derivedService = "com.spldeolin.cadeau.library.inherited.CommonService";
        } else {
            ConfigUtil.derivedService = derivedService;
        }
        String derivedServiceImpl = props.getProperty("derived-service-impl");
        if (StringUtils.isBlank(derivedServiceImpl) ||
                !FileExistsUtil.referenceExist(ConfigUtil.projectPath, derivedServiceImpl)) {
            log.info("\t“通用ServiceImpl抽象类”未指定或是路径不存在，缺省依赖Cadeau Library");
            ConfigUtil.derivedServiceImpl = "com.spldeolin.cadeau.library.inherited.CommonServiceImpl";
        } else {
            ConfigUtil.derivedServiceImpl = derivedServiceImpl;
        }
        String derivedMapper = props.getProperty("derived-mapper");
        if (StringUtils.isBlank(derivedMapper) || !FileExistsUtil.referenceExist(ConfigUtil.projectPath,
                derivedMapper)) {
            log.info("\t“通用Mapper接口”未指定或是路径不存在，缺省依赖Cadeau Library");
            ConfigUtil.derivedMapper = "com.spldeolin.cadeau.library.inherited.CommonMapper";
        } else {
            ConfigUtil.derivedMapper = derivedMapper;
        }
        String controllerExtraAnnotationPackage = props.getProperty("controller-extra-annotation-package");
        if (StringUtils.isBlank(controllerExtraAnnotationPackage) ||
                !FileExistsUtil.referenceExist(ConfigUtil.projectPath, controllerExtraAnnotationPackage)) {
            log.info("\t“控制层拓展注解包”未指定或是路径不存在，缺省依赖Cadeau Library");
            ConfigUtil.controllerExtraAnnotationPackage = "com.spldeolin.cadeau.library.annotation";
        } else {
            ConfigUtil.controllerExtraAnnotationPackage = controllerExtraAnnotationPackage;
        }
        String requestResult = props.getProperty("request-result");
        if (StringUtils.isBlank(requestResult) ||
                !FileExistsUtil.referenceExist(ConfigUtil.projectPath, requestResult)) {
            log.info("\t“RequestResult类”未指定或是路径不存在，缺省依赖Cadeau Library");
            ConfigUtil.requestResult = "com.spldeolin.cadeau.library.dto.RequestResult";
        } else {
            ConfigUtil.requestResult = requestResult;
        }
        String validableList = props.getProperty("validable-list");
        if (StringUtils.isBlank(validableList) ||
                !FileExistsUtil.referenceExist(ConfigUtil.projectPath, validableList)) {
            log.info("\t“ValidableList类”未指定或是路径不存在，缺省依赖Cadeau Library");
            ConfigUtil.validableList = "com.spldeolin.cadeau.library.valid.ValidableList";
        } else {
            ConfigUtil.validableList = validableList;
        }
        String textOption = props.getProperty("text-option");
        if (StringUtils.isBlank(textOption) || !FileExistsUtil.referenceExist(ConfigUtil.projectPath, textOption)) {
            log.info("\t“TextOption类”未指定或是路径不存在，缺省依赖Cadeau Library");
            ConfigUtil.textOption = "com.spldeolin.cadeau.library.valid.annotation.TextOption";
        } else {
            ConfigUtil.textOption = textOption;
        }
        log.info("初始化配置完成");
    }

    private static void figureValues() {
        log.info("计算最终包名、最终路径");
        // tag
        classDocEnd = classDocEnd.replace("${author}", author).replace("${date}", date);
        // table comment
        tableComments = new String[tableNames.length];
        for (String tableName : tableNames) {
            ArrayUtils.add(tableComments, JdbcUtil.getTableCommtents(tableName));
        }
        // final package
        String bussinessPart = bussiness;
        if (StringUtils.isNotBlank(bussinessPart)) {
            bussinessPart = "." + bussinessPart;
        }
        daoPackage = daoPackage.replace("${basePackage}", basePackage).replace("${bussinessPart}", bussinessPart);
        mapperPackage = mapperPackage.replace("${bussinessPart}", bussinessPart);
        modelPackage = modelPackage.replace("${basePackage}", basePackage).replace("${bussinessPart}", bussinessPart);
        inputPackage = inputPackage.replace("${basePackage}", basePackage).replace("${bussinessPart}", bussinessPart);
        servicePackage = servicePackage.replace("${basePackage}", basePackage).replace("${bussinessPart}",
                bussinessPart);
        serviceImplPackage = serviceImplPackage.replace("${basePackage}", basePackage).replace("${bussinessPart}",
                bussinessPart);
        controllerPackage = controllerPackage.replace("${basePackage}", basePackage).replace("${bussinessPart}",
                bussinessPart);
        // final path
        daoPath4mbg = projectPath + mavenJava;
        mapperPath4mbg = projectPath + mavenRes;
        modelPath4mbg = projectPath + mavenJava;
        inputPath = projectPath + mavenJava + inputPackage.replace('.', sep) + sep;
        servicePath = projectPath + mavenJava + servicePackage.replace('.', sep) + sep;
        serviceImplPath = projectPath + mavenJava + serviceImplPackage.replace('.', sep) + sep;
        controllerPath = projectPath + mavenJava + controllerPackage.replace('.', sep) + sep;
        log.info("计算最终包名、最终路径完成");
    }

    public static String getAuthor() {
        return author;
    }

    public static void setAuthor(String author) {
        ConfigUtil.author = author;
    }

    public static String getDate() {
        return date;
    }

    public static void setDate(String date) {
        ConfigUtil.date = date;
    }

    public static String getBussiness() {
        return bussiness;
    }

    public static void setBussiness(String bussiness) {
        ConfigUtil.bussiness = bussiness;
    }

    public static String[] getTableNames() {
        return tableNames;
    }

    public static void setTableNames(String[] tableNames) {
        ConfigUtil.tableNames = tableNames;
    }

    public static String[] getModelCns() {
        return modelCns;
    }

    public static void setModelCns(String[] modelCns) {
        ConfigUtil.modelCns = modelCns;
    }

    public static String getMysqlUrl() {
        return mysqlUrl;
    }

    public static void setMysqlUrl(String mysqlUrl) {
        ConfigUtil.mysqlUrl = mysqlUrl;
    }

    public static String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public static void setMysqlDatabase(String mysqlDatabase) {
        ConfigUtil.mysqlDatabase = mysqlDatabase;
    }

    public static String getMysqlUsername() {
        return mysqlUsername;
    }

    public static void setMysqlUsername(String mysqlUsername) {
        ConfigUtil.mysqlUsername = mysqlUsername;
    }

    public static String getMysqlPassword() {
        return mysqlPassword;
    }

    public static void setMysqlPassword(String mysqlPassword) {
        ConfigUtil.mysqlPassword = mysqlPassword;
    }

    public static Boolean getOverWrite() {
        return overWrite;
    }

    public static void setOverWrite(Boolean overWrite) {
        ConfigUtil.overWrite = overWrite;
    }

    public static String getProjectPath() {
        return projectPath;
    }

    public static void setProjectPath(String projectPath) {
        ConfigUtil.projectPath = projectPath;
    }

    public static String getBasePackage() {
        return basePackage;
    }

    public static void setBasePackage(String basePackage) {
        ConfigUtil.basePackage = basePackage;
    }

    public static String getMapperFolder() {
        return mapperFolder;
    }

    public static void setMapperFolder(String mapperFolder) {
        ConfigUtil.mapperFolder = mapperFolder;
    }

    public static String getDerivedService() {
        return derivedService;
    }

    public static void setDerivedService(String derivedService) {
        ConfigUtil.derivedService = derivedService;
    }

    public static String getDerivedServiceImpl() {
        return derivedServiceImpl;
    }

    public static void setDerivedServiceImpl(String derivedServiceImpl) {
        ConfigUtil.derivedServiceImpl = derivedServiceImpl;
    }

    public static String getDerivedMapper() {
        return derivedMapper;
    }

    public static void setDerivedMapper(String derivedMapper) {
        ConfigUtil.derivedMapper = derivedMapper;
    }

    public static String getControllerExtraAnnotationPackage() {
        return controllerExtraAnnotationPackage;
    }

    public static void setControllerExtraAnnotationPackage(String controllerExtraAnnotationPackage) {
        ConfigUtil.controllerExtraAnnotationPackage = controllerExtraAnnotationPackage;
    }

    public static String getRequestResult() {
        return requestResult;
    }

    public static void setRequestResult(String requestResult) {
        ConfigUtil.requestResult = requestResult;
    }

    public static String getValidableList() {
        return validableList;
    }

    public static void setValidableList(String validableList) {
        ConfigUtil.validableList = validableList;
    }

    public static String getTextOption() {
        return textOption;
    }

    public static void setTextOption(String textOption) {
        ConfigUtil.textOption = textOption;
    }

    public static String[] getTableComments() {
        return tableComments;
    }

    public static void setTableComments(String[] tableComments) {
        ConfigUtil.tableComments = tableComments;
    }

    public static String getClassDocEnd() {
        return classDocEnd;
    }

    public static void setClassDocEnd(String classDocEnd) {
        ConfigUtil.classDocEnd = classDocEnd;
    }

    public static String getDaoPackage() {
        return daoPackage;
    }

    public static void setDaoPackage(String daoPackage) {
        ConfigUtil.daoPackage = daoPackage;
    }

    public static String getMapperPackage() {
        return mapperPackage;
    }

    public static void setMapperPackage(String mapperPackage) {
        ConfigUtil.mapperPackage = mapperPackage;
    }

    public static String getModelPackage() {
        return modelPackage;
    }

    public static void setModelPackage(String modelPackage) {
        ConfigUtil.modelPackage = modelPackage;
    }

    public static String getInputPackage() {
        return inputPackage;
    }

    public static void setInputPackage(String inputPackage) {
        ConfigUtil.inputPackage = inputPackage;
    }

    public static String getServicePackage() {
        return servicePackage;
    }

    public static void setServicePackage(String servicePackage) {
        ConfigUtil.servicePackage = servicePackage;
    }

    public static String getServiceImplPackage() {
        return serviceImplPackage;
    }

    public static void setServiceImplPackage(String serviceImplPackage) {
        ConfigUtil.serviceImplPackage = serviceImplPackage;
    }

    public static String getControllerPackage() {
        return controllerPackage;
    }

    public static void setControllerPackage(String controllerPackage) {
        ConfigUtil.controllerPackage = controllerPackage;
    }

    public static String getDaoPath4mbg() {
        return daoPath4mbg;
    }

    public static void setDaoPath4mbg(String daoPath4mbg) {
        ConfigUtil.daoPath4mbg = daoPath4mbg;
    }

    public static String getMapperPath4mbg() {
        return mapperPath4mbg;
    }

    public static void setMapperPath4mbg(String mapperPath4mbg) {
        ConfigUtil.mapperPath4mbg = mapperPath4mbg;
    }

    public static String getModelPath4mbg() {
        return modelPath4mbg;
    }

    public static void setModelPath4mbg(String modelPath4mbg) {
        ConfigUtil.modelPath4mbg = modelPath4mbg;
    }

    public static String getInputPath() {
        return inputPath;
    }

    public static void setInputPath(String inputPath) {
        ConfigUtil.inputPath = inputPath;
    }

    public static String getServicePath() {
        return servicePath;
    }

    public static void setServicePath(String servicePath) {
        ConfigUtil.servicePath = servicePath;
    }

    public static String getServiceImplPath() {
        return serviceImplPath;
    }

    public static void setServiceImplPath(String serviceImplPath) {
        ConfigUtil.serviceImplPath = serviceImplPath;
    }

    public static String getControllerPath() {
        return controllerPath;
    }

    public static void setControllerPath(String controllerPath) {
        ConfigUtil.controllerPath = controllerPath;
    }

}
