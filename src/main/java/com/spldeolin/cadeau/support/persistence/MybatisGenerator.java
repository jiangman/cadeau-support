package com.spldeolin.cadeau.support.persistence;

import static com.spldeolin.cadeau.support.util.ConstantUtil.br;
import static com.spldeolin.cadeau.support.util.ConstantUtil.mavenRes;
import static com.spldeolin.cadeau.support.util.ConstantUtil.sep;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import com.spldeolin.cadeau.support.util.ConfigUtil;
import com.spldeolin.cadeau.support.util.FileParseUtil;
import com.spldeolin.cadeau.support.util.StringCaseUtil;
import lombok.extern.log4j.Log4j2;

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
            log.error("checked", e);
            throw new RuntimeException();
        }
        CONTEXT = CONFIGURATION.getContext("Mysql");
    }

    /**
     * 生成持久层与input类属性片段
     */
    public static void daoMapperModel() {
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
        for (String tableName : ConfigUtil.getTableNames()) {
            // 重写table标签
            configTable(tableName);
            // 生成
            generte();
        }
        // 为Mapper接口添加@Mapper注解
        addMapperAnnotation();
    }

    private static void configJdbc() {
        JDBCConnectionConfiguration mysqlConfig = CONTEXT.getJdbcConnectionConfiguration();
        mysqlConfig.setConnectionURL(ConfigUtil.getMysqlUrl());
        mysqlConfig.setUserId(ConfigUtil.getMysqlUsername());
        mysqlConfig.setPassword(ConfigUtil.getMysqlPassword());
    }

    private static void configCommonMapper() {
        PluginConfiguration mapperPlugin = new PluginConfiguration();
        mapperPlugin.setConfigurationType("tk.mybatis.mapper.generator.MapperPlugin");
        mapperPlugin.addProperty("mappers", ConfigUtil.getDerivedMapper());
        mapperPlugin.addProperty("caseSensitive", "true");
        mapperPlugin.addProperty("useMapperCommentGenerator", "false");
        CONTEXT.addPluginConfiguration(mapperPlugin);
    }

    private static void addInputGeneratePlugin() {
        // 清除临时文件
        try {
            FileUtils.deleteDirectory(new File(ConfigUtil.getProjectPath() + mavenRes + "ftl"));
        } catch (IOException e) {
            log.error("checked", e);
            throw new RuntimeException();
        }
        PluginConfiguration plugin = new PluginConfiguration();
        plugin.setConfigurationType("com.spldeolin.cadeau.support.input.InputFieldGeneratePlugin");
        CONTEXT.addPluginConfiguration(plugin);
    }

    private static void configModel() {
        JavaModelGeneratorConfiguration config = CONTEXT.getJavaModelGeneratorConfiguration();
        config.setTargetProject(ConfigUtil.getModelPath4mbg());
        config.setTargetPackage(ConfigUtil.getModelPackage());
    }

    private static void configMapper() {
        SqlMapGeneratorConfiguration config = CONTEXT.getSqlMapGeneratorConfiguration();
        config.setTargetProject(ConfigUtil.getMapperPath4mbg());
        config.setTargetPackage(ConfigUtil.getMapperPackage());
    }

    private static void configDao() {
        JavaClientGeneratorConfiguration config = CONTEXT.getJavaClientGeneratorConfiguration();
        config.setTargetProject(ConfigUtil.getDaoPath4mbg());
        config.setTargetPackage(ConfigUtil.getDaoPackage());
    }

    private static void configTable(String tableName) {
        CONTEXT.getTableConfigurations().get(0).setTableName(tableName);
    }

    private static void generte() {
        DefaultShellCallback callback = new DefaultShellCallback(ConfigUtil.getOverWrite());
        MyBatisGenerator myBatisGenerator;
        try {
            myBatisGenerator = new MyBatisGenerator(CONFIGURATION, callback, MBG_WARNINGS);
            myBatisGenerator.generate(null);
        } catch (SQLException | IOException | InterruptedException | InvalidConfigurationException e) {
            log.error("checked", e);
            throw new RuntimeException();
        }
    }

    private static void addMapperAnnotation() {
        Iterator<File> files = FileUtils.iterateFiles(
                new File(ConfigUtil.getDaoPath4mbg() + ConfigUtil.getDaoPackage().replace('.', sep)),
                new String[] {"java"}, true);
        while (files.hasNext()) {
            File file = files.next();
            String content;
            try {
                content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("checked", e);
                throw new RuntimeException();
            }
            if (content.contains("org.apache.ibatis.annotations.Mapper")) {
                continue;
            }
            String header = "public interface";
            String doc = "/**" + br + " * “" + getModelCnsByFile(file) + "”数据库映射" + br + ConfigUtil.getClassDocEnd();
            content = content.replace(header,
                    "import org.apache.ibatis.annotations.Mapper;" + br + doc + br + "@Mapper" + br + header);
            try {
                FileUtils.write(file, content, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("checked", e);
                throw new RuntimeException();
            }
        }
    }

    private static String getModelCnsByFile(File file) {
        String fileName = FileParseUtil.fileName(file);
        int index = ArrayUtils.indexOf(ConfigUtil.getTableNames(),
                StringCaseUtil.camelToSnake(fileName.replace("Mapper", "")));
        return ConfigUtil.getModelCns()[index];
    }

}