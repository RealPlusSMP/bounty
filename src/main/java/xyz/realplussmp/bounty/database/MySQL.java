package xyz.realplussmp.bounty.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import xyz.realplussmp.bounty.Bounty;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL implements Database {

    private final HikariDataSource dataSource;

    public MySQL(Bounty plugin) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" +
                plugin.getConfig().getString("database.host") + ":" +
                plugin.getConfig().getInt("database.port") + "/" +
                plugin.getConfig().getString("database.name"));
        config.setUsername(plugin.getConfig().getString("database.user"));
        config.setPassword(plugin.getConfig().getString("database.pass"));
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);
    }

    public void connect() {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bounties (
                    uuid VARCHAR(36) PRIMARY KEY,
                    amount DOUBLE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}