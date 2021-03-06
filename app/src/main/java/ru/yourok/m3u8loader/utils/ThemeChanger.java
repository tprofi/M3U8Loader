package ru.yourok.m3u8loader.utils;

import android.app.Activity;

import ru.yourok.loader.Options;
import ru.yourok.m3u8loader.R;

/**
 * Created by yourok on 23.12.16.
 */

public class ThemeChanger {
    public static void SetTheme(Activity activity) {
        switch (Options.getInstance(activity).GetTheme()) {
            case 0:
                SetLightTheme(activity);
                return;
            case 1:
                SetDarkTheme(activity);
                return;
        }
    }

    public static void SetDarkTheme(Activity activity) {
        activity.setTheme(R.style.AppThemeDark);
    }

    public static void SetLightTheme(Activity activity) {
        activity.setTheme(R.style.AppTheme);
    }
}
