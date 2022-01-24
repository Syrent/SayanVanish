package ir.sayandevelopment.database;

import ir.sayandevelopment.VanishedPlayer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class SQL {

    private Connection connection;

    public abstract void openConnection() throws Exception;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isClosed() throws SQLException {
        return connection == null || connection.isClosed() || !connection.isValid(0);
    }

    public void createTable() throws Exception {
        String sql;
        sql = "CREATE TABLE IF NOT EXISTS sayanvanish_vanishedplayers (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), gamemode VARCHAR(32), vanished BOOLEAN DEFAULT FALSE, online BOOLEAN DEFAULT FALSE);";
        execute(sql);
    }

    public void updateVanishedPlayer(VanishedPlayer vanishedPlayer) throws Exception {
        String sql = String.format("INSERT INTO sayanvanish_vanishedplayers (uuid, name, gamemode, vanished, online) " +
                "VALUES ('%s','%s','%s','%s','%s') ON DUPLICATE KEY UPDATE gamemode = '%s', vanished = '%s';",
                vanishedPlayer.getUuid(), vanishedPlayer.getUserName(), vanishedPlayer.getGameMode(),
                vanishedPlayer.isVanished() ? 1 : 0, vanishedPlayer.isOnline() ? 1 : 0, vanishedPlayer.getGameMode(),
                vanishedPlayer.isVanished() ? 1 : 0, vanishedPlayer.isOnline() ? 1 : 0);
        execute(sql);
    }

    public VanishedPlayer getVanishedPlayer(UUID uuid) throws Exception {
        String sql = String.format("SELECT * FROM sayanvanish_vanishedplayers WHERE uuid = '%s';", uuid.toString());
        try {
            VanishedPlayer vanishedPlayer = new VanishedPlayer(getInfo(sql, "name"), getInfo(sql, "gameMode"),
                    UUID.fromString(getInfo(sql, "uuid")), getBooleanInfo(sql, "vanished"));
            vanishedPlayer.setOnline(getBooleanInfo(sql, "online"));
            return vanishedPlayer;
        } catch (IllegalArgumentException e) {
            return new VanishedPlayer(null, null, null, false);
        }
    }

    public List<VanishedPlayer> getVanishedPlayers() throws Exception {
        List<VanishedPlayer> vanishedPlayers = new ArrayList<>();
        String sql = "SELECT * FROM sayanvanish_vanishedplayers WHERE vanished = 1 AND online = 1;";
        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            VanishedPlayer vanishedPlayer = new VanishedPlayer(getInfo(sql, "name"), getInfo(sql, "gameMode"),
                    UUID.fromString(getInfo(sql, "uuid")), getBooleanInfo(sql, "vanished"));
            vanishedPlayer.setOnline(getBooleanInfo(sql, "online"));
            vanishedPlayers.add(vanishedPlayer);
        }
        return vanishedPlayers;
    }

    public int getVanishedPlayersCount() throws Exception {
        String sql = "SELECT vanished FROM sayanvanish_vanishedplayers WHERE vanished = 1 AND online = 1;";
        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        int count = 0;
        while (resultset.next()) {
            count++;
        }
        return count;
    }

    public int getVanishedPlayersCount(String gameMode) throws Exception {
        String sql = "SELECT * FROM sayanvanish_vanishedplayers WHERE vanished = 1 AND online = 1 AND gamemode = '" + gameMode + "';";
        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        int count = 0;
        while (resultset.next()) {
            count++;
        }
        return count;
    }

    public boolean isOnline(String uuid) throws Exception {
        String sql = "SELECT online FROM sayanvanish_vanishedplayers WHERE uuid = '" + uuid + "';";
        return getBooleanInfo(sql, "online");
    }

    public void setOnline(String uuid, boolean online) throws Exception {
        String sql = "SELECT uuid FROM sayanvanish_vanishedplayers WHERE uuid = '" + uuid + "';";
        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        if (resultset.next()) {
            sql = String.format("UPDATE sayanvanish_vanishedplayers SET online = %s WHERE uuid = '%s';", online ? 1 : 0, uuid);
            execute(sql);
        }
    }

    public void setGameMode(String uuid, String gameMode) throws Exception {
        String sql = "SELECT uuid FROM sayanvanish_vanishedplayers WHERE uuid = '" + uuid + "';";
        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        if (resultset.next()) {
            sql = String.format("UPDATE sayanvanish_vanishedplayers SET gamemode = '%s' WHERE uuid = '%s';", gameMode, uuid);
            execute(sql);
        }
    }

    private String getInfo(String sql, String column) throws Exception {
        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);


        String result = "";
        if (resultset.next()) {
            result = resultset.getString(column);
        }

        resultset.close();
        statement.close();

        return result;
    }

    private boolean getBooleanInfo(String sql, String column) throws Exception {
        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);


        boolean result = false;
        if (resultset.next()) {
            result = resultset.getBoolean(column);
        }

        resultset.close();
        statement.close();

        return result;
    }

    private void execute(String sql) throws Exception {
        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }
}
