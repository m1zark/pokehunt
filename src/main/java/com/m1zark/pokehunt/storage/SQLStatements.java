package com.m1zark.pokehunt.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.m1zark.pokehunt.PHInfo;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.util.BountiesManager;
import com.m1zark.pokehunt.util.HuntsManager;
import com.m1zark.pokehunt.config.BountiesConfig;
import com.m1zark.pokehunt.config.HuntsConfig;
import com.m1zark.pokehunt.util.logs.Log;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Date;

public class SQLStatements {
    private String bountiesTable;
    private String huntsTable;
    private String playerDataTable;
    private String logTable;

    public SQLStatements(String bountiesTable, String huntsTable, String playerDataTable, String logTable) {
        this.bountiesTable = bountiesTable;
        this.huntsTable = huntsTable;
        this.playerDataTable = playerDataTable;
        this.logTable = logTable;
    }

    public void createTables() {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.bountiesTable + "` (ID INTEGER, date MEDIUMTEXT, loreText INTEGER, pokemon_name CHAR(36), pokemon_form INTEGER, pokemon_ability CHAR(36), pokemon_size CHAR(36), pokemon_gender CHAR(36), pokemon_ball CHAR(36), pokemon_nature CHAR(36), PRIMARY KEY(ID))")) {
                statement.executeUpdate();
            }

            try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.huntsTable + "` (ID INTEGER, date MEDIUMTEXT, loreText INTEGER, pokemon_name CHAR(36), pokemon_form INTEGER, pokemon_size CHAR(36), pokemon_gender CHAR(36), pokemon_nature CHAR(36), PRIMARY KEY(ID))")) {
                statement.executeUpdate();
            }

            try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.playerDataTable + "` (playerUUID CHAR(36), completions INTEGER DEFAULT 0, hunt_completions INTEGER DEFAULT 0, b1 BOOLEAN NOT NULL DEFAULT FALSE, b2 BOOLEAN NOT NULL DEFAULT FALSE, b3 BOOLEAN NOT NULL DEFAULT FALSE, b4 BOOLEAN NOT NULL DEFAULT FALSE, b5 BOOLEAN NOT NULL DEFAULT FALSE, h1 INTEGER DEFAULT 0, h2 INTEGER DEFAULT 0, h3 INTEGER DEFAULT 0, h4 INTEGER DEFAULT 0, h5 INTEGER DEFAULT 0, PRIMARY KEY(playerUUID))")) {
                statement.executeUpdate();
            }

            try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.logTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, log MEDIUMTEXT, PRIMARY KEY(ID))")) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void initializeBounties() {
        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.bountiesTable + "`").executeQuery()) {
                if(!results.next()) {
                    int i = 1;
                    while(i <= BountiesConfig.getNumberBounties()){
                        BountiesManager bounty = new BountiesManager(i);

                        try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.bountiesTable + "` (ID, date, loreText, pokemon_name, pokemon_form, pokemon_ability, pokemon_size, pokemon_gender, pokemon_ball, pokemon_nature) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                            statement.setInt(1, i);
                            statement.setString(2, Instant.now().plusSeconds(BountiesConfig.getTime(i)).toString());
                            statement.setInt(3, bounty.getFlavorText());
                            statement.setString(4, bounty.getName());
                            statement.setInt(5, bounty.getForm());
                            statement.setString(6, bounty.getAbility());
                            statement.setString(7, bounty.getGrowth());
                            statement.setString(8, bounty.getGender());
                            statement.setString(9, bounty.getPokeball());
                            statement.setString(10, bounty.getNature());
                            statement.executeUpdate();
                        }

                        i++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> loadBountiesData(int id) {
        HashMap<String, String> info = Maps.newHashMap();

        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.bountiesTable + "` WHERE ID = '" + id + "'").executeQuery()) {
                while(results.next()) {
                    info.put("date", results.getString("date"));
                    info.put("lore", String.valueOf(results.getInt("loreText")));
                    info.put("name", results.getString("pokemon_name"));
                    info.put("ability", results.getString("pokemon_ability"));
                    info.put("nature", results.getString("pokemon_nature"));
                    info.put("gender", results.getString("pokemon_gender"));
                    info.put("growth", results.getString("pokemon_size"));
                    info.put("pokeball", results.getString("pokemon_ball"));
                    info.put("form", String.valueOf(0));
                }
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return info;
        }
    }

    public void updateBounties(BountiesManager bounty, boolean player) {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("UPDATE `" + this.bountiesTable + "` SET date = ?, loreText = ?, pokemon_name = ?, pokemon_form = ?, pokemon_ability = ?, pokemon_size = ?, pokemon_gender = ?, pokemon_ball = ?, pokemon_nature = ? WHERE ID = ?")) {
                statement.setString(1, Instant.now().plusSeconds(BountiesConfig.getTime(bounty.getBountyID())).toString());
                statement.setInt(2, bounty.getFlavorText());
                statement.setString(3, bounty.getName());
                statement.setInt(4, bounty.getForm());
                statement.setString(5, bounty.getAbility());
                statement.setString(6, bounty.getGrowth());
                statement.setString(7, bounty.getGender());
                statement.setString(8, bounty.getPokeball());
                statement.setString(9, bounty.getNature());
                statement.setInt(10, bounty.getBountyID());
                statement.executeUpdate();
            }

            if(player) {
                String id = "b" + bounty.getBountyID();
                try (PreparedStatement update = connection.prepareStatement("UPDATE `" + this.playerDataTable + "` SET " + id + " = false")) {
                    update.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void initializeHunts() {
        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.huntsTable + "`").executeQuery()) {
                if(!results.next()) {
                    int i = 1;
                    while(i <= HuntsConfig.getNumberHunts()){
                        HuntsManager hunts = new HuntsManager(i);

                        try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.huntsTable + "` (ID, date, loreText, pokemon_name, pokemon_form, pokemon_size, pokemon_gender, pokemon_nature) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                            statement.setInt(1, i);
                            statement.setString(2, Instant.now().plusSeconds(HuntsConfig.getTime(i)).toString());
                            statement.setInt(3, hunts.getFlavorText());
                            statement.setString(4, hunts.getName());
                            statement.setInt(5, hunts.getForm());
                            statement.setString(6, hunts.getGrowth());
                            statement.setString(7, hunts.getGender());
                            statement.setString(8, hunts.getNature());
                            statement.executeUpdate();
                        }

                        i++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> loadHuntsData(int id) {
        HashMap<String, String> info = Maps.newHashMap();

        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.huntsTable + "` WHERE ID = '" + id + "'").executeQuery()) {
                while(results.next()) {
                    info.put("date", results.getString("date"));
                    info.put("lore", String.valueOf(results.getInt("loreText")));
                    info.put("name", results.getString("pokemon_name"));
                    info.put("form", String.valueOf(results.getInt("pokemon_form")));
                    info.put("nature", results.getString("pokemon_nature"));
                    info.put("gender", results.getString("pokemon_gender"));
                    info.put("growth", results.getString("pokemon_size"));
                }
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return info;
        }
    }

    public void updateHunts(HuntsManager hunt, boolean player) {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("UPDATE `" + this.huntsTable + "` SET date = ?, loreText = ?, pokemon_name = ?, pokemon_form = ?, pokemon_size = ?, pokemon_gender = ?, pokemon_nature = ? WHERE ID = ?")) {
                statement.setString(1, Instant.now().plusSeconds(HuntsConfig.getTime(hunt.getHuntID())).toString());
                statement.setInt(2, hunt.getFlavorText());
                statement.setString(3, hunt.getName());
                statement.setInt(4, hunt.getForm());
                statement.setString(5, hunt.getGrowth());
                statement.setString(6, hunt.getGender());
                statement.setString(7, hunt.getNature());
                statement.setInt(8, hunt.getHuntID());
                statement.executeUpdate();
            }

            if(player) {
                String id = "h" + hunt.getHuntID();
                try (PreparedStatement update = connection.prepareStatement("UPDATE `" + this.playerDataTable + "` SET " + id + " = 0")) {
                    update.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void addPlayerData(UUID uuid) {
        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet check = connection.prepareStatement("SELECT * FROM `" + this.playerDataTable + "` WHERE playerUUID = '" + uuid + "'").executeQuery()) {
                if(!check.next()) {
                    try(PreparedStatement insert = connection.prepareStatement("INSERT INTO `" + this.playerDataTable + "` (playerUUID, completions, hunt_completions, b1, b2, b3, b4, b5, h1, h2, h3, h4, h5) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        insert.setString(1, uuid.toString());
                        insert.setInt(2, 0);
                        insert.setInt(3, 0);
                        insert.setBoolean(4, false);
                        insert.setBoolean(5, false);
                        insert.setBoolean(6, false);
                        insert.setBoolean(7, false);
                        insert.setBoolean(8, false);
                        insert.setInt(9, 0);
                        insert.setInt(10, 0);
                        insert.setInt(11, 0);
                        insert.setInt(12, 0);
                        insert.setInt(13, 0);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerData(Player player, int id, String type) {
        try(Connection connection = DataSource.getConnection()) {
            if(type.equals("bounty")) {
                try (PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.playerDataTable + "` SET b" + id + " = true WHERE playerUUID = '" + player.getUniqueId() + "'")) {
                    updatePlayer.executeUpdate();
                }

                for (String searching : BountiesConfig.getHuntCompletions().trim().split(",")) {
                    if (String.valueOf(id).equals(searching)) {
                        try (PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.playerDataTable + "` SET completions = completions + 1 WHERE playerUUID = '" + player.getUniqueId() + "'")) {
                            updatePlayer.executeUpdate();
                        }
                    }
                }
            } else {
                try (PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.playerDataTable + "` SET h"+id+" = h"+id+" + 1 WHERE playerUUID = '" + player.getUniqueId() + "'")) {
                    updatePlayer.executeUpdate();
                }

                for (String searching : HuntsConfig.getHuntCompletions().trim().split(",")) {
                    if (String.valueOf(id).equals(searching)) {
                        try (PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.playerDataTable + "` SET hunt_completions = hunt_completions + 1 WHERE playerUUID = '" + player.getUniqueId() + "'")) {
                            updatePlayer.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerCompletions(Player player, String type) {
        int completion = 0;

        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.playerDataTable + "` WHERE playerUUID = '" + player.getUniqueId() + "'").executeQuery()) {
                if(results.next()) {
                    completion = type.equals("bounty") ? results.getInt("completions") : results.getInt("hunt_completions");
                }
                return completion;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return completion;
        }
    }

    public int getPlayerHuntCount(Player player, int id) {
        int count = 0;

        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.playerDataTable + "` WHERE playerUUID = '" + player.getUniqueId() + "'").executeQuery()) {
                if(results.next()) {
                    count = results.getInt("h"+id);
                }
                return count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return count;
        }
    }

    public Date getExpires(int id, String type) {
        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet end = connection.prepareStatement("SELECT date FROM `" + (type.equals("bounties") ? this.bountiesTable : this.huntsTable) + "` WHERE ID = '" + id + "'").executeQuery()) {
                if(end.next()) {
                    return Date.from(Instant.parse(end.getString("date")));
                }
                return new Date();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public boolean checkCompleted(UUID uuid, int id, String type) {
        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.playerDataTable + "` WHERE playerUUID = '" + uuid + "'").executeQuery()) {
                if(type.equals("bounty")) return results.next() && results.getBoolean("b"+id);
                else return results.next() && results.getInt("h"+id) >= HuntsConfig.getAmountNeeded(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public void addLog(Log log) {
        try(Connection connection = DataSource.getConnection()) {
            Gson gson = new Gson();
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.logTable + "` (log) VALUES (?)")) {
                statement.setString(1, gson.toJson(log));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Log> getLogs(String type) {
        try(Connection connection = DataSource.getConnection()) {
            Gson gson = new Gson();
            List<Log> logs = new ArrayList<>();

            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.logTable + "` ORDER BY ID DESC").executeQuery()) {
                while(results.next()) {
                    Log log = gson.fromJson(results.getString("log"), Log.class);
                    if(log.getHuntType().equalsIgnoreCase(type)) logs.add(log);
                }
                return logs;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }

    public void deleteLogs() {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + this.logTable + "`")) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
