package jp.azisaba.main.resourceworld.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import jp.azisaba.main.resourceworld.ProtectManager;
import jp.azisaba.main.resourceworld.ResourceWorld;
import net.md_5.bungee.api.ChatColor;

public class ResourceWorldCommand implements CommandExecutor {

	private ResourceWorld plugin;

	public ResourceWorldCommand(ResourceWorld plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage().replace("{LABEL}", label));
			return true;
		}

		if (args[0].equalsIgnoreCase("recreate")) {
			plugin.recreateAllResourceWorlds();
			sender.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GREEN + "再生成システム" + ChatColor.YELLOW + "] "
					+ ChatColor.GREEN + "再生成が完了しました！");
		}

		if (args[0].equalsIgnoreCase("protect")) {
			boolean b = !ProtectManager.get();
			ProtectManager.set(b);

			sender.sendMessage(ChatColor.GREEN + "資源の中心保護を" + ChatColor.YELLOW + b + ChatColor.GREEN + "にしました。");
		}
		return true;
	}
}
