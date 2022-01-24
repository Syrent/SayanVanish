package ir.sayandevelopment;

import ir.sayandevelopment.bridge.VanishSource;

import java.util.UUID;

public class VanishedPlayer {
    private String userName;
    private String gameMode;
    private UUID uuid;
    private boolean vanished;
    private boolean online;
    private VanishSource vanishSource;

    public VanishedPlayer(String userName, String gameMode, UUID uuid, boolean vanished) {
        this.userName = userName;
        this.uuid = uuid;
        this.gameMode = gameMode;
        this.vanished = vanished;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
    }

    public VanishSource getVanishSource() {
        return vanishSource;
    }

    public void setVanishSource(VanishSource vanishSource) {
        this.vanishSource = vanishSource;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
