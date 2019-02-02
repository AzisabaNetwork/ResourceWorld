package jp.azisaba.main.resourceworld;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import jp.azisaba.main.resourceworld.command.ResourceWorldCommand;
import jp.azisaba.main.resourceworld.listeners.ProtectSpawnListener;
import jp.azisaba.main.resourceworld.task.BroadcastWarningTask;
import jp.azisaba.main.resourceworld.task.ResourceWorldCreateTask;

public class ResourceWorld extends JavaPlugin {

	private final String PLUGIN_NAME = "ResourceWorld";

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

		Bukkit.getPluginCommand("resourceworld").setExecutor(new ResourceWorldCommand(this));

		Bukkit.getPluginManager().registerEvents(new ProtectSpawnListener(this, config.createWorldList), this);

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

			return generateWithMultiverse(createWorld);

		} else {

			return generateNormally(createWorld);
		}
	}

//	public boolean connectPortal(String portalName, String worldName) {
//
//		if (!isEnablePortalPlugin()) {
//			getLogger().warning("AdvancedPortals Pluginがロードされていないためポータルが接続できませんでした。");
//			return false;
//		}
//
//		AdvancedPortalsPlugin ap = (AdvancedPortalsPlugin) Bukkit.getPluginManager().getPlugin("AdvancedPortals");
//
//		MultiversePortals mvp = (MultiversePortals) Bukkit.getPluginManager().getPlugin("Multiverse-Portals");
//		MVPortal portal = mvp.getPortalManager().getPortal(portalName);
//
//		if (portal == null) {
//			getLogger().warning(portalName + " という名前のポータルがみつかりません。");
//			return false;
//		}
//
//		boolean success = portal.setProperty("dest", "w:" + worldName);
//
//		if (!success) {
//			getLogger().warning("ポータルの接続に失敗。");
//		} else {
//			getLogger().info("ポータルの接続に成功。");
//		}
//		return success;
//	}

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
				FileUtils.deleteDirectory(world.getWorldFolder());
			} catch (IOException e) {
				this.getLogger().warning(createWorld.getWorldName() + "のunloadに失敗。");
				e.printStackTrace();
				return false;
			}
		}

		World newWorld = generateWorld(createWorld.getWorldName(), createWorld.getEnvironment());

		newWorld.getWorldBorder().setSize(createWorld.getWorldBorderSize());
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
		spawn.setX(0);
		spawn.setZ(0);

		Location loc = getTopLocation(spawn);

		if (createWorld.getEnvironment() == Environment.NORMAL) {
			mvWorld.setSpawnLocation(loc);
			mvWorld.getCBWorld().setSpawnLocation(loc);

			createFloor(loc, Material.STONE);
			createSpace(loc);
		} else if (createWorld.getEnvironment() == Environment.NETHER) {
			loc = mvWorld.getSpawnLocation();

			loc.setY(0);

			while (loc.getBlock().getType() != Material.AIR) {
				loc.add(0, 1, 0);
			}

			if (loc.getY() >= 128) {
				loc.setY(32);
			}

			mvWorld.setSpawnLocation(loc);

			createFloor(loc, Material.NETHERRACK);
			createSpace(loc);
		} else if (createWorld.getEnvironment() == Environment.THE_END) {

			Location check = new Location(world, 5, 70, 5);
			mvWorld.setSpawnLocation(getTopLocation(check));
		}
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

//	private boolean isEnablePortalPlugin() {
//		return Bukkit.getPluginManager().getPlugin("AdvancedPortals") != null;
//	}

	private Location getTopLocation(Location loc) {
		loc = loc.clone();
		loc.setY(257);

		while (loc.getBlock().getType() == Material.AIR) {
			loc.setY(loc.getY() - 1);
		}

		loc.add(0, 1, 0);
		return loc;
	}

	private void createFloor(Location loc, Material material) {
		Location floor1 = loc.clone();
		floor1.subtract(1, 1, 1);

		Location floor2 = loc.clone();
		floor2.add(1, 0, 1);
		floor2.subtract(0, 1, 0);

		for (int x = floor1.getBlockX(); x <= floor2.getBlockX(); x++) {
			for (int y = floor1.getBlockY(); y <= floor2.getBlockY(); y++) {
				for (int z = floor1.getBlockZ(); z <= floor2.getBlockZ(); z++) {

					Location l = new Location(loc.getWorld(), x, y, z);

					l.getBlock().setType(material);
				}
			}
		}
	}

	private void createSpace(Location loc) {
		Location pos1 = loc.clone();
		pos1.subtract(1, 0, 1);

		Location pos2 = loc.clone();
		pos2.add(1, 2, 1);

		for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
			for (int y = pos1.getBlockY(); y <= pos2.getBlockY(); y++) {
				for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {

					Location l = new Location(loc.getWorld(), x, y, z);
					l.getBlock().setType(Material.AIR);
				}
			}
		}
	}
}
