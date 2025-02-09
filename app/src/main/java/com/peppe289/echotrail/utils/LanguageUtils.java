package com.peppe289.echotrail.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.peppe289.echotrail.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageUtils {
    public static void setLocale(Context context, Locale locale) {
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
        context.createConfigurationContext(config);
    }
}