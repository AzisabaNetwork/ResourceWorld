package jp.azisaba.main.resourceworld.listeners;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import jp.azisaba.main.resourceworld.RecreateWorld;
import jp.azisaba.main.resourceworld.ResourceWorld;
import jp.azisaba.main.resourceworld.utils.Safety;

public class CreateSafetySpawnListener implements Listener {

	private HashMap<String, RecreateWorld> worldMap = new HashMap<String, RecreateWorld>();

	public CreateSafetySpawnListener(ResourceWorld plugin, List<RecreateWorld> worlds) {
		if (worlds == null) {
			return;
		}

		for (RecreateWorld world : worlds) {
			worldMap.put(world.getWorldName(), world);
		}
	}

	@EventHandler
	public void onChangedWorld(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();
		World w = p.getWorld();

		if (!worldMap.containsKey(w.getName())) {
			return;
		}
		RecreateWorld world = worldMap.get(w.getName());

		Location loc = getSpawnLocation(w);
		Material mat = getCorrectMaterial(w);

		if (world.getProtect() > 0) {
			Safety.createFloor(loc, mat, world.getProtect(), world.getProtect());
			Safety.createSpace(loc, world.getProtect(), 5, world.getProtect());
			Bukkit.getLogger().info("Created.");
		}
	}

	private Location getSpawnLocation(World world) {
		Environment env = world.getEnvironment();
		Location loc = null;
		if (env == Environment.NORMAL) {
			loc = new Location(world, 0.5, 63, 0.5);
		} else if (env == Environment.NETHER) {
			loc = new Location(world, 0.5, 32, 0.5);
		} else if (env == Environment.THE_END) {
			loc = new Location(world, 5, 70, 5);
		}

		return loc;
	}

	private Material getCorrectMaterial(World world) {
		Environment env = world.getEnvironment();
		if (env == Environment.NORMAL) {
			return Material.STONE;
		} else if (env == Environment.NETHER) {
			return Material.NETHERRACK;
		}

		return null;
	}
}
