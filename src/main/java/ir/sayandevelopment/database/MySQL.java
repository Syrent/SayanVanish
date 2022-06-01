package ir.sayandevelopment.database;

import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.VelocityMain;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.hikari.HikariConfig;
import me.mohamad82.ruom.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MySQL implements Database {

    private HikariDataSource dataSource;
    private final String host;
    private final String database;
    private final String user;
    private final String pass;
    private final int port;
    private final boolean ssl;
    private final boolean certificateVerification;
    private final int poolSize;
    private final int maxLifetime;

    public MySQL() {
        this.host = "localhost";
        this.database = "server";
        this.user = "server";
        this.pass = "yG%@NU6wz}i#)ZQN";
        this.port = 3306;
        this.ssl = false;
        this.certificateVerification = true;
        this.poolSize = 10;
        this.maxLifetime = 1800;
    }

    public boolean connect() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setPoolName("SayanVanishMySQLPool");

        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMaxLifetime(maxLifetime * 1000L);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);

        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);

        hikariConfig.addDataSourceProperty("useSSL", String.valueOf(ssl));
        if (!certificateVerification) {
            hikariConfig.addDataSourceProperty("verifyServerCertificate", String.valueOf(false));
        }

        hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
        hikariConfig.addDataSourceProperty("encoding", "UTF-8");
        hikariConfig.addDataSourceProperty("useUnicode", "true");

        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("jdbcCompliantTruncation", "false");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "275");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Recover if connection gets interrupted
        hikariConfig.addDataSourceProperty("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));

        dataSource = new HikariDataSource(hikariConfig);

        try {
            dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void init() {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS sayanvanish_vanishedplayers " +
                    "(uuid VARCHAR(64) UNIQUE, name VARCHAR(16), gamemode VARCHAR(32), " +
                    "vanished BOOLEAN DEFAULT FALSE, online BOOLEAN DEFAULT FALSE);";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasStats(UUID uuid) {
        String sql = "SELECT uuid FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void updateVanishedPlayer(VanishedPlayer vanishedPlayer) {
        String sql;
        try (Connection connection = dataSource.getConnection()) {
            if (hasStats(vanishedPlayer.getUuid())) {
                sql = "UPDATE sayanvanish_vanishedplayers SET uuid=?, name=?, gamemode=?, vanished=?, online=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, vanishedPlayer.getUuid().toString());
                    statement.setString(2, vanishedPlayer.getUserName());
                    statement.setString(3, vanishedPlayer.getGameMode());
                    statement.setBoolean(4, vanishedPlayer.isVanished());
                    statement.setBoolean(5, vanishedPlayer.isOnline());
                    statement.setString(6, vanishedPlayer.getUuid().toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO sayanvanish_vanishedplayers (uuid, name, gamemode, vanished, online) VALUES (?, ?, ?, ?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, vanishedPlayer.getUuid().toString());
                    statement.setString(2, vanishedPlayer.getUserName());
                    statement.setString(3, vanishedPlayer.getGameMode());
                    statement.setBoolean(4, vanishedPlayer.isVanished());
                    statement.setBoolean(5, vanishedPlayer.isOnline());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public VanishedPlayer getVanishedPlayers(UUID uuid) {
        VanishedPlayer vanishedPlayer = new VanishedPlayer(null, null, uuid, false);
        String sql = "SELECT name, gamemode, vanished, online FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        vanishedPlayer.setUserName(result.getString(1));
                        vanishedPlayer.setGameMode(result.getString(2));
                        vanishedPlayer.setVanished(result.getBoolean(3));
                        vanishedPlayer.setOnline(result.getBoolean(4));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vanishedPlayer;
    }

    @Override
    public String getName(UUID uuid) {
        String sql = "SELECT name FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getString(1);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    @Override
    public String getUUID(String username) {
        String sql = "SELECT uuid FROM sayanvanish_vanishedplayers WHERE name = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getString(1);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    @Override
    public String getGameMode(UUID uuid) {
        String sql = "SELECT gamemode FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getString(1);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    @Override
    public void setGameMode(UUID uuid, String gameMode) {
        String sql = "SELECT gamemode FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        sql = "UPDATE sayanvanish_vanishedplayers SET gamemode = ? WHERE uuid = ?;";
                        try (PreparedStatement statement2 = connection.prepareStatement(sql)) {
                            statement2.setString(1, gameMode);
                            statement2.setString(2, uuid.toString());
                            statement2.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isVanished(UUID uuid) {
        String sql = "SELECT vanished FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getBoolean(1);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void setVanished(UUID uuid, boolean vanished) {
        String sql = "SELECT vanished FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        sql = "UPDATE sayanvanish_vanishedplayers SET vanished = ? WHERE uuid = ?;";
                        try (PreparedStatement statement2 = connection.prepareStatement(sql)) {
                            statement2.setBoolean(1, vanished);
                            statement2.setString(2, uuid.toString());
                            statement2.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isOnline(UUID uuid) {
        String sql = "SELECT online FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getBoolean(1);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void setOnline(UUID uuid, boolean online) {
        String sql = "SELECT online FROM sayanvanish_vanishedplayers WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        sql = "UPDATE sayanvanish_vanishedplayers SET online = ? WHERE uuid = ?;";
                        try (PreparedStatement statement2 = connection.prepareStatement(sql)) {
                            statement2.setBoolean(1, online);
                            statement2.setString(2, uuid.toString());
                            statement2.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int vanishedCount() {
        String sql = "SELECT online FROM sayanvanish_vanishedplayers WHERE vanished = 1 AND online = 1;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while (result.next()) {
                        count++;
                    }
                    return count;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public int vanishedCount(String gameMode) {
        String sql = "SELECT online FROM sayanvanish_vanishedplayers WHERE vanished = 1 AND online = 1 AND gamemode = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, gameMode);
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while (result.next()) {
                        count++;
                    }
                    return count;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public Set<String> getVanishedPlayers() {
        Set<String> vanishedPlayers = new HashSet<>();
        String sql = "SELECT name FROM sayanvanish_vanishedplayers WHERE vanished = 1 AND online = 1;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        vanishedPlayers.add(result.getString(1));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return vanishedPlayers;
    }
}
