package com.m1zark.pokehunt.storage;

import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.config.Config;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;

public class Migrate {
    private static Connection h2Connection = null;
    private static DataSource h2Source = null;

    private static Connection mysqlConnection = null;
    private static DataSource mysqlSource = null;

    private static void connectH2() {
        try {
            if (h2Source == null) {
                h2Source = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource("jdbc:h2:" + PokeHunt.getInstance().getConfigDir().toString() + File.separator + "/storage/data");
            }
            h2Connection = h2Source.getConnection();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private static void connectMYSQL() {
        try {
            if (mysqlSource == null) {
                mysqlSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource("jdbc:mysql://" + Config.mysqlUsername + ":" + Config.mysqlPassword + "@" + Config.mysqlURL);
            }
            mysqlConnection = mysqlSource.getConnection();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void migrate() {
        connectH2();
        connectMYSQL();

        try {
            final PreparedStatement insertStatement = h2Connection.prepareStatement("INSERT INTO PH_PLAYERDATA (playerUUID,completions) VALUES (?,?)");

            final Statement statement1 = mysqlConnection.createStatement();
            final ResultSet resultSet = statement1.executeQuery("SELECT playerUUID,completions FROM PH_COMPLETIONS WHERE completions > 0");
            while (resultSet.next()) {
                insertStatement.clearParameters();
                insertStatement.setString(1, resultSet.getString("playerUUID"));
                insertStatement.setInt(2, resultSet.getInt("completions"));
                insertStatement.executeUpdate();
            }

            mysqlConnection.close();
            h2Connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
