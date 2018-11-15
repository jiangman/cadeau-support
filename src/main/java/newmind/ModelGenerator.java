package newmind;

import static com.spldeolin.cadeau.support.util.ConstantUtils.sep;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import com.spldeolin.cadeau.support.util.StringCaseUtils;
import com.spldeolin.cadeau.support.util.Times;
import com.zaxxer.hikari.HikariDataSource;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import lombok.extern.log4j.Log4j2;
import newmind.dto.ColumnDTO;
import newmind.dto.MapperJavaFtl;
import newmind.dto.MapperXmlFtl;
import newmind.dto.ModelFtl;
import newmind.dto.TableColumnDTO;

/**
 * @author Deolin 2018/11/14
 */
@Log4j2
public class ModelGenerator {

    private static final String AUTHOR = "Deolin" + " " + Times.toString(LocalDate.now(), "yyyy/MM/dd");

    private static final String BASE_PACKAGE_REFERENCE = "com.splendid.newmind.core";

    private static final String PROJECT_PATH = "C:\\java-development\\projects-repo\\deolin-projects\\new-mind";

    private static final String VERSION_COLUMN_NAME = "updated_at";

    private static final String JDBC_IP = "192.168.2.2";

    private static final Integer JDBC_PORT = 3306;

    private static final String JDBC_DATABASE = "new_mind";

    private static final String JDBC_USERNAME = "admin";

    private static final String JDBC_PASSWORD = "admin";

    public static void generator(String tableName) {
        // DataSource
        DataSource dataSource = createDataSource();

        // 表信息
        StringBuilder tableInfoSql = appendTableInfoSql(JDBC_DATABASE, tableName);
        List<Map<String, Object>> tableInfos = selectAsMapList(dataSource, tableInfoSql.toString());
        Map<String, Object> tableInfo = getFirstTableInfo(tableInfos, tableName);
        TableColumnDTO tableColumnDTO = mapTableName2TableComment(tableInfo);

        // 字段信息
        StringBuilder columnInfoSql = appendColumnInfoSql(JDBC_DATABASE, tableName);
        List<Map<String, Object>> columnInfos = selectAsMapList(dataSource, columnInfoSql.toString());
        fillColumnInfo(tableColumnDTO, columnInfos);

        // Model Freemarker
        ModelFtl modelFtl = createModelFtl(tableColumnDTO);
        String modelName = modelFtl.getModelName();
        String fileContent = formatModelFtl(modelFtl);

        // Model 输出文件
        writeModelFile(modelName, fileContent);

        // Mapper.java Freemarker
        MapperJavaFtl mapperJavaFtl = createMapperJavaFtl(tableColumnDTO);
        fileContent = formatMapperJavaFtl(mapperJavaFtl);

        // Mapper.java 输出文件
        writeMapperJavaFile(modelName, fileContent);

        // Mapper.xml Freemarker
        MapperXmlFtl mapperXmlFtl = createMapperXmlFtl(tableColumnDTO);
        fileContent = formatMapperXmlFtl(mapperXmlFtl);

        // Mapper.xml 输出文件
        writeMapperXmlFile(modelName, fileContent);

    }

    private static DataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUsername(JDBC_USERNAME);
        dataSource.setPassword(JDBC_PASSWORD);
        dataSource.setJdbcUrl("jdbc:mysql://" + JDBC_IP + ":" + JDBC_PORT + "/" + "information_schema");
        return dataSource;
    }

    private static StringBuilder appendTableInfoSql(String database, String tableName) {
        StringBuilder sql = new StringBuilder(128);
        sql.append("SELECT * FROM `TABLES` WHERE TABLE_SCHEMA = '");
        sql.append(database);
        sql.append("' AND TABLE_NAME IN (");
        sql.append("'");
        sql.append(tableName);
        sql.append("',");
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql;
    }

    private static List<Map<String, Object>> selectAsMapList(DataSource dataSource, String sql) {
        QueryRunner qr = new QueryRunner(dataSource);
        MapListHandler mapListHandler = new MapListHandler();
        List<Map<String, Object>> result = null;
        try {
            result = qr.query(sql, mapListHandler);
        } catch (SQLException e) {
            log.error("数据库连接失败", e);
        }
        return result;
    }

    private static Map<String, Object> getFirstTableInfo(List<Map<String, Object>> tableInfos, String tableName) {
        if (tableInfos.size() < 1) {
            log.error("表不存在 {}", tableName);
            System.exit(0);
        }
        return tableInfos.get(0);
    }

    private static TableColumnDTO mapTableName2TableComment(Map<String, Object> tableInfo) {
        return TableColumnDTO.builder()
                .name((String) tableInfo.get("TABLE_NAME"))
                .comment((String) tableInfo.get("TABLE_COMMENT"))
                .columns(Lists.newArrayList()).build();
    }

    private static StringBuilder appendColumnInfoSql(String database, String tableName) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("SELECT * FROM `COLUMNS` WHERE TABLE_SCHEMA = '");
        sb.append(database);
        sb.append("' AND TABLE_NAME IN (");
        sb.append("'");
        sb.append(tableName);
        sb.append("',");
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb;
    }

    private static void fillColumnInfo(TableColumnDTO tableColumnDTO, List<Map<String, Object>> columnInfos) {
        for (Map<String, Object> columnInfo : columnInfos) {
            ColumnDTO column = ColumnDTO.builder()
                    .name((String) columnInfo.get("COLUMN_NAME"))
                    .comment((String) columnInfo.get("COLUMN_COMMENT"))
                    .type((String) columnInfo.get("DATA_TYPE"))
                    .length((BigInteger) columnInfo.get("CHARACTER_MAXIMUM_LENGTH"))
                    .isTinyint1Unsigned("tinyint(1) unsigned".equals(columnInfo.get("COLUMN_TYPE")))
                    .build();
            tableColumnDTO.getColumns().add(column);
        }
    }

    private static ModelFtl createModelFtl(TableColumnDTO tableColumnDTO) {
        ModelFtl modelFtl = new ModelFtl();
        modelFtl.setPackageReference(BASE_PACKAGE_REFERENCE);
        modelFtl.setModelCnsName(tableColumnDTO.getComment());
        modelFtl.setAuthor(AUTHOR);
        String tableName = tableColumnDTO.getName();
        modelFtl.setTableName(tableName);
        modelFtl.setModelName(StringCaseUtils.snakeToUpperCamel(tableName));

        List<ModelFtl.Property> properties = Lists.newArrayList();
        for (ColumnDTO columnDTO : tableColumnDTO.getColumns()) {
            ModelFtl.Property property = new ModelFtl.Property();
            property.setFieldCnsName(columnDTO.getComment());
            String columnName = columnDTO.getName();
            property.setIsVersion(VERSION_COLUMN_NAME.equals(columnName));
            property.setColumnName(columnName);
            property.setFieldType(TypeHander.toJavaTypeName(columnDTO));
            property.setFieldName(StringCaseUtils.snakeToLowerCamel(columnName));
            properties.add(property);
        }
        modelFtl.setProperties(properties);

        return modelFtl;
    }

    private static String formatModelFtl(ModelFtl modelFtl) {
        String result = "";

        Version version = new Version("2.3.23");
        Configuration cfg = new Configuration(version);
        String folderPath = System.getProperty("user.dir") + ".src.main.resources.".replace('.', File.separatorChar)
                + "new-freemarker-template" + sep;
        try (StringWriter out = new StringWriter()) {
            cfg.setDirectoryForTemplateLoading(new File(folderPath));
            Template template = cfg.getTemplate("model.ftl", "utf-8");

            template.process(modelFtl, out);
            out.flush();
            result = out.getBuffer().toString();
        } catch (IOException | TemplateException e) {
            log.error("", e);
            System.exit(0);
        }
        return result;
    }

    private static void writeModelFile(String fileName, String fileContent) {
        try {
            FileUtils.write(new File(PROJECT_PATH + (".src.main.java." + BASE_PACKAGE_REFERENCE + ".model.")
                    .replace('.', File.separatorChar) + fileName + ".java"), fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private static MapperJavaFtl createMapperJavaFtl(TableColumnDTO tableColumnDTO) {
        MapperJavaFtl mapperJavaFtl = new MapperJavaFtl();
        mapperJavaFtl.setPackageReference(BASE_PACKAGE_REFERENCE);
        mapperJavaFtl.setModelCnsName(tableColumnDTO.getComment());
        mapperJavaFtl.setAuthor(AUTHOR);
        String tableName = tableColumnDTO.getName();
        mapperJavaFtl.setModelName(StringCaseUtils.snakeToUpperCamel(tableName));
        return mapperJavaFtl;
    }

    private static String formatMapperJavaFtl(MapperJavaFtl mapperJavaFtl) {
        String result = "";

        Version version = new Version("2.3.23");
        Configuration cfg = new Configuration(version);
        String folderPath = System.getProperty("user.dir") + ".src.main.resources.".replace('.', File.separatorChar)
                + "new-freemarker-template" + sep;
        try (StringWriter out = new StringWriter()) {
            cfg.setDirectoryForTemplateLoading(new File(folderPath));
            Template template = cfg.getTemplate("mapper.java.ftl", "utf-8");

            template.process(mapperJavaFtl, out);
            out.flush();
            result = out.getBuffer().toString();
        } catch (IOException | TemplateException e) {
            log.error("", e);
            System.exit(0);
        }
        return result;
    }

    private static void writeMapperJavaFile(String modelName, String fileContent) {
        try {
            FileUtils.write(new File(PROJECT_PATH + (".src.main.java." + BASE_PACKAGE_REFERENCE + ".dao.")
                            .replace('.', File.separatorChar) + modelName + "Mapper.java"), fileContent,
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private static MapperXmlFtl createMapperXmlFtl(TableColumnDTO tableColumnDTO) {
        MapperXmlFtl mapperXmlFtl = new MapperXmlFtl();
        mapperXmlFtl.setPackageReference(BASE_PACKAGE_REFERENCE);
        mapperXmlFtl.setModelName(StringCaseUtils.snakeToUpperCamel(tableColumnDTO.getName()));
        return mapperXmlFtl;
    }

    private static String formatMapperXmlFtl(MapperXmlFtl mapperXmlFtl) {
        String result = "";

        Version version = new Version("2.3.23");
        Configuration cfg = new Configuration(version);
        String folderPath = System.getProperty("user.dir") + ".src.main.resources.".replace('.', File.separatorChar)
                + "new-freemarker-template" + sep;
        try (StringWriter out = new StringWriter()) {
            cfg.setDirectoryForTemplateLoading(new File(folderPath));
            Template template = cfg.getTemplate("mapper.xml.ftl", "utf-8");

            template.process(mapperXmlFtl, out);
            out.flush();
            result = out.getBuffer().toString();
        } catch (IOException | TemplateException e) {
            log.error("", e);
            System.exit(0);
        }
        return result;
    }

    private static void writeMapperXmlFile(String modelName, String fileContent) {
        try {
            FileUtils.write(new File(PROJECT_PATH + (".src.main.resources.mapper.").replace('.', File.separatorChar)
                    + modelName + "Mapper.xml"), fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public static void main(String[] args) {
        generator("generator_demo");
    }

}
