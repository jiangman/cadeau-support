package newmind.dto;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;

/**
 * @author Deolin 2018/11/14
 */
@Data
public class JdbcProperties {

    private String ip;

    private Integer port;

    private String database;

    private String username;

    private String password;

    private HikariDataSource dataSource;

    public JdbcProperties(String ip, Integer port, String database, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;

        dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + "information_schema");
    }

}
