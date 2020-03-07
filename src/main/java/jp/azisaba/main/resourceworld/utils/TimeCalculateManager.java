package jp.azisaba.main.resourceworld.utils;

import org.bukkit.Bukkit;

import java.util.*;

public class TimeCalculateManager {

    private static List<Integer> warnSecList = Arrays.asList(1, 2, 3, 4, 5, 10, 30, 60, 180, 300, 600, 60 * 30,
            60 * 60);

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
            Bukkit.getLogger().warning("Next warn become minus value. (wran=" + warn + ") set value to 0.");
            warn = 0;
        }

        return warn;
    }

    public static Date nextRecreateDate() {
        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.HOUR_OF_DAY) >= 21) {
            calendar.add(Calendar.DATE, 1);
        }

        int attempt = 0;
        while (true) {

            attempt++;

            if (attempt > 100) {
                return null;
            }

            int week = calendar.get(Calendar.WEEK_OF_MONTH);
            boolean isCorrectWeek = week == 1 || week == 3;
            boolean isSaturday = calendar.get(Calendar.DAY_OF_WEEK) == 7;

            if (isCorrectWeek && isSaturday) {
                break;
            }

            calendar.add(Calendar.DATE, 1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static String convertToWarningStringFromDate(Date date) {
        Calendar nextWarn = Calendar.getInstance();
        nextWarn.setTime(date);

        long nextRecreate = getNextRecreate();

        int attempt = 0;
        for (int i : warnSecList) {
            attempt++;
            if (nextRecreate - ((long) i * 1000L) == date.getTime()) {
                return convertToStringFromAttempt(attempt);
            }
        }

        return null;
    }

    private static String convertToStringFromAttempt(int i) {
        long l = warnSecList.get(i - 1);

        if (l < 60) {
            return l + "秒";
        } else if (l < 3600) {
            return (l / 60) + "分";
        } else {
            return (l / 3600) + "時間";
        }
    }
}
