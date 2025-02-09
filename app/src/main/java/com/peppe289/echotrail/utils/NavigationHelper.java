package com.peppe289.echotrail.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.peppe289.echotrail.ui.activity.ContainerActivity;

/**
 * The {@code MoveActivity} utility class provides reusable methods to manage activity navigation.
 * It simplifies the process of starting new activities with optional stack management,
 * such as clearing the back stack or preserving the current task.
 */
@SuppressWarnings("unused")
public class NavigationHelper {

    /**
     * Add a new fragment in new empty activity. This help the modularity of the app
     * and make the app more scalable.
     *
     * @param context   The context of the current activity.
     * @param fragment  The fragment to add to the activity.
     * @param args      The arguments to pass to the fragment.
     */
    public static void startActivityForFragment(Context context, Class<? extends Fragment> fragment, Bundle args) {
        Intent intent = new Intent(context, ContainerActivity.class);
        String str = fragment.getName();
        intent.putExtra(ContainerActivity.FRAGMENT_KEY, fragment.getName());
        if (args != null) {
            intent.putExtra(ContainerActivity.ARGS_KEY, args);
        }
        context.startActivity(intent);
    }

    /**
     * Starts a new activity without modifying the current activity stack.
     * <p>
     * This method is used when you want to add a new activity on top of the existing task stack.
     * </p>
     *
     * @param activity    The context of the current activity.
     * @param destination The class of the activity to start.
     */
    public static void addActivity(Context activity, Class<?> destination, MoveActivityCallback callback) {
        Intent intent = new Intent(activity, destination);
        if (callback != null) {
            callback.onActivityMoved(intent);
        }
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
    public static void rebaseActivity(Context activity, Class<?> destination, MoveActivityCallback callback) {
        Intent intent = new Intent(activity, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (callback != null) {
            callback.onActivityMoved(intent);
        }
        activity.startActivity(intent);
    }

    public interface MoveActivityCallback {
        void onActivityMoved(Intent intent);
    }
}
