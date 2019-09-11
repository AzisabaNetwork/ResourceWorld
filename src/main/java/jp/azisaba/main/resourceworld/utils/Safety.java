package jp.azisaba.main.resourceworld.utils;

import org.bukkit.Location;
import org.bukkit.Material;

public class Safety {

	public static void createFloor(Location loc, Material material, int x1, int z1) {
		Location floor1 = loc.clone();
		floor1.subtract(x1, 1, z1);

		Location floor2 = loc.clone();
		floor2.add(x1, -1, z1);

		for (int x = floor1.getBlockX(); x <= floor2.getBlockX(); x++) {
			for (int y = floor1.getBlockY(); y <= floor2.getBlockY(); y++) {
				for (int z = floor1.getBlockZ(); z <= floor2.getBlockZ(); z++) {

					Location l = new Location(loc.getWorld(), x, y, z);

					if (l.getBlock().getType() != material)
						l.getBlock().setType(material);
				}
			}
		}
	}

	public static void createSpace(Location loc, int x1, int y1, int z1) {
		Location pos1 = loc.clone();
		pos1.subtract(x1, 0, z1);

		Location pos2 = loc.clone();
		pos2.add(x1, y1, z1);

		for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
			for (int y = pos1.getBlockY(); y <= pos2.getBlockY(); y++) {
				for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {

					Location l = new Location(loc.getWorld(), x, y, z);

					if (l.getBlock().getType() != Material.AIR && l.getBlock().getType() != Material.VOID_AIR)
						l.getBlock().setType(Material.AIR);
				}
			}
		}
	}
}
