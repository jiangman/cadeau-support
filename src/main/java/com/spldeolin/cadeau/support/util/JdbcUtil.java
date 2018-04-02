package com.spldeolin.cadeau.support.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JdbcUtil {

    private static Connection conn = null;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(ConfigUtil.getMysqlUrl(), ConfigUtil.getMysqlUsername(),
                    ConfigUtil.getMysqlPassword());
        } catch (Exception e) {
            log.error("checked", e);
            throw new RuntimeException();
        }
    }

    public static String getTableCommtents(String tableName) {
        String tableCommtents = "";
        try {
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery(
                    "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" +
                            ConfigUtil.getMysqlDatabase() + "'");
            while (rs.next()) {
                tableCommtents = rs.getString("TABLE_COMMENT");
            }
        } catch (SQLException e) {
            log.error("checked", e);
            throw new RuntimeException();
        }
        return tableCommtents;
    }

    public static String getColumnType(String schemaName, String tableName, String columnName) {
        String columnType = "";
        String sql = "SELECT COLUMN_TYPE FROM information_schema.COLUMNS WHERE ";
        sql += "TABLE_SCHEMA = '" + schemaName + "' ";
        sql += "AND TABLE_NAME = '" + tableName + "' ";
        sql += "AND COLUMN_NAME = '" + columnName + "'";

        try {
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            while (rs.next()) {
                columnType = rs.getString("COLUMN_TYPE");
            }
        } catch (SQLException e) {
            log.error("checked", e);
            throw new RuntimeException();
        }
        return columnType;
    }

}
