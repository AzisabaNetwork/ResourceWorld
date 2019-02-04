package jp.azisaba.main.resourceworld.utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bukkit.plugin.AuthorNagException;

public class TimeCalculateManager {

	private static List<Integer> warnSecList = Arrays.asList(10, 30, 60, 180, 300, 600, 60 * 30, 60 * 60);

	static {
		Collections.sort(warnSecList);
		Collections.reverse(warnSecList);
	}

	public static long getNextRecreate() {
		return nextRecreateDate().getTime();
	}

	public static long getNextWarn() {
		Date next = nextRecreateDate();

		long warn = -1L;
		for (int i : warnSecList) {

			if (next.getTime() - ((long) i * 1000L) < System.currentTimeMillis()) {
				continue;
			}

			warn = next.getTime() - ((long) i * 1000L);
			break;
		}

		if (warn <= 0) {
			throw new AuthorNagException("Next warn become minus value. (wran=" + warn + ")");
		}

		return warn;
	}

	public static Date nextRecreateDate() {
		Calendar calendar = Calendar.getInstance();

		int attempt = 0;
		while (true) {

			attempt++;

			if (attempt > 100) {
				return null;
			}

			boolean isCorrectWeek = calendar.get(Calendar.WEEK_OF_MONTH) == 1
					|| calendar.get(Calendar.WEEK_OF_MONTH) == 3;
			boolean isSaturday = calendar.get(Calendar.DAY_OF_WEEK) == 7;

			if (isCorrectWeek && isSaturday) {
				break;
			}

			calendar.add(Calendar.DATE, 1);
		}

		return calendar.getTime();
	}
}
