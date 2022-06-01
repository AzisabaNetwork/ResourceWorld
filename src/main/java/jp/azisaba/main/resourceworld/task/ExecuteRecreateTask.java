package jp.azisaba.main.resourceworld.task;

import java.util.ArrayDeque;
import jp.azisaba.main.resourceworld.RecreateWorld;
import jp.azisaba.main.resourceworld.ResourceWorld;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public class ExecuteRecreateTask {

  private final ResourceWorld plugin;

  private final ArrayDeque<RecreateWorld> worlds = new ArrayDeque<>();

  public ExecuteRecreateTask(ResourceWorld plugin) {
    this.plugin = plugin;
  }

  public void add(RecreateWorld world) {
    worlds.add(world);
  }

  public void run() {
    schedule(0);
  }

  private void schedule(long delay) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      RecreateWorld world = worlds.poll();
      if (world == null) {
        return;
      }

      Bukkit.broadcastMessage(
          ChatColor.YELLOW + "[" + ChatColor.GREEN + "再生成システム" + ChatColor.YELLOW + "] "
              + ChatColor.RED + world.getWorldName() + ChatColor.GREEN + " を再生成しています...");

      boolean b = plugin.recreateResourceWorld(world);

      if (b && plugin.config.logInConsole) {
        plugin.getLogger().info(world.getWorldName() + "の生成に成功。");
      }

      Bukkit.broadcastMessage(
          ChatColor.YELLOW + "[" + ChatColor.GREEN + "再生成システム" + ChatColor.YELLOW + "] "
              + ChatColor.RED + world.getWorldName() + ChatColor.GREEN + " の再生成に成功！");

      if (!worlds.isEmpty()) {
        schedule(20L * 10L);
      }
    }, delay);
  }
}
