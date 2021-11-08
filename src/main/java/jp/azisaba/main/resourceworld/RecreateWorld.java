package jp.azisaba.main.resourceworld;

import org.bukkit.World.Environment;

public class RecreateWorld {

    private String worldName;
    private Environment type;
    private double worldBorderSize;
    private boolean keepInventory;
    private String portalName = null;

    private int protect = -1;

    public RecreateWorld(String worldName, Environment type, double worldBorderSize, boolean keepInventory) {
        this.worldName = worldName;
        this.type = type;
        this.worldBorderSize = worldBorderSize;
        this.keepInventory = keepInventory;
    }

    public RecreateWorld(String worldName, Environment type, double worldBorderSize, boolean keepInventory, String portal) {
        this.worldName = worldName;
        this.type = type;
        this.worldBorderSize = worldBorderSize;
        this.keepInventory = keepInventory;
        this.portalName = portal;
    }

    public RecreateWorld(String worldName, Environment type, double worldBorderSize, boolean keepInventory, int protect) {
        this.worldName = worldName;
        this.type = type;
        this.worldBorderSize = worldBorderSize;
        this.keepInventory = keepInventory;
        this.protect = protect;
    }

    public RecreateWorld(String worldName, Environment type, double worldBorderSize, boolean keepInventory, String portal, int protect) {
        this.worldName = worldName;
        this.type = type;
        this.worldBorderSize = worldBorderSize;
        this.keepInventory = keepInventory;
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

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public String getPortalName() {
        return portalName;
    }

    public int getProtect() {
        return protect;
    }
}
