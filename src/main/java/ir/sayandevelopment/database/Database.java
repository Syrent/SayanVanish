package ir.sayandevelopment.database;

import ir.sayandevelopment.VanishedPlayer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public interface Database {
    void init();

    boolean hasStats(UUID uuid);

    void updateVanishedPlayer(VanishedPlayer vanishedPlayer);

    VanishedPlayer getVanishedPlayers(UUID uuid);

    String getName(UUID uuid);

    String getUUID(String name);

    String getGameMode(UUID uuid);

    void setGameMode(UUID uuid, String gameMode);

    boolean isVanished(UUID uuid);

    void setVanished(UUID uuid, boolean vanished);

    boolean isOnline(UUID uuid);

    void setOnline(UUID uuid, boolean online);

    int vanishedCount();

    int vanishedCount(String gameMode);

    Set<String> getVanishedPlayers();
}
