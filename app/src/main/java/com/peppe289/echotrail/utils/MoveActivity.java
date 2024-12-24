package com.peppe289.echotrail.utils;

import android.content.Context;
import android.content.Intent;

/**
 * The {@code MoveActivity} utility class provides reusable methods to manage activity navigation.
 * It simplifies the process of starting new activities with optional stack management,
 * such as clearing the back stack or preserving the current task.
 */
@SuppressWarnings("unused")
public class MoveActivity {

    /**
     * Starts a new activity without modifying the current activity stack.
     * <p>
     * This method is used when you want to add a new activity on top of the existing task stack.
     * </p>
     *
     * @param activity    The context of the current activity.
     * @param destination The class of the activity to start.
     */
    public static void addActivity(Context activity, Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        activity.startActivity(intent);
    }

    /**
     * Starts a new activity and clears the current activity stack.
     * <p>
     * This method is used to rebase the activity stack, ensuring that the new activity becomes
     * the root of a new task, and all previous activities are removed from the stack.
     * </p>
     *
     * <p><b>Note:</b> The back button will not navigate to previous activities after using this method.</p>
     *
     * @param activity    The context of the current activity.
     * @param destination The class of the activity to start.
     */
    public static void rebaseActivity(Context activity, Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }
}