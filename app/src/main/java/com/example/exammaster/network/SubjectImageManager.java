package com.example.exammaster.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SubjectImageManager {

    private static final String PREF_NAME = "subject_images";
    private static final String KEY_PREFIX = "subject_image_";

    private final SharedPreferences prefs;

    public SubjectImageManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSubjectImage(long subjectId, String imageUri) {
        if (subjectId <= 0 || imageUri == null || imageUri.trim().isEmpty()) {
            return;
        }

        prefs.edit()
                .putString(KEY_PREFIX + subjectId, imageUri)
                .apply();
    }

    public String getSubjectImage(long subjectId) {
        if (subjectId <= 0) {
            return null;
        }

        return prefs.getString(KEY_PREFIX + subjectId, null);
    }

    public void deleteSubjectImage(long subjectId) {
        if (subjectId <= 0) {
            return;
        }

        prefs.edit()
                .remove(KEY_PREFIX + subjectId)
                .apply();
    }
}