package com.spldeolin.cadeau.support.util;

import static com.spldeolin.cadeau.support.util.ConstantUtil.SDF;
import static com.spldeolin.cadeau.support.util.ConstantUtil.br;
import static com.spldeolin.cadeau.support.util.ConstantUtil.mavenJava;
import static com.spldeolin.cadeau.support.util.ConstantUtil.mavenRes;
import static com.spldeolin.cadeau.support.util.ConstantUtil.sep;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class ConfigUtil {

    @Getter
    @Setter
    private static Properties props = new Properties();

    /**
     * 作者
     */
    @Getter
    @Setter
    private static String author = "Deolin";

    /**
     * 生成日期
     */
    @Getter
    @Setter
    private static String date;

    /**
     * 业务模块
     */
    @Getter
    @Setter
    private static String bussiness = "";

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
    private static String mysqlUrl = "jdbc:mysql://localhost:3306/information_schema?useSSL=false";

    /**
     * 数据库名
     */
    @Getter
    @Setter
    private static String mysqlDatabase = "information_schema";

    /**
     * 数据库用户名
     */
    @Getter
    @Setter
    private static String mysqlUsername = "root";

    /**
     * 数据库密码
     */
    @Getter
    @Setter
    private static String mysqlPassword = "root";

    /**
     * 文件重名时是否覆盖
     */
    @Getter
    @Setter
    private static Boolean overWrite = false;

    /**
     * 项目路径
     */
    @Getter
    @Setter
    private static String projectPath = System.getProperty("user.dir") + sep;

    /**
     * 基础包名
     */
    @Getter
    @Setter
    private static String basePackage = "com.spldeolin.cadeau.demo";

    /**
     * mapper.xml文件夹的Reference
     */
    @Getter
    @Setter
    private static String mapperFolder = "mapper";

    /**
     * 通用Service接口的Reference
     */
    @Getter
    @Setter
    private static String derivedService = "";

    /**
     * 通用ServiceImpl抽象类的Reference
     */
    @Getter
    @Setter
    private static String derivedServiceImpl = "";

    /**
     * 通用Mapper接口的Reference
     */
    @Getter
    @Setter
    private static String derivedMapper = "tk.mybatis.mapper.inherited.Mapper";

    /**
     * TextOption的Reference
     */
    @Getter
    @Setter
    private static String textOption = "";

    /**
     * Page的Reference
     */
    @Getter
    @Setter
    private static String page = "";

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
    private static String classDocEnd = " *" + br + " * @author ${author} ${date}" + br + " */";

    // 最终包名 start

    @Getter
    @Setter
    private static String daoPackage = "${basePackage}.dao${bussinessPart}";

    @Getter
    @Setter
    private static String mapperPackage = "mapper${bussinessPart}";

    @Getter
    @Setter
    private static String modelPackage = "${basePackage}.model${bussinessPart}";

    @Getter
    @Setter
    private static String inputPackage = "${basePackage}.input${bussinessPart}";

    @Getter
    @Setter
    private static String servicePackage = "${basePackage}.service${bussinessPart}";

    @Getter
    @Setter
    private static String serviceImplPackage = "${basePackage}.service.impl${bussinessPart}";

    @Getter
    @Setter
    private static String controllerPackage = "${basePackage}.controller${bussinessPart}";

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
        if (StringUtils.isBlank(date)) {
            log.info("\t“生成日期”未指定，使用缺省配置");
            ConfigUtil.date = SDF.format(new Date());
        } else {
            try {
                SDF.parse(date);
                ConfigUtil.date = date;
            } catch (ParseException e) {
                log.info("\t“生成日期”无法解析，使用缺省配置");
                ConfigUtil.date = SDF.format(new Date());
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
        if (StringUtils.isBlank(mapperFolder) || !FileExistsUtil.resourceExist(ConfigUtil.projectPath, mapperFolder)) {
            log.info("\t“mapper.xml文件夹”未指定或是路径不存在，使用缺省配置");
            ConfigUtil.mapperFolder = projectPath + mavenRes + mapperFolder.replace('.', sep) + "mapper";
        } else {
            ConfigUtil.mapperFolder = mapperFolder;
        }
        // component
        String derivedService = props.getProperty("derived-service");
        if (StringUtils.isBlank(derivedService) ||
                !FileExistsUtil.referenceExist(ConfigUtil.projectPath, derivedService)) {
            log.info("\t“通用Service接口”未指定或是路径不存在，请指定");
            ConfigUtil.derivedService = "com.spldeolin.cadeau.library.inherited.CommonService";
        } else {
            ConfigUtil.derivedService = derivedService;
        }
        String derivedServiceImpl = props.getProperty("derived-service-impl");
        if (StringUtils.isBlank(derivedServiceImpl) ||
                !FileExistsUtil.referenceExist(ConfigUtil.projectPath, derivedServiceImpl)) {
            log.info("\t“通用ServiceImpl抽象类”未指定或是路径不存在，请指定");
            ConfigUtil.derivedServiceImpl = "com.spldeolin.cadeau.library.inherited.CommonServiceImpl";
        } else {
            ConfigUtil.derivedServiceImpl = derivedServiceImpl;
        }
        String derivedMapper = props.getProperty("derived-mapper");
        if (StringUtils.isBlank(derivedMapper) || !FileExistsUtil.referenceExist(ConfigUtil.projectPath,
                derivedMapper)) {
            log.info("\t“通用Mapper接口”未指定或是路径不存在，请指定");
            ConfigUtil.derivedMapper = "com.spldeolin.cadeau.library.inherited.CommonMapper";
        } else {
            ConfigUtil.derivedMapper = derivedMapper;
        }
        String textOption = props.getProperty("text-option");
        if (StringUtils.isBlank(textOption) || !FileExistsUtil.referenceExist(ConfigUtil.projectPath, textOption)) {
            log.info("\t“TextOption类”未指定或是路径不存在，请指定");
            ConfigUtil.textOption = "com.spldeolin.cadeau.library.valid.annotation.TextOption";
        } else {
            ConfigUtil.textOption = textOption;
        }
        String page = props.getProperty("page");
        if (StringUtils.isBlank(page) || !FileExistsUtil.referenceExist(ConfigUtil.projectPath, page)) {
            log.info("\t“Page类”未指定或是路径不存在，请指定");
            return;
        } else {
            ConfigUtil.page = page;
        }
        String serviceException = props.getProperty("service-exception");
        if (StringUtils.isBlank(serviceException) ||
                !FileExistsUtil.referenceExist(ConfigUtil.projectPath, serviceException)) {
            log.info("\t“ServiceException类”未指定或是路径不存在，请指定");
        } else {
            ConfigUtil.serviceException = serviceException;
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

}
