package com.example.exammaster;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.exammaster.network.SessionManager;

import java.util.Calendar;

public class StreakManager {

    private static final String PREF_NAME = "study_streak_prefs";

    private static final String KEY_STREAK_PREFIX = "streak_count_";
    private static final String KEY_LAST_STUDY_DAY_PREFIX = "last_study_day_";

    private StreakManager() {
    }

    public static int getCurrentStreak(Context context, SessionManager sessionManager) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String userKey = getUserKey(sessionManager);

        String streakKey = KEY_STREAK_PREFIX + userKey;
        String lastStudyDayKey = KEY_LAST_STUDY_DAY_PREFIX + userKey;

        int streak = prefs.getInt(streakKey, 0);
        long lastStudyDay = prefs.getLong(lastStudyDayKey, 0L);

        if (lastStudyDay == 0L) {
            return 0;
        }

        long today = getStartOfTodayMillis();
        long yesterday = getStartOfYesterdayMillis();

        /*
         * Если пользователь занимался сегодня — показываем текущий стрик.
         */
        if (lastStudyDay == today) {
            return streak;
        }

        /*
         * Если пользователь занимался вчера, но сегодня ещё не занимался,
         * стрик пока не сбрасываем. День ещё не закончился.
         */
        if (lastStudyDay == yesterday) {
            return streak;
        }

        /*
         * Если последний день занятий был раньше вчера,
         * значит пользователь пропустил день — стрик сбрасываем.
         */
        prefs.edit()
                .putInt(streakKey, 0)
                .putLong(lastStudyDayKey, 0L)
                .apply();

        return 0;
    }

    public static int recordStudyToday(Context context, SessionManager sessionManager) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String userKey = getUserKey(sessionManager);

        String streakKey = KEY_STREAK_PREFIX + userKey;
        String lastStudyDayKey = KEY_LAST_STUDY_DAY_PREFIX + userKey;

        int currentStreak = prefs.getInt(streakKey, 0);
        long lastStudyDay = prefs.getLong(lastStudyDayKey, 0L);

        long today = getStartOfTodayMillis();
        long yesterday = getStartOfYesterdayMillis();

        int newStreak;

        /*
         * Если сегодня уже засчитывали обучение,
         * второй раз за день стрик не увеличиваем.
         */
        if (lastStudyDay == today) {
            return currentStreak;
        }

        /*
         * Если занимался вчера и сегодня снова позанимался,
         * увеличиваем стрик на 1.
         */
        if (lastStudyDay == yesterday) {
            newStreak = currentStreak + 1;
        } else {
            /*
             * Если раньше не занимался или пропустил день,
             * начинаем стрик заново с 1.
             */
            newStreak = 1;
        }

        prefs.edit()
                .putInt(streakKey, newStreak)
                .putLong(lastStudyDayKey, today)
                .apply();

        return newStreak;
    }

    private static String getUserKey(SessionManager sessionManager) {
        if (sessionManager == null) {
            return "guest";
        }

        long userId = sessionManager.getUserId();

        if (userId > 0) {
            return "user_" + userId;
        }

        String email = sessionManager.getEmail();

        if (email != null && !email.trim().isEmpty()) {
            return "email_" + email.trim().toLowerCase();
        }

        return "guest";
    }

    private static long getStartOfTodayMillis() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    private static long getStartOfYesterdayMillis() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_YEAR, -1);

        return calendar.getTimeInMillis();
    }
}