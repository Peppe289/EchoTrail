package com.peppe289.echotrail.fragment;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.MainActivity;
import com.peppe289.echotrail.NotesListActivity;
import com.peppe289.echotrail.PersonalInfoActivity;
import com.peppe289.echotrail.PreferencesActivity;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.databinding.FragmentAccountBinding;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.MoveActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment implements PersonalInfoActivity.OnAccountEditedListener {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private FragmentAccountBinding binding;
    private MaterialTextView username;
    private MaterialTextView email;
    private ScheduledFuture<?> scheduledFuture;
    private com.google.android.material.textview.MaterialTextView publishedNotes;
    private com.google.android.material.textview.MaterialTextView readedNotes;
    private LoadingManager loadingManager;

    @Override
    public void onAccountEdited() {
        triggerFetchInfoImmediately();
    }

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
            MoveActivity.rebaseActivity(getActivity(), MainActivity.class, null);
            Log.i("AccountFragment", "User logged out");
        });

        publishedNotes = binding.notesPublished;
        readedNotes = binding.notesRead;

        AppBarLayout appBarLayout = binding.appBarLayout;
        PersonalInfoActivity.AccountEditNotifier.getInstance().setListener(this);

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            float percentage = Math.abs(verticalOffset) / (float) appBarLayout1.getTotalScrollRange();
            binding.userIcon.setScaleX(1 - percentage);
            binding.userIcon.setScaleY(1 - percentage);
            binding.userIcon.setAlpha(1 - percentage);
        });

        binding.mynotes.setOnClickListener(view -> MoveActivity.addActivity(requireActivity(), NotesListActivity.class, null));
        binding.mypreferences.setOnClickListener(view -> MoveActivity.addActivity(requireActivity(), PreferencesActivity.class, null));
        binding.personalData.setOnClickListener(view -> MoveActivity.addActivity(requireActivity(), PersonalInfoActivity.class, null));

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

    private void triggerFetchInfoImmediately() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
        }
        fetchInfo();
        startFetchingUserInfo();
    }

    private void fetchInfo() {
        requireActivity().runOnUiThread(() -> {
            // load user headers (name and email) from the cache if possible.
            UserController.getUserHeadersFromPreferences(requireContext(), headers -> {
                username.setText(headers.get("username"));
                email.setText(headers.get("email"));
            });

            List<String> notesWritten = new ArrayList<>();
            List<String> notesReaded = new ArrayList<>();

            UserController.getUserNotesList(new UserDAO.NotesListCallback() {
                @Override
                public void onComplete(QuerySnapshot userNotes) {
                    if (userNotes != null && !userNotes.isEmpty()) {
                        for (DocumentSnapshot document : userNotes) {
                            if (document != null) notesWritten.add(document.getId());
                        }
                    }
                    publishedNotes.setText(String.valueOf(notesWritten.size()));

                    UserController.getReadedNotesList(new UserDAO.NotesListCallback() {
                        @Override
                        public void onComplete(QuerySnapshot notes) {
                            if (notes != null && !notes.isEmpty()) {
                                for (DocumentSnapshot document : notes) {
                                    if (document != null) notesReaded.add(document.getId());
                                }
                            }
                            readedNotes.setText(String.valueOf(notesReaded.size()));
                            loadingManager.hideLoading();
                        }

                        @Override
                        public void onError(ErrorType errorType) {
                            Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
                            loadingManager.hideLoading();
                        }
                    });
                }

                @Override
                public void onError(ErrorType errorType) {
                    Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
                    loadingManager.hideLoading();
                }
            });
        });
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