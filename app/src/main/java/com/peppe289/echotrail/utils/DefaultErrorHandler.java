package com.peppe289.echotrail.utils;
import android.content.Context;
import android.widget.Toast;
import androidx.annotation.Nullable;

public class DefaultErrorHandler {
    private static DefaultErrorHandler instance;
    private final Context context;

    private DefaultErrorHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * After the first call, this method will return the same instance of the class.
     * For this, we can consider context null after first time.
     * <p>
     *     In case of bad usage (context and instance are null), the method will return
     *     null and exception is generated from Toast or other methods.
     * </p>
     *
     * @param context application context
     * @return the instance of the class
     */
    public static synchronized DefaultErrorHandler getInstance(@Nullable Context context) {
        if (instance == null && context != null) {
            instance = new DefaultErrorHandler(context);
        }
        return instance;
    }

    public void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showError(ErrorType error) {
        if (error != null) {
            showError(error.getMessage(context));
        } else {
            showError(ErrorType.UNKNOWN_ERROR.getMessage(context));
        }
    }
}