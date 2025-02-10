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
        String androidID = UniqueIDHelper.getUniqueID(context);
        UserController.addSession(context, new ControllerCallback<Void, ErrorType>() {
            @Override
            public void onSuccess(Void result) {
                UserController.checkValidSession(androidID, new ControllerCallback<Boolean, ErrorType>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        // Don't put lunch activity here because can make delay.
                    }

                    @Override
                    public void onError(ErrorType error) {
                        // if preferences are invalid with a server database, some stuff when wrong, so invalidate this login.
                        try {
                            // when done if necessary, back to the login page without cause an error.
                            UserController.logout(context);
                        } catch (RuntimeException ignore) {
                        }
                    }
                });
            }

            @Override
            public void onError(ErrorType error) {

            }
        });
    }
}
