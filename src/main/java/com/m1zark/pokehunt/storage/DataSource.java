package com.m1zark.pokehunt.storage;

import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class DataSource extends SQLStatements {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    public DataSource(String bountiesTable, String huntsTable, String playerDataTable, String logTable) {
        super(bountiesTable, huntsTable, playerDataTable, logTable);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void shutdown() {
        if (ds != null) {
            ds.close();
        }
    }

    static {
        if(Config.storageType.equalsIgnoreCase("h2")) {
            config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
            config.addDataSourceProperty("URL", "jdbc:h2:" + PokeHunt.getInstance().getConfigDir() + "/storage/data;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MSSQLServer");
        } else {
            config.setJdbcUrl("jdbc:mysql://" + Config.mysqlURL);
            config.setUsername(Config.mysqlUsername);
            config.setPassword(Config.mysqlPassword);

            config.addDataSourceProperty("alwaysSendSetIsolation", false);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            config.addDataSourceProperty("prepStmtCacheSize", 250);
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("useServerPrepStmts", true);
            config.addDataSourceProperty("cacheCallableStmts", true);
            config.addDataSourceProperty("cacheServerConfiguration", true);
            config.addDataSourceProperty("elideSetAutoCommits", true);
            config.addDataSourceProperty("useLocalSessionState", true);
        }

        config.setPoolName("PokeHunts");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(10);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);
        config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10));
        config.setConnectionTestQuery("/* PokeHunts ping */ SELECT 1");
        config.setInitializationFailTimeout(1);
        ds = new HikariDataSource(config);
    }
}
