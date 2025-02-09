package com.peppe289.echotrail.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.user.PreferencesController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageUtils {
    public static Context setAppLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        PreferencesController.setLanguages(languageCode);

        return context.createConfigurationContext(configuration);
    }

    public static Context setAppLanguage(Context context) {
        Locale locale;

        String languageCode = PreferencesController.getLanguages();

        locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        PreferencesController.setLanguages(languageCode);

        return context.createConfigurationContext(configuration);
    }

    public static List<Locale> getAvailableLocales() {
        Locale[] testLocales = {
                Locale.ENGLISH, Locale.ITALIAN
        };

        return List.of(testLocales);
    }
}