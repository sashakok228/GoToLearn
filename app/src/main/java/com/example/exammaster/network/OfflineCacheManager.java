package com.example.exammaster.network;

import android.content.Context;
import android.content.SharedPreferences;

public class OfflineCacheManager {

    private static final String PREF_NAME = "offline_cache";

    private static final String KEY_SUBJECTS_JSON = "subjects_json";
    private static final String KEY_TICKETS_PREFIX = "tickets_subject_";

    private final SharedPreferences prefs;

    public OfflineCacheManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSubjectsJson(String json) {
        prefs.edit()
                .putString(KEY_SUBJECTS_JSON, json)
                .apply();
    }

    public String getSubjectsJson() {
        return prefs.getString(KEY_SUBJECTS_JSON, null);
    }

    public void saveTicketsJson(long subjectId, String json) {
        prefs.edit()
                .putString(KEY_TICKETS_PREFIX + subjectId, json)
                .apply();
    }

    public String getTicketsJson(long subjectId) {
        return prefs.getString(KEY_TICKETS_PREFIX + subjectId, null);
    }

    public boolean hasSubjectsCache() {
        String json = getSubjectsJson();
        return json != null && !json.trim().isEmpty();
    }

    public boolean hasTicketsCache(long subjectId) {
        String json = getTicketsJson(subjectId);
        return json != null && !json.trim().isEmpty();
    }

    public void clearAllCache() {
        prefs.edit().clear().apply();
    }
}