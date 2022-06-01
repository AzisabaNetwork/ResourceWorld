package jp.azisaba.main.resourceworld.command;

import jp.azisaba.main.resourceworld.ProtectManager;
import jp.azisaba.main.resourceworld.RecreateWorld;
import jp.azisaba.main.resourceworld.ResourceWorld;
import jp.azisaba.main.resourceworld.task.ExecuteRecreateTask;
import jp.azisaba.main.resourceworld.task.SpawnPointTaskManager;
import jp.azisaba.main.resourceworld.utils.TimeCalculateManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.Date;

public class ResourceWorldCommand implements CommandExecutor {

    private static String prefix = ChatColor.YELLOW + "[" + ChatColor.GREEN + "再生成システム" + ChatColor.YELLOW + "] ";
    private ResourceWorld plugin;

    public ResourceWorldCommand(ResourceWorld plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, cmd, label);
            return true;
        }

        if (args[0].equalsIgnoreCase("recreate") && sender.hasPermission("resourceworld.command.resourceworld.admin")) {
            recreateWorlds();
            return true;
        }

        if (args[0].equalsIgnoreCase("protect") && sender.hasPermission("resourceworld.command.resourceworld.admin")) {
            boolean b = !ProtectManager.get();
            ProtectManager.set(b);

            sender.sendMessage(
                    prefix + ChatColor.GREEN + "資源の中心保護を" + ChatColor.YELLOW + b + ChatColor.GREEN + "にしました。");
            return true;
        }

        if (args[0].equalsIgnoreCase("seichi") && sender.hasPermission("resourceworld.command.resourceworld.admin")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Consoleはサポートしていません");
                return true;
            }

            Player p = (Player) sender;

            if (!p.getWorld().getName().equalsIgnoreCase("resource")) {
                p.sendMessage(ChatColor.RED + "このワールドでは実行できません！");
                return true;
            }

            SpawnPointTaskManager.runTask(p, p.getWorld());
            return true;
        }

        if (args[0].equalsIgnoreCase("next")) {
            Date next = TimeCalculateManager.nextRecreateDate();

            if (next == null) {
                sender.sendMessage(prefix + ChatColor.RED + "取得に失敗しました。運営に問い合わせてください。");
                return true;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(next);

            String dateStr = cal.get(Calendar.YEAR) + "年" + (cal.get(Calendar.MONTH) + 1) + "月" + cal.get(Calendar.DATE)
                    + "日 " + cal.get(Calendar.HOUR_OF_DAY) + "時";

            sender.sendMessage(
                    prefix + ChatColor.GREEN + "次の再生成は " + ChatColor.RED + dateStr + ChatColor.GREEN + " です。");
            return true;
        }

        sendUsage(sender, cmd, label);
        return true;
    }

    private void sendUsage(CommandSender sender, Command cmd, String label) {
        if (sender.hasPermission("resourceworld.command.resourceworld.admin")) {
            sender.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage().replace("{LABEL}", label));
        } else {
            sender.sendMessage(ChatColor.RED + "使い方: /" + label + " next");
        }
    }

    private void recreateWorlds() {
        ExecuteRecreateTask recreateTask = new ExecuteRecreateTask(plugin);
        plugin.config.createWorldList.forEach(recreateTask::add);
        recreateTask.run();
    }
}
