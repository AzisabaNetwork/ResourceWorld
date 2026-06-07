package jp.azisaba.main.resourceworld;

import jp.azisaba.main.resourceworld.command.ResourceWorldCommand;
import jp.azisaba.main.resourceworld.listeners.ProtectSpawnListener;
import jp.azisaba.main.resourceworld.task.BroadcastWarningTask;
import jp.azisaba.main.resourceworld.task.ResourceWorldCreateTask;
import jp.azisaba.main.resourceworld.task.SpawnPointTaskManager;
import jp.azisaba.main.resourceworld.utils.Safety;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.utils.result.FailureReason;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.CloneWorldOptions;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.core.world.options.LoadWorldOptions;
import org.mvplugins.multiverse.core.world.options.RegenWorldOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ArrayList;

public class ResourceWorld extends JavaPlugin {

    public ResourceWorldConfig config;

    private ResourceWorldCreateTask createTask;
    private BroadcastWarningTask warningTask;

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

        Bukkit.getLogger().info(getName() + " enabled.");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(getName() + " disabled.");
    }

    private static void delete(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(currentPath -> {
                try {
                    Files.delete(currentPath);
                } catch (IOException e) {
                    throw new WorldDeleteException(e);
                }
            });
        } catch (WorldDeleteException e) {
            throw e.getCause();
        }
    }

    public boolean recreateResourceWorld(RecreateWorld createWorld) {
        if (this.config.useMultiverse) {

            if (!isEnableMultiverse()) {
                getLogger().warning("MultiverseCore Pluginがロードされていません。通常の方法で生成します。");
                return generateNormally(createWorld);
            }

            WorldManager manager = getMultiverseWorldManager();

            MultiverseWorld world = manager.getWorld(createWorld.getWorldName() + "-ready").getOrNull();
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
        Path worldFolder = new File(Bukkit.getWorldContainer(), createWorld.getWorldName()).toPath();

        if (world != null) {
            if (!evacuatePlayers(world)) {
                return false;
            }

            worldFolder = world.getWorldFolder().toPath();
            if (!Bukkit.unloadWorld(world, false)) {
                getLogger().warning(createWorld.getWorldName()
                        + "をアンロードできなかったため、ワールドフォルダの削除を中止しました。");
                return false;
            }
        }

        try {
            delete(worldFolder);
        } catch (IOException e) {
            getLogger().log(java.util.logging.Level.SEVERE,
                    createWorld.getWorldName() + "のワールドフォルダを削除できませんでした。", e);
            return false;
        }

        World newWorld = generateWorld(createWorld.getWorldName(), createWorld.getEnvironment());
        if (newWorld == null) {
            getLogger().warning(createWorld.getWorldName() + "の生成に失敗しました。");
            return false;
        }

        newWorld.getWorldBorder().setSize(createWorld.getWorldBorderSize());
        return saveGeneratedWorld(newWorld);
    }

    private boolean generateWithMultiverse(RecreateWorld createWorld) {

        WorldManager manager = getMultiverseWorldManager();

        MultiverseWorld existingWorld = manager.getWorld(createWorld.getWorldName()).getOrNull();
        Attempt<LoadedMultiverseWorld, ?> generationResult;
        if (existingWorld == null) {
            generationResult = manager.createWorld(
                    CreateWorldOptions.worldName(createWorld.getWorldName())
                            .environment(createWorld.getEnvironment())
                            .worldType(WorldType.NORMAL)
                            .generateStructures(true));
        } else {
            LoadedMultiverseWorld loadedWorld = manager.getLoadedWorld(existingWorld).getOrNull();
            if (loadedWorld == null) {
                Attempt<LoadedMultiverseWorld, ?> loadResult =
                        manager.loadWorld(LoadWorldOptions.world(existingWorld));
                if (!checkMultiverseResult(loadResult, "再生成対象ワールドのロード")) {
                    return false;
                }
                loadedWorld = loadResult.get();
            }

            generationResult = manager.regenWorld(
                    RegenWorldOptions.world(loadedWorld)
                            .randomSeed(true)
                            .keepGameRule(true)
                            .keepWorldBorder(true)
                            .keepWorldConfig(true));
        }

        if (!checkMultiverseResult(generationResult, "ワールドの再生成")) {
            return false;
        }

        LoadedMultiverseWorld mvWorld = generationResult.get();

        World world = Bukkit.getWorld(createWorld.getWorldName());
        if (world == null) {
            getLogger().warning("生成したワールドをBukkitから取得できませんでした。");
            return false;
        }
        world.getWorldBorder().setSize(createWorld.getWorldBorderSize());
        world.getWorldBorder().setCenter(mvWorld.getSpawnLocation());

        Location spawn = mvWorld.getSpawnLocation();
        spawn.setX(0.5);
        spawn.setZ(0.5);
        spawn.setPitch(0);
        spawn.setYaw(0);
        Location loc = getTopLocation(spawn);

        mvWorld.setAdjustSpawn(false);

        if (createWorld.getEnvironment() == Environment.NORMAL) {
            loc.setY(63);
            mvWorld.setSpawnLocation(loc);

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

        if (!saveGeneratedWorld(world)) {
            return false;
        }

        return checkMultiverseConfigSave(manager);
    }

    private boolean moveWolrdWithMultiverse(RecreateWorld createWorld) {
        WorldManager manager = getMultiverseWorldManager();

        LoadedMultiverseWorld before = manager.getLoadedWorld(createWorld.getWorldName() + "-ready").getOrNull();
        if (before == null) {
            return false;
        }

        Attempt<LoadedMultiverseWorld, ?> cloneResult =
                manager.cloneWorld(CloneWorldOptions.fromTo(before, createWorld.getWorldName()));
        if (!checkMultiverseResult(cloneResult, "ワールドの複製")) {
            return false;
        }

        World clonedWorld = Bukkit.getWorld(createWorld.getWorldName());
        if (clonedWorld == null || !saveGeneratedWorld(clonedWorld)) {
            getLogger().warning("複製先ワールドの保存を確認できなかったため、複製元ワールドを保持します。");
            return false;
        }

        if (!checkMultiverseResult(
                manager.deleteWorld(DeleteWorldOptions.world(before)),
                "複製元ワールドの削除")) {
            return false;
        }

        return checkMultiverseConfigSave(manager);
    }

    private World generateWorld(String worldName, Environment env) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(env);

        World world = creator.createWorld();
        return world;
    }

    private boolean isEnableMultiverse() {
        return Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core") && MultiverseCoreApi.isLoaded();
    }

    private WorldManager getMultiverseWorldManager() {
        return MultiverseCoreApi.get().getWorldManager();
    }

    private <T, F extends FailureReason> boolean checkMultiverseResult(Attempt<T, F> result, String operation) {
        if (result.isSuccess()) {
            return true;
        }

        getLogger().warning(operation + "に失敗しました: " + result.getFailureMessage());
        return false;
    }

    private boolean checkMultiverseConfigSave(WorldManager manager) {
        var result = manager.saveWorldsConfig();
        if (result.isSuccess()) {
            return true;
        }

        getLogger().log(java.util.logging.Level.SEVERE, "Multiverse-Coreのワールド設定を保存できませんでした。",
                result.getCause());
        return false;
    }

    private boolean saveGeneratedWorld(World world) {
        File worldFolder = world.getWorldFolder();
        try {
            Files.createDirectories(worldFolder.toPath());
            world.getChunkAt(world.getSpawnLocation()).load(true);
            world.save();
        } catch (IOException | RuntimeException e) {
            getLogger().log(java.util.logging.Level.SEVERE, world.getName() + "の保存に失敗しました。", e);
            return false;
        }

        if (!worldFolder.isDirectory()) {
            getLogger().severe(world.getName() + "のワールドフォルダが作成されませんでした: "
                    + worldFolder.getAbsolutePath());
            return false;
        }

        return true;
    }

    private boolean evacuatePlayers(World world) {
        if (world.getPlayers().isEmpty()) {
            return true;
        }

        World destinationWorld = Bukkit.getWorlds().stream()
                .filter(candidate -> !candidate.equals(world))
                .findFirst()
                .orElse(null);
        if (destinationWorld == null) {
            getLogger().warning(world.getName() + "からプレイヤーを退避できるワールドがありません。");
            return false;
        }

        Location destination = destinationWorld.getSpawnLocation();
        for (Player player : new ArrayList<>(world.getPlayers())) {
            if (!player.teleport(destination)) {
                getLogger().warning(player.getName() + "を" + world.getName() + "から退避できませんでした。");
                return false;
            }
        }

        return true;
    }

    private Location getTopLocation(Location loc) {
        loc = loc.clone();
        loc.setY(257);

        while (loc.getBlock().getType() == Material.AIR || loc.getBlock().getType() == Material.VOID_AIR) {
            loc.subtract(0, 1, 0);
        }

        loc.add(0, 1, 0);
        return loc;
    }

    private static final class WorldDeleteException extends RuntimeException {
        private WorldDeleteException(IOException cause) {
            super(cause);
        }

        @Override
        public synchronized IOException getCause() {
            return (IOException) super.getCause();
        }
    }
}
