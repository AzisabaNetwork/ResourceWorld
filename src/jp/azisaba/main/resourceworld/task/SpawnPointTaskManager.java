package jp.azisaba.main.resourceworld.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ComparisonChain;

import jp.azisaba.main.resourceworld.ResourceWorld;
import jp.azisaba.main.resourceworld.utils.JSONMessage;
import net.md_5.bungee.api.ChatColor;

public class SpawnPointTaskManager {

	private static ResourceWorld plugin;
	private static BukkitTask task = null;

	public static void init(ResourceWorld plugin) {
		SpawnPointTaskManager.plugin = plugin;
	}

	public static void cancelTask() {
		if (task != null) {
			task.cancel();
		}
	}

	public static void runTask(Player p, World world) {

		if (task != null) {
			return;
		}

		if (p != null)
			p.sendMessage(ChatColor.GREEN + "ブロックを取得しています...");

		List<Location> locList = new ArrayList<Location>();

		for (int x = -25; x <= 25; x++) {
			for (int y = 10; y <= 256; y++) {
				for (int z = -25; z <= 25; z++) {

					Location loc = new Location(world, x, y, z);

					if ((61 <= loc.getY() && loc.getY() <= 62)) {

						if (loc.getBlock().getType() == Material.STONE) {
							continue;
						}

						locList.add(loc);
						continue;
					}

					if ((loc.getY() >= 17 && loc.getY() <= 60
							&& (Math.abs(loc.getX()) == 25 || Math.abs(loc.getZ()) == 25)) || loc.getY() == 10) {

						if (loc.getBlock().getType() == Material.GLASS) {
							continue;
						}

						locList.add(loc);
						continue;
					}

					if (loc.getBlock().getType() == Material.AIR || loc.getBlock().getType() == Material.VOID_AIR) {
						continue;
					}

					locList.add(loc);
				}
			}
		}

		sort(locList);
		Collections.reverse(locList);

		if (p != null)
			p.sendMessage(ChatColor.GREEN + "破壊しています...");

		task = new BukkitRunnable() {

			private Player player = p;
			private int blockCount = locList.size();
			private List<Location> blocks = locList;

			@Override
			public void run() {

				if (blocks.size() <= 0) {
					if (player != null) {
						player.sendMessage(ChatColor.GREEN + "完了！");
						JSONMessage.create(ChatColor.GREEN + "完了！").actionbar(player);
					}
					this.cancel();

					task = null;
				}

				int processed = -1;
				long start = System.currentTimeMillis();

				while (start + 100 > System.currentTimeMillis()) {
					processed++;

					if (processed >= blocks.size()) {
						break;
					}

					Location loc = blocks.get(processed);

					if (loc.getY() == 10 || (loc.getY() >= 17 && loc.getY() <= 60
							&& (Math.abs(loc.getX()) == 25 || Math.abs(loc.getZ()) == 25))) {
						loc.getBlock().setType(Material.GLASS);
						continue;
					}

					if (61 <= loc.getY() && loc.getY() <= 62) {
						loc.getBlock().setType(Material.STONE);
						continue;
					}

					loc.getBlock().setType(Material.AIR);
				}

				blocks = blocks.subList(processed, blocks.size());

				if (player != null) {
					double percentage = ((double) (blockCount - blocks.size()) / (double) blockCount) * 100d;
					int ceil = (int) Math.ceil(percentage);

					String meter = ChatColor.GREEN + StringUtils.repeat("｜", ceil) + ChatColor.WHITE
							+ StringUtils.repeat("｜", 100 - ceil);
					JSONMessage
							.create(meter + ChatColor.YELLOW + " / " + ChatColor.GREEN + ""
									+ String.format("%.2f", percentage)
									+ "%")
							.actionbar(player);
				}
			}
		}.runTaskTimer(plugin, 0, 5);
	}

	private static void sort(List<Location> locList) {
		Collections.sort(locList, (loc1, loc2) -> ComparisonChain.start()
				.compare(loc1.getX(), loc2.getX())
				.compare(loc1.getZ(), loc2.getZ())
				.compare(loc1.getY(), loc2.getY())
				.result());
	}
}
