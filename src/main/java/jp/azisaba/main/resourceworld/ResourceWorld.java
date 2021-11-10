package jp.azisaba.main.resourceworld;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import jp.azisaba.main.resourceworld.command.ResourceWorldCommand;
import jp.azisaba.main.resourceworld.listeners.ProtectSpawnListener;
import jp.azisaba.main.resourceworld.task.BroadcastWarningTask;
import jp.azisaba.main.resourceworld.task.ResourceWorldCreateTask;
import jp.azisaba.main.resourceworld.task.SpawnPointTaskManager;
import jp.azisaba.main.resourceworld.utils.Safety;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

public class ResourceWorld extends JavaPlugin {

    private final String PLUGIN_NAME = "ResourceWorld";

    public ResourceWorldConfig config;

    private ResourceWorldCreateTask createTask;
    private BroadcastWarningTask warningTask;

    private static void delete(String path) {
        File filePath = new File(path);
        String[] list = filePath.list();
        for (String file : list) {
            File f = new File(path + File.separator + file);
            if (f.isDirectory()) {
                delete(path + File.separator + file);
            } else {
                f.delete();
            }
        }
        filePath.delete();
    }

    @Override
    public void onEnable() {

        this.config = new ResourceWorldConfig(this);
        this.config.loadConfig();

        if (config.createWorldList.size() > 0) {
            createTask = new ResourceWorldCreateTask(this, new ArrayList<RecreateWorld>(config.createWorldList));
            createTask.runTask();

            warningTask = new BroadcastWarningTask(this);
            warningTask.runTask();
        }

        SpawnPointTaskManager.init(this);

        Bukkit.getPluginCommand("resourceworld").setExecutor(new ResourceWorldCommand(this));

        Bukkit.getPluginManager().registerEvents(new ProtectSpawnListener(this, config.createWorldList), this);
//		Bukkit.getPluginManager().registerEvents(new CreateSafetySpawnListener(this, config.createWorldList), this);

        Bukkit.getLogger().info(PLUGIN_NAME + " enabled.");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(PLUGIN_NAME + " disabled.");
    }

    public boolean recreateResourceWorld(RecreateWorld createWorld) {
        if (this.config.useMultiverse) {

            if (!isEnableMultiverse()) {
                getLogger().warning("MultiverseCore Pluginがロードされていません。通常の方法で生成します。");
                return generateNormally(createWorld);
            }

            MVWorldManager manager = ((MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core"))
                    .getMVWorldManager();

            MultiverseWorld world = manager.getMVWorld(createWorld.getWorldName() + "-ready");
            if (world == null) {
                return generateWithMultiverse(createWorld);
            } else {
                return moveWolrdWithMultiverse(createWorld);
            }

        } else {

            return generateNormally(createWorld);
        }
    }

    public void recreateAllResourceWorlds() {
        if (createTask != null) {
            createTask.stopTask();
        }
        if (warningTask != null) {
            warningTask.stopTask();
        }

        for (RecreateWorld world : config.createWorldList) {
            recreateResourceWorld(world);
        }
    }

    private boolean generateNormally(RecreateWorld createWorld) {

        World world = Bukkit.getWorld(createWorld.getWorldName());

        if (world != null) {
            Bukkit.unloadWorld(world, false);
            try {
                delete(world.getWorldFolder().getPath());
            } catch (Exception e) {
                this.getLogger().warning(createWorld.getWorldName() + "のunloadに失敗。");
                e.printStackTrace();
                return false;
            }
        }

        World newWorld = generateWorld(createWorld.getWorldName(), createWorld.getEnvironment());

        newWorld.getWorldBorder().setSize(createWorld.getWorldBorderSize());
        newWorld.setGameRule(GameRule.KEEP_INVENTORY, createWorld.isKeepInventory());
        return true;
    }

    private boolean generateWithMultiverse(RecreateWorld createWorld) {

        MVWorldManager manager = ((MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core"))
                .getMVWorldManager();

        if (manager.getMVWorld(createWorld.getWorldName()) != null) {
            manager.deleteWorld(createWorld.getWorldName(), false, true);
        }

        boolean success = manager.addWorld(createWorld.getWorldName(), createWorld.getEnvironment(), null,
                WorldType.NORMAL, true, null);
        if (!success) {
            getLogger().warning("ワールド生成に失敗。");
            return false;
        }

        MultiverseWorld mvWorld = manager.getMVWorld(createWorld.getWorldName());

        boolean b = manager.loadWorld(createWorld.getWorldName());

        if (!b) {
            getLogger().warning("ロードに失敗。");
            return false;
        } else {
            getLogger().info("ロード成功。");
        }

        World world = Bukkit.getWorld(createWorld.getWorldName());
        world.getWorldBorder().setSize(createWorld.getWorldBorderSize());
        world.getWorldBorder().setCenter(mvWorld.getSpawnLocation());

        Location spawn = mvWorld.getSpawnLocation();
        spawn.setX(0.5);
        spawn.setZ(0.5);

        world.setGameRule(GameRule.KEEP_INVENTORY, createWorld.isKeepInventory());

        Location loc = getTopLocation(spawn);

        mvWorld.setAdjustSpawn(false);

        if (createWorld.getEnvironment() == Environment.NORMAL) {
            loc.setY(63);
            mvWorld.getCBWorld().setSpawnLocation(loc);

            Safety.createFloor(loc, Material.STONE, createWorld.getProtect(), createWorld.getProtect());
            Safety.createSpace(loc, createWorld.getProtect(), 20, createWorld.getProtect());
        } else if (createWorld.getEnvironment() == Environment.NETHER) {
            loc = mvWorld.getSpawnLocation();

            loc.setY(32);

            mvWorld.setSpawnLocation(loc);

            Safety.createFloor(loc, Material.NETHERRACK, createWorld.getProtect(), createWorld.getProtect());
            Safety.createSpace(loc, createWorld.getProtect(), 5, createWorld.getProtect());
        } else if (createWorld.getEnvironment() == Environment.THE_END) {

            Location check = new Location(world, 5, 70, 5);
            mvWorld.setSpawnLocation(getTopLocation(check));
        }
        return true;
    }

    private boolean moveWolrdWithMultiverse(RecreateWorld createWorld) {
        MVWorldManager manager = ((MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core"))
                .getMVWorldManager();

        MultiverseWorld before = manager.getMVWorld(createWorld.getWorldName() + "-ready");
        if (before == null) {
            return false;
        }

        manager.cloneWorld(before.getName(), createWorld.getWorldName());
        manager.deleteWorld(before.getName(), false, true);
        manager.removeWorldFromConfig(before.getName());

        return true;
    }

    private World generateWorld(String worldName, Environment env) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(env);

        World world = creator.createWorld();
        return world;
    }

    private boolean isEnableMultiverse() {
        return Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null;
    }

    private Location getTopLocation(Location loc) {
        loc = loc.clone();
        loc.setY(257);

        while (loc.getBlock().getType() == Material.AIR || loc.getBlock().getType() == Material.VOID_AIR) {
            loc.subtract(0, 1, 0);
        }

        loc.add(0.5, 1, 0.5);
        loc.setPitch(0);
        loc.setYaw(0);
        return loc;
    }
}
