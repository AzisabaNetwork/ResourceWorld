package jp.azisaba.main.resourceworld.task;

import java.util.Calendar;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import jp.azisaba.main.resourceworld.ResourceWorld;
import jp.azisaba.main.resourceworld.utils.TimeCalculateManager;

public class BroadcastWarningTask {

	private ResourceWorld plugin;
	private long last = 0L;
	private boolean skipIfSameAsLast = false;

	private BukkitTask task;

	public BroadcastWarningTask(ResourceWorld plugin) {
		this.plugin = plugin;
	}

	public void runTask() {
		task = getTask().runTaskLater(plugin, 1);
	}

	public void stopTask() {
		if (task != null) {
			task.cancel();
		}
	}

	private BukkitRunnable getTask() {
		return new BukkitRunnable() {
			public void run() {

				long nextWarn = TimeCalculateManager.getNextWarn();
				if (skipIfSameAsLast) {
					if (last == nextWarn) {
						task = getTask().runTaskLater(plugin, 1);
						return;
					} else {
						skipIfSameAsLast = false;
					}
				}

				long waitTicks = getWaitTicks(nextWarn) / 2;
				last = nextWarn;

				if (TimeCalculateManager.getNextWarn() - System.currentTimeMillis() > 1000) {

					if (waitTicks <= 0) {
						waitTicks = 1;
					}

					if (plugin.config.logInConsole) {
						plugin.getLogger()
								.info("次の時刻確認タスクを " + waitTicks + " tick(s) 後に実行します。");
					}
					task = getTask().runTaskLater(plugin, waitTicks);
					return;
				}

				Date next = new Date(TimeCalculateManager.getNextWarn());

				Calendar cal = Calendar.getInstance();
				cal.setTime(next);

				String converted = TimeCalculateManager.convertToWarningStringFromDate(next);

				if (converted == null) {
					task = getTask().runTaskLater(plugin, 20);
					return;
				}

				Bukkit.broadcastMessage(
						plugin.config.chatPrefix + plugin.config.chatWarning.replace("{TIME}", converted));
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
				}

				task = getTask().runTaskLater(plugin, 1);
				skipIfSameAsLast = true;
			}
		};
	}

	private long getWaitTicks(long nextWarn) {
		long now = System.currentTimeMillis();

		double seconds = (double) (nextWarn - now) / 1000;
		return (long) Math.floor(seconds * 20L);
	}
}
