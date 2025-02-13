package com.peppe289.echotrail.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textview.MaterialTextView;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.user.PreferencesController;
import com.peppe289.echotrail.ui.activity.MainActivity;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentAccountBinding;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.ui.dialog.IconPickerDialog;
import com.peppe289.echotrail.utils.*;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private MaterialTextView username;
    private MaterialTextView email;
    private com.google.android.material.textview.MaterialTextView publishedNotes;
    private com.google.android.material.textview.MaterialTextView readedNotes;
    private LoadingManager loadingManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        username = binding.usernameTextView;
        email = binding.emailTextView;

        loadingManager = new LoadingManager(rootView);
        loadingManager.showLoading();

        binding.logoutBtn.setOnClickListener(view -> {
            UserController.logout(requireContext());
            NavigationHelper.rebaseActivity(getActivity(), MainActivity.class, null);
            Log.i("AccountFragment", "User logged out");
        });

        publishedNotes = binding.notesPublished;
        readedNotes = binding.notesRead;

        AppBarLayout appBarLayout = binding.appBarLayout;

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            float percentage = Math.abs(verticalOffset) / (float) appBarLayout1.getTotalScrollRange();
            binding.userIcon.setScaleX(1 - percentage);
            binding.userIcon.setScaleY(1 - percentage);
            binding.userIcon.setAlpha(1 - percentage);
        });

        binding.mynotes.setOnClickListener(view -> NavigationHelper.startActivityForFragment(requireActivity(), UserListFragment.class, null));
        binding.mypreferences.setOnClickListener(view -> NavigationHelper.startActivityForFragment(requireActivity(), PreferencesFragment.class, null));
        binding.personalData.setOnClickListener(view -> NavigationHelper.startActivityForFragment(requireActivity(), PersonalInfoFragment.class, null));

        binding.version.setText(getAppVersion());

        fetchInfo();

        return rootView;
    }

    private String getAppVersion() {
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            String versionName = pInfo.versionName;
            return "v" + versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    /**
     * Setting the user icon and color when the fragment is resumed to the foreground
     * helps me (the sexy programmer) to update icon after change from account settings
     * and also when the fragment is added.
     */
    @Override
    public void onResume() {
        super.onResume();
        ImageUtils.setImageWithBackground(binding.imageView,
                IconPickerDialog.iconList.get(PreferencesController.getImageIndex()),
                IconPickerDialog.colorList.get(PreferencesController.getColorIndex()));
    }

    private void fetchInfo() {
        // load user headers (name and email) from the cache if possible.
        UserController.getUserHeadersFromPreferences(new ControllerCallback<HashMap<String, String>, ErrorType>() {
            @Override
            public void onSuccess(HashMap<String, String> result) {
                username.setText(result.get("username"));
                email.setText(result.get("email"));
            }

            @Override
            public void onError(ErrorType error) {
                handleError(error);
            }
        });

        UserController.listenerUserInfo(new ControllerCallback<User, ErrorType>() {
            @Override
            public void onSuccess(User userInfo) {
                if (userInfo != null) {
                    if (userInfo.getNotes() != null)
                        publishedNotes.setText(String.valueOf(userInfo.getNotes().size()));
                    else
                        publishedNotes.setText("0");

                    if (userInfo.getReadedNotes() != null)
                        readedNotes.setText(String.valueOf(userInfo.getReadedNotes().size()));
                    else
                        readedNotes.setText("0");

                    Integer imageIndex = userInfo.getImageIndex();
                    Integer colorIndex = userInfo.getColorIndex();

                    ImageUtils.setImageWithBackground(binding.imageView,
                            IconPickerDialog.iconList.get(imageIndex == null ? 0 : imageIndex),
                            IconPickerDialog.colorList.get(colorIndex == null ? 0 : colorIndex));

                    if (imageIndex != null) {
                        PreferencesController.setImageIndex(imageIndex);
                    }

                    if (colorIndex != null) {
                        PreferencesController.setColorIndex(colorIndex);
                    }

                    loadingManager.hideLoading();
                }
            }

            @Override
            public void onError(ErrorType error) {
                handleError(error);
            }
        });
    }

    private void handleError(ErrorType error) {
        DefaultErrorHandler.getInstance(null).showError(error);
    }
}