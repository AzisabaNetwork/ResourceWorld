package jp.azisaba.main.resourceworld;

import org.bukkit.World.Environment;

public class RecreateWorld {

    private String worldName;
    private Environment type;
    private double worldBorderSize;
    private String portalName = null;

    private int protect = -1;

    public RecreateWorld(String worldName, Environment type, double worldBorderSize) {
        this.worldName = worldName;
        this.type = type;
        this.worldBorderSize = worldBorderSize;
    }

    public RecreateWorld(String worldName, Environment type, double worldBorderSize, String portal) {
        this.worldName = worldName;
        this.type = type;
        this.worldBorderSize = worldBorderSize;
        this.portalName = portal;
    }

    public RecreateWorld(String worldName, Environment type, double worldBorderSize, int protect) {
        this.worldName = worldName;
        this.type = type;
        this.worldBorderSize = worldBorderSize;
        this.protect = protect;
    }

    public RecreateWorld(String worldName, Environment type, double worldBorderSize, String portal, int protect) {
        this.worldName = worldName;
        this.type = type;
        this.worldBorderSize = worldBorderSize;
        this.portalName = portal;
        this.protect = protect;
    }

    public String getWorldName() {
        return worldName;
    }

    public Environment getEnvironment() {
        return type;
    }

    public double getWorldBorderSize() {
        return worldBorderSize;
    }

    public String getPortalName() {
        return portalName;
    }

    public int getProtect() {
        return protect;
    }
}
