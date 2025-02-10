package com.peppe289.echotrail.utils;

import com.peppe289.echotrail.utils.callback.IPGeolocationCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IPGeolocation {

    /**
     * TOOD: update firebase for support cloud function and add response for get public IP.
     * For now get only "Unknown".
     *
     * @param callback The callback to be invoked upon completion.
     */
    public static void getCountryFromIP(IPGeolocationCallback<String, ErrorType> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // This is only a template for make work this method.
            callback.onSuccess("Unknown");
        });
    }
}

