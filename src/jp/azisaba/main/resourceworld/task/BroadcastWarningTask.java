package jp.azisaba.main.resourceworld.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import jp.azisaba.main.resourceworld.ResourceWorld;

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

	private int getWaitTick() {
		long nextWarn = getNextWarnMilliSecond();
		long now = System.currentTimeMillis();

		double distance = (double) (nextWarn - now);
		double seconds = distance / 1000;

		return (int) (seconds * 20);
	}

	private long getNextWarnMilliSecond() {
		return getNextWarningDate().getTime();
	}

	private Date getNextWarningDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		Calendar calendar = Calendar.getInstance();

		int year, month;
		if (calendar.get(Calendar.DATE) > 15) {

			month = calendar.get(Calendar.MONTH) + 2;
			year = calendar.get(Calendar.YEAR);

			if (month == 13) {
				month = 1;
				year += 1;
			}

			try {
				date = sdf.parse(year + "/" + String.format("%02d", month) + "/01 0:00:00");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			month = calendar.get(Calendar.MONTH) + 1;
			year = calendar.get(Calendar.YEAR);

			try {
				date = sdf.parse(year + "/" + String.format("%02d", month) + "/16 0:00:00");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		calendar.setTime(date);
		calendar.add(Calendar.HOUR, -1);

		int count = 0;
		while (calendar.compareTo(Calendar.getInstance()) < 0) {

			if (count == 0) {
				calendar.add(Calendar.MINUTE, 30);
			} else if (count == 1) {
				calendar.add(Calendar.MINUTE, 15);
			} else if (count <= 3) {
				calendar.add(Calendar.MINUTE, 5);
			} else if (count <= 5) {
				calendar.add(Calendar.MINUTE, 2);
			} else if (count == 6) {
				calendar.add(Calendar.SECOND, 30);
			} else if (count == 7) {
				calendar.add(Calendar.SECOND, 20);
			} else if (count == 8) {
				calendar.add(Calendar.SECOND, 5);
			} else if (count <= 12) {
				calendar.add(Calendar.SECOND, 1);
			} else {
				return null;
			}

			count++;
		}

		return calendar.getTime();
	}

	private BukkitRunnable getTask() {
		return new BukkitRunnable() {
			public void run() {

				if (getNextWarnMilliSecond() - System.currentTimeMillis() > 1000) {
					if (plugin.config.logInConsole) {
						plugin.getLogger()
								.info("次の時刻確認タスクを " + (getWaitTick() / 2) + " tick(s) 後に実行します。");
					}
					task = getTask().runTaskLater(plugin, getWaitTick() / 2);
					return;
				}

				Date next = getNextWarningDate();

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
