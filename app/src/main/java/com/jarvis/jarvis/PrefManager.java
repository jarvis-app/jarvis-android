package com.jarvis.jarvis;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by subhgupt on 22/03/15.
 */
public class PrefManager {

    public static boolean langEnglish(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isEnglish = prefs.getBoolean("EnglishLang", true);
        return isEnglish;
    }

    public static void setLangEnglish(Context context, boolean enabled)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean("EnglishLang", enabled).commit();
    }
}

