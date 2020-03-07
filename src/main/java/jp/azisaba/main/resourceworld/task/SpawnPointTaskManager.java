package jp.azisaba.main.resourceworld.task;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import jp.azisaba.main.resourceworld.ResourceWorld;
import jp.azisaba.main.resourceworld.utils.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                    Material type = getTypeFromLocation(loc);

                    if (loc.getBlock().getType() == Material.VOID_AIR && type == Material.AIR) {
                        continue;
                    }

                    if (loc.getBlock().getType() != type) {
                        locList.add(loc);
                    }
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
                    Material type = getTypeFromLocation(loc);

                    if (type != null) {
                        loc.getBlock().setType(type);
                    }
                }

                blocks = blocks.subList(processed, blocks.size());

                if (player != null) {
                    double percentage;

                    if (blockCount != 0) {
                        percentage = ((double) (blockCount - blocks.size()) / (double) blockCount) * 100d;
                    } else {
                        percentage = 100;
                    }

                    int ceil = (int) Math.ceil(percentage);

                    String meter = ChatColor.GREEN + Strings.repeat("｜", ceil) + ChatColor.WHITE
                            + Strings.repeat("｜", 100 - ceil);
                    JSONMessage
                            .create(meter + ChatColor.YELLOW + " / " + ChatColor.GREEN + ""
                                    + String.format("%.2f", percentage)
                                    + "%")
                            .actionbar(player);
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    private static Material getTypeFromLocation(Location loc) {
        if (loc.getX() == 0 && loc.getY() == 62 && loc.getZ() == 0) {
            return Material.OBSIDIAN;
        } else if (61 <= loc.getY() && loc.getY() <= 62) {
            return Material.STONE;
        } else if (loc.getY() == 10 && (loc.getZ() >= -6 && loc.getZ() <= -4)
                && (loc.getX() >= -6 && loc.getX() <= -4)) {
            return Material.DIAMOND_BLOCK;
        } else if (loc.getY() == 10 && Math.abs(loc.getX()) % 5 == 0 && Math.abs(loc.getZ()) % 5 == 0) {
            return Material.SEA_LANTERN;
        } else if ((loc.getY() >= 17 && loc.getY() <= 60
                && (Math.abs(loc.getX()) == 25 || Math.abs(loc.getZ()) == 25)) || loc.getY() == 10) {
            return Material.GLASS;
        } else if (loc.getY() >= 15 && loc.getY() <= 60 && loc.getX() >= -7 && loc.getX() <= -3 && loc.getZ() >= -7
                && loc.getZ() <= -3 && !((loc.getZ() >= -6 && loc.getZ() <= -4)
                && (loc.getX() >= -6 && loc.getX() <= -4))) {
            return Material.GLASS;
        } else if (Math.abs(loc.getX()) <= 25 && Math.abs(loc.getZ()) <= 25) {
            return Material.AIR;
        }

        return null;
    }

    private static void sort(List<Location> locList) {
        Collections.sort(locList, (loc1, loc2) -> ComparisonChain.start()
                .compare(loc1.getX(), loc2.getX())
                .compare(loc1.getZ(), loc2.getZ())
                .compare(loc1.getY(), loc2.getY())
                .result());
    }
}
