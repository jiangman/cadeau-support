package com.spldeolin.cadeau.support.persistence;

import static com.spldeolin.cadeau.support.util.ConstantUtils.br;
import static com.spldeolin.cadeau.support.util.ConstantUtils.mavenRes;
import static com.spldeolin.cadeau.support.util.ConstantUtils.sep;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import com.spldeolin.cadeau.support.util.ProjectProperties;
import com.spldeolin.cadeau.support.util.FileParseUtils;
import com.spldeolin.cadeau.support.util.JdbcUtils;
import com.spldeolin.cadeau.support.util.StringCaseUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class MybatisGenerator {

    private static final List<String> MBG_WARNINGS;

    private static final Configuration CONFIGURATION;

    private static final Context CONTEXT;

    static {
        MBG_WARNINGS = new ArrayList<>();
        try {
            CONFIGURATION = new ConfigurationParser(MBG_WARNINGS).parseConfiguration(
                    MybatisGenerator.class.getResourceAsStream("/mybatis-generator-config.xml"));
        } catch (IOException | XMLParserException e) {
            throw new RuntimeException();
        }
        CONTEXT = CONFIGURATION.getContext("Mysql");
    }

    /**
     * 生成持久层与input类属性片段
     */
    public static void daoMapperModel() {
        ProjectProperties properties = ProjectProperties.instance();
        // 重写jdbcConnection标签的属性
        configJdbc();
        // 重写通用Mapper接口
        configCommonMapper();
        // 追加用于生成Input类的plugin标签（判断）
        addInputGeneratePlugin();
        // 重写javaModelGenerator标签
        configModel();
        // 重写sqlMapGenerator标签
        configMapper();
        // 重写javaClientGenerator标签
        configDao();
        for (String tableName : properties.getTableNames()) {
            // 重写table标签
            configTable(tableName);
            // 生成
            generte();
        }
        // 为Mapper接口添加@Mapper注解
        addMapperAnnotation();
        // 为Mapper接口、Mapper.xml、Model追加Generated信息
        addGeneratedInfo();
    }

    private static void configJdbc() {
        ProjectProperties properties = ProjectProperties.instance();
        JDBCConnectionConfiguration mysqlConfig = CONTEXT.getJdbcConnectionConfiguration();
        mysqlConfig.setConnectionURL(properties.getMysqlUrl());
        mysqlConfig.setUserId(properties.getMysqlUsername());
        mysqlConfig.setPassword(properties.getMysqlPassword());
    }

    private static void configCommonMapper() {
        ProjectProperties properties = ProjectProperties.instance();
        PluginConfiguration mapperPlugin = new PluginConfiguration();
        mapperPlugin.setConfigurationType("tk.mybatis.mapper.generator.MapperPlugin");
        mapperPlugin.addProperty("mappers", properties.getCommonMapper());
        mapperPlugin.addProperty("caseSensitive", "true");
        mapperPlugin.addProperty("useMapperCommentGenerator", "false");
        CONTEXT.addPluginConfiguration(mapperPlugin);
    }

    @SneakyThrows
    private static void addInputGeneratePlugin() {
        ProjectProperties properties = ProjectProperties.instance();
        // 清除临时文件
        FileUtils.deleteDirectory(new File(properties.getProjectPath() + mavenRes + "ftl"));
        PluginConfiguration plugin = new PluginConfiguration();
        plugin.setConfigurationType("com.spldeolin.cadeau.support.input.InputFieldGeneratePlugin");
        CONTEXT.addPluginConfiguration(plugin);
    }

    private static void configModel() {
        ProjectProperties properties = ProjectProperties.instance();
        JavaModelGeneratorConfiguration config = CONTEXT.getJavaModelGeneratorConfiguration();
        config.setTargetProject(properties.getModelPath4mbg());
        config.setTargetPackage(properties.getModelPackage());
    }

    private static void configMapper() {
        ProjectProperties properties = ProjectProperties.instance();
        SqlMapGeneratorConfiguration config = CONTEXT.getSqlMapGeneratorConfiguration();
        config.setTargetProject(properties.getMapperPath4mbg());
        config.setTargetPackage(properties.getMapperPackage());
    }

    private static void configDao() {
        ProjectProperties properties = ProjectProperties.instance();
        JavaClientGeneratorConfiguration config = CONTEXT.getJavaClientGeneratorConfiguration();
        config.setTargetProject(properties.getDaoPath4mbg());
        config.setTargetPackage(properties.getDaoPackage());
    }

    private static void configTable(String tableName) {
        ProjectProperties properties = ProjectProperties.instance();
        // 表名
        TableConfiguration tableConfiguration = CONTEXT.getTableConfigurations().get(0);
        tableConfiguration.setTableName(tableName);
        // 添加columnOverride标签
        for (Map<String, String> column : JdbcUtils.listColumnType(properties.getMysqlDatabase(), tableName)) {
            String jdbcType = column.get("columnType");
            if ("datetime".equals(jdbcType)) {
                jdbcType = "timestamp";
            }
            if (StringUtils.equalsAny(jdbcType, "date", "time", "timestamp")) {
                ColumnOverride columnOverride = new ColumnOverride(column.get("columnName"));
                columnOverride.setJdbcType(StringUtils.upperCase(jdbcType));
                switch (jdbcType) {
                    case "date":
                        columnOverride.setJavaType("java.time.LocalDate");
                        break;
                    case "time":
                        columnOverride.setJavaType("java.time.LocalTime");
                        break;
                    case "timestamp":
                        columnOverride.setJavaType("java.time.LocalDateTime");
                        break;
                }
                tableConfiguration.addColumnOverride(columnOverride);
            }
        }
    }

    @SneakyThrows
    private static void generte() {
        ProjectProperties properties = ProjectProperties.instance();
        DefaultShellCallback callback = new DefaultShellCallback(properties.getOverWrite());
        MyBatisGenerator myBatisGenerator;
        myBatisGenerator = new MyBatisGenerator(CONFIGURATION, callback, MBG_WARNINGS);
        myBatisGenerator.generate(null);
    }

    @SneakyThrows
    private static void addMapperAnnotation() {
        ProjectProperties properties = ProjectProperties.instance();
        Iterator<File> files = FileUtils.iterateFiles(
                new File(properties.getDaoPath4mbg() + properties.getDaoPackage().replace('.', sep)),
                new String[] {"java"}, true);
        while (files.hasNext()) {
            File file = files.next();
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            if (content.contains("org.apache.ibatis.annotations.Mapper")) {
                continue;
            }
            String header = "public interface";
            String doc = "/**" + br + " * “" + getModelCnsByFile(file) + "”数据库映射" + br + properties.getClassDocEnd();
            content = content.replace(header,
                    "import org.apache.ibatis.annotations.Mapper;" + br + doc + br + "@Mapper" + br + header);
            FileUtils.write(file, content, StandardCharsets.UTF_8);
        }
    }

    @SneakyThrows
    private static void addGeneratedInfo() {
        ProjectProperties properties = ProjectProperties.instance();
        String generatedInfo = "/*" + br + " * Generated by Cadeau Support." + br + " *" +
                " * https://github.com/spldeolin/cadeau-support" + br + " */" + br;
        // DAO
        Iterator<File> daoFiles = FileUtils.iterateFiles(
                new File(properties.getDaoPath4mbg() + properties.getDaoPackage().replace('.', sep)),
                new String[] {"java"}, true);
        while (daoFiles.hasNext()) {
            File file = daoFiles.next();
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            if (content.startsWith(generatedInfo)) {
                continue;
            }
            content = generatedInfo + content;
            FileUtils.write(file, content, StandardCharsets.UTF_8);
        }

        // Mapper
        Iterator<File> mapperFiles = FileUtils.iterateFiles(
                new File(properties.getMapperPath4mbg() + properties.getMapperPackage().replace('.', sep)),
                new String[] {"xml"}, true);
        while (mapperFiles.hasNext()) {
            File file = mapperFiles.next();
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            if (content.contains("Cadeau Support")) {
                continue;
            }
            String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            content = content.replace(xmlHeader, xmlHeader + br + "<!--" + br + "  Generated by Cadeau Support at " +
                    properties.getDate() + "." + br + "  https://github.com/spldeolin/cadeau-support" + br + "-->");
            FileUtils.write(file, content, StandardCharsets.UTF_8);
        }

        // Model
        Iterator<File> modelFiles = FileUtils.iterateFiles(
                new File(properties.getDaoPath4mbg() + properties.getDaoPackage().replace('.', sep)),
                new String[] {"java"}, true);
        while (modelFiles.hasNext()) {
            File file = modelFiles.next();
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            if (content.startsWith(generatedInfo)) {
                continue;
            }
            content = generatedInfo + content;
            FileUtils.write(file, content, StandardCharsets.UTF_8);
            int i = 0;
        }
    }

    private static String getModelCnsByFile(File file) {
        ProjectProperties properties = ProjectProperties.instance();
        String fileName = FileParseUtils.fileName(file);
        int index = ArrayUtils.indexOf(properties.getTableNames(),
                StringCaseUtils.camelToSnake(fileName.replace("Mapper", "")));
        return properties.getModelCns()[index];
    }

}