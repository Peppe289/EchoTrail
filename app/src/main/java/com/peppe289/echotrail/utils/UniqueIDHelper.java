package com.peppe289.echotrail.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;

import java.util.UUID;

public class UniqueIDHelper {
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    public static String getUniqueID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE);
        String uniqueID = prefs.getString(PREF_UNIQUE_ID, null);
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            prefs.edit().putString(PREF_UNIQUE_ID, uniqueID).apply();
        }
        return uniqueID;
    }

    public static String getUUID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE);
        return prefs.getString(PREF_UNIQUE_ID, null);
    }

    public static void addSessionAtLogin(Context context) {
        UserController.addSession(context, new ControllerCallback<Void, ErrorType>() {
            @Override
            public void onSuccess(Void result) {

            }

            @Override
            public void onError(ErrorType error) {

            }
        });
    }
}
