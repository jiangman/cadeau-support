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
import com.google.common.collect.Maps;
import com.spldeolin.cadeau.support.util.StringCaseUtils;
import com.spldeolin.cadeau.support.util.Times;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import lombok.extern.log4j.Log4j2;
import newmind.dto.ColumnDTO;
import newmind.dto.JdbcProperties;
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

    private static final String JDBC_IP = "192.168.2.2";

    private static final Integer JDBC_PORT = 3306;

    private static final String JDBC_DATABASE = "new_mind";

    private static final String JDBC_USERNAME = "admin";

    private static final String JDBC_PASSWORD = "admin";

    public static void generator(List<String> tableNames) {
        // JDBC
        JdbcProperties jdbcProperties = new JdbcProperties(JDBC_IP, JDBC_PORT, JDBC_DATABASE, JDBC_USERNAME,
                JDBC_PASSWORD);

        // 表信息
        StringBuilder tableInfoSql = appendTableInfoSql(jdbcProperties.getDatabase(), tableNames);
        List<Map<String, Object>> tableInfos = selectAsMapList(jdbcProperties.getDataSource(), tableInfoSql.toString());
        Map<String, TableColumnDTO> tableColumns = mapTableName2TableComment(tableInfos);

        // 字段信息
        StringBuilder columnInfoSql = appendColumnInfoSql(jdbcProperties.getDatabase(), tableNames);
        List<Map<String, Object>> columnInfos = selectAsMapList(jdbcProperties.getDataSource(),
                columnInfoSql.toString());
        fillColumnInfoToDTO(columnInfos, tableColumns);
        log.info(tableColumns);

        // Freemarker
        List<ModelFtl> modelFtls = createModelFtls(tableColumns);
        Map<String, String> fileName2Content = formatFtls(modelFtls, "model.ftl");

        // 输出文件
        writeFiles(fileName2Content);
    }

    private static StringBuilder appendTableInfoSql(String database, List<String> tableNames) {
        StringBuilder sql = new StringBuilder(128);
        sql.append("SELECT * FROM `TABLES` WHERE TABLE_SCHEMA = '");
        sql.append(database);
        sql.append("' AND TABLE_NAME IN (");
        for (String tableName : tableNames) {
            sql.append("'");
            sql.append(tableName);
            sql.append("',");
        }
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

    private static Map<String, TableColumnDTO> mapTableName2TableComment(List<Map<String, Object>> tableInfos) {
        Map<String, TableColumnDTO> result = Maps.newHashMapWithExpectedSize(tableInfos.size());
        for (Map<String, Object> tableInfo : tableInfos) {
            String tableName = (String) tableInfo.get("TABLE_NAME");
            result.put(tableName, TableColumnDTO.builder().name(tableName)
                    .comment((String) tableInfo.get("TABLE_COMMENT")).columns(Lists.newArrayList()).build());
        }
        return result;
    }

    private static StringBuilder appendColumnInfoSql(String database, List<String> tableNames) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("SELECT * FROM `COLUMNS` WHERE TABLE_SCHEMA = '");
        sb.append(database);
        sb.append("' AND TABLE_NAME IN (");
        for (String tableName : tableNames) {
            sb.append("'");
            sb.append(tableName);
            sb.append("',");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb;
    }

    private static void fillColumnInfoToDTO(List<Map<String, Object>> columnInfos,
            Map<String, TableColumnDTO> tableColumns) {
        for (Map<String, Object> columnInfo : columnInfos) {
            String tableName = (String) columnInfo.get("TABLE_NAME");
            TableColumnDTO tableColumn = tableColumns.get(tableName);
            if (tableColumn != null) {
                ColumnDTO column = ColumnDTO.builder()
                        .name((String) columnInfo.get("COLUMN_NAME"))
                        .comment((String) columnInfo.get("COLUMN_COMMENT"))
                        .type((String) columnInfo.get("DATA_TYPE"))
                        .length((BigInteger) columnInfo.get("CHARACTER_MAXIMUM_LENGTH"))
                        .isTinyint1Unsigned("tinyint(1) unsigned".equals(columnInfo.get("COLUMN_TYPE")))
                        .build();
                tableColumn.getColumns().add(column);
            }
        }
    }

    private static List<ModelFtl> createModelFtls(Map<String, TableColumnDTO> tableColumns) {
        List<ModelFtl> modelFtls = Lists.newArrayList();
        for (TableColumnDTO tableColumnDTO : tableColumns.values()) {
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
                property.setColumnName(columnName);
                property.setFieldType(TypeHander.toJavaTypeName(columnDTO));
                property.setFieldName(StringCaseUtils.snakeToLowerCamel(columnName));
                properties.add(property);
            }

            modelFtl.setProperties(properties);
            modelFtls.add(modelFtl);
        }
        return modelFtls;
    }

    private static Map<String, String> formatFtls(List<ModelFtl> modelFtls, String ftlFileName) {
        Map<String, String> result = Maps.newHashMap();

        Version version = new Version("2.3.23");
        Configuration cfg = new Configuration(version);
        String folderPath = System.getProperty("user.dir") + sep + "src" + sep + "main" + sep + "resources" + sep
                + "new-freemarker-template" + sep;
        try (StringWriter out = new StringWriter()) {
            cfg.setDirectoryForTemplateLoading(new File(folderPath));
            Template template = cfg.getTemplate(ftlFileName, "utf-8");

            for (ModelFtl modelFtl : modelFtls) {
                template.process(modelFtl, out);
                out.flush();
                result.put(modelFtl.getModelName(), out.getBuffer().toString());
            }
        } catch (IOException | TemplateException e) {
            log.error("", e);
            System.exit(0);
        }
        return result;
    }

    private static void writeFiles(Map<String, String> fileName2Content) {
        for (Map.Entry<String, String> entry : fileName2Content.entrySet()) {
            String fileName = entry.getKey();
            String fileContent = entry.getValue();
            try {
                FileUtils.write(new File(PROJECT_PATH + (".src.main.java." + BASE_PACKAGE_REFERENCE + ".model.")
                        .replace('.', File.separatorChar) + fileName + ".java"), fileContent, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

    public static void main(String[] args) {
        generator(Lists.newArrayList("generator_demo"));
    }

}
