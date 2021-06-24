package com.example.woco.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum AppSharedPref {
    instance;

    private SharedPreferences sharedPreferences;

    public void setFirstTime(Context context, String flag) {
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstTime", flag);

        editor.commit();
    }

    public String getFirstTime(Context g8dl_context) {
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(g8dl_context);
        return sharedPreferences.getString("firstTime", "true");
    }
}
