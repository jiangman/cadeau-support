package newmind;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import newmind.dto.ColumnDTO;
import newmind.dto.JdbcProperties;
import newmind.dto.TableColumnDTO;

/**
 * @author Deolin 2018/11/14
 */
@Log4j2
public class ModelGenerator {

    public static void generator(JdbcProperties jdbcProperties, List<String> tableNames) {
        StringBuilder tableInfoSql = appendTableInfoSql(jdbcProperties.getDatabase(), tableNames);
        List<Map<String, Object>> tableInfos = selectAsMapList(jdbcProperties.getDataSource(), tableInfoSql.toString());
        Map<String, TableColumnDTO> tableColumns = mapTableName2TableComment(tableInfos);
        log.info(tableColumns);

        StringBuilder columnInfoSql = appendColumnInfoSql(jdbcProperties.getDatabase(), tableNames);
        List<Map<String, Object>> columnInfos = selectAsMapList(jdbcProperties.getDataSource(),
                columnInfoSql.toString());
        fillColumnInfoToDTO(columnInfos, tableColumns);

        log.info(tableColumns);
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

    public static void main(String[] args) {
        generator(new JdbcProperties("192.168.2.2", 3306, "new_mind", "admin", "admin"),
                Lists.newArrayList("user", "permission"));
    }

}
