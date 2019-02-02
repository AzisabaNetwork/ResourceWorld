package jp.azisaba.main.resourceworld.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

	private static Date nextRecreateDate() {
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

		return date;
	}
}
