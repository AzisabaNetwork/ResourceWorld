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

				if (TimeCalculateManager.getNextWarn() - System.currentTimeMillis() > 1000) {
					if (plugin.config.logInConsole) {
						plugin.getLogger()
								.info("次の時刻確認タスクを " + (getWaitTicks() / 2) + " tick(s) 後に実行します。");
					}
					task = getTask().runTaskLater(plugin, getWaitTicks() / 2);
					return;
				}

				Date next = new Date(TimeCalculateManager.getNextWarn());

				Calendar cal = Calendar.getInstance();
				cal.setTime(next);

				if (cal.get(Calendar.SECOND) == 55) {

					new BukkitRunnable() {

						int sec = 5;

						public void run() {
							Bukkit.broadcastMessage(plugin.config.chatPrefix
									+ plugin.config.chatWarning.replace("{TIME}", sec + "秒"));
							for (Player p : Bukkit.getOnlinePlayers()) {
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
							}

							sec--;

							if (sec == 0) {
								this.cancel();

								task = getTask().runTaskLater(plugin, 20);
							}
						}
					}.runTaskTimer(plugin, 0, 20);

					return;
				} else {
					Bukkit.broadcastMessage(plugin.config.chatPrefix
							+ plugin.config.chatWarning.replace("{TIME}", convertToStringFromDate(next)));
					for (Player p : Bukkit.getOnlinePlayers()) {
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
					}
				}

				task = getTask().runTaskLater(plugin, 25);
			}
		};
	}

	private int getWaitTicks() {
		long nextWarn = TimeCalculateManager.getNextWarn();
		long now = System.currentTimeMillis();

		double distance = (double) (nextWarn - now);
		double seconds = distance / 1000;

		return (int) (seconds * 20);
	}

	private String convertToStringFromDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		if (cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) < 1) {
			return "1時間";
		} else if (cal.get(Calendar.MINUTE) == 30) {
			return "30分";
		} else if (cal.get(Calendar.MINUTE) == 45) {
			return "15分";
		} else if (cal.get(Calendar.MINUTE) == 50) {
			return "10分";
		} else if (cal.get(Calendar.MINUTE) == 55) {
			return "5分";
		} else if (cal.get(Calendar.MINUTE) == 57) {
			return "3分";
		} else if (cal.get(Calendar.MINUTE) == 59 && cal.get(Calendar.SECOND) <= 5) {
			return "1分";
		}

		return (60 - cal.get(Calendar.SECOND)) + "秒";
	}
}
