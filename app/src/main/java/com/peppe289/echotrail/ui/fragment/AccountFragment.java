package com.peppe289.echotrail.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.peppe289.echotrail.ui.activity.MainActivity;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentAccountBinding;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.utils.DefaultErrorHandler;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.NavigationHelper;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private FragmentAccountBinding binding;
    private MaterialTextView username;
    private MaterialTextView email;
    private ScheduledFuture<?> scheduledFuture;
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

        // TODO: implements the button function (should be open notify settings)
        binding.notify.setOnClickListener(view -> Log.i("AccountFragment", "Notify settings button clicked"));

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

        binding.idTextView.setText(UserController.getUid());
        binding.copyIdLayout.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("UID", binding.idTextView.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(binding.getRoot().getContext(), "UID Copied", Toast.LENGTH_SHORT).show();
        });

        startFetchingUserInfo();
        fetchInfo();

        return rootView;
    }

    private void startFetchingUserInfo() {
        scheduledFuture = executorService.scheduleWithFixedDelay(this::fetchInfo, 0, 5, TimeUnit.SECONDS);
    }

    private void fetchInfo() {
        requireActivity().runOnUiThread(() -> {
            // load user headers (name and email) from the cache if possible.
            UserController.getUserHeadersFromPreferences(requireContext(), new ControllerCallback<HashMap<String, String>, ErrorType>() {
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

            UserController.getUserInfoByUID(UserController.getUid(), new ControllerCallback<User, ErrorType>() {
                @Override
                public void onSuccess(User userInfo) {
                    if (userInfo != null) {
                        publishedNotes.setText(String.valueOf(userInfo.getNotes().size()));
                        readedNotes.setText(String.valueOf(userInfo.getReadedNotes().size()));
                        loadingManager.hideLoading();
                    }
                }

                @Override
                public void onError(ErrorType error) {
                    handleError(error);
                }
            });
        });
    }

    private void handleError(ErrorType error) {
        DefaultErrorHandler.getInstance(null).showError(error);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
        executorService.shutdown();
    }
}