package com.peppe289.echotrail.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityPersonalInfoBinding;
import com.peppe289.echotrail.utils.DefaultErrorHandler;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.adapter.UserLinksAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonalInfoActivity extends AppCompatActivity {

    private ActivityPersonalInfoBinding binding;
    private com.google.android.material.textfield.TextInputEditText usernameEditText;
    private String currentUsername;
    private com.google.android.material.button.MaterialButton saveButton;
    private com.google.android.material.button.MaterialButton cancelButton;
    private OnAccountEditedListener listener;
    private ListView linksView;
    private UserLinksAdapter userLinksAdapter;
    private LinearLayout addLinkLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPersonalInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialization();
        loadDefaultValue();
        setUpToolBar();
        setUpButton();
    }

    private void setUpButton() {
        saveButton.setOnClickListener(v -> {
            String newUsername = Objects.requireNonNull(usernameEditText.getText()).toString();
            if (!newUsername.equals(currentUsername)) {
                UserController.setUsername(newUsername);
                finish();
            }
            if (listener != null) {
                listener.onAccountEdited();
            }
        });

        cancelButton.setOnClickListener(v -> finish());
        addLinkLayout.setOnClickListener(v -> shouwCustomInput((str) -> {
            userLinksAdapter.add(str);
            UserController.updateUserLinks(str);
        }));
    }

    private void initialization() {
        usernameEditText = binding.usernameEditText;
        saveButton = binding.saveButton;
        cancelButton = binding.cancelButton;
        listener = AccountEditNotifier.getInstance().getListener();
        addLinkLayout = findViewById(R.id.add_link);
        linksView = findViewById(R.id.links_list);
        userLinksAdapter = new UserLinksAdapter(this, R.layout.personal_link_row, new ArrayList<>());
        linksView.setAdapter(userLinksAdapter);
        linksView.setOnItemClickListener((parent, view, position, id) ->
                showCustomDialog("Eliminare?", "Stai per eliminare questo link dal tuo account. Sei sicuro?",
                        () -> {
                            String link = userLinksAdapter.getItem(position);
                            if (link != null) {
                                userLinksAdapter.remove(link);
                                UserController.removeUserLink(link);
                            }
                        }, "Annulla", "Elimina"));

        UserController.getUserLinks(new ControllerCallback<List<String>, ErrorType>() {
            @Override
            public void onSuccess(List<String> result) {
                if (result != null) {
                    for (String link : result) {
                        userLinksAdapter.add(link);
                    }
                }
            }

            @Override
            public void onError(ErrorType error) {
                handleError(error);
            }
        });
    }

    private void loadDefaultValue() {
        UserController.getUsername(new ControllerCallback<String, ErrorType>() {
            @Override
            public void onSuccess(String result) {
                currentUsername = result;
                usernameEditText.setText(result);
            }

            @Override
            public void onError(ErrorType error) {
                handleError(error);
            }
        });
    }

    private void setUpToolBar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (!Objects.requireNonNull(usernameEditText.getText()).toString().equals(currentUsername)) {
                showCustomDialog(getString(R.string.request_keep_page), getString(R.string.request_keep_page_desc),
                        () -> getOnBackPressedDispatcher().onBackPressed());
            } else {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void shouwCustomInput(CallBackInput callBackDialog) {
        View customView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextInputEditText inputEditText = customView.findViewById(R.id.link_edit_text);

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.put_link))
                .setView(customView)
                .setPositiveButton(getString(R.string.add), (dialogInterface, i) -> {
                    String inputText = inputEditText.getText() != null ? inputEditText
                            .getText().toString().replaceAll("[\\n\\r]", "") : "";

                    callBackDialog.onPositiveClick(inputText);
                })
                .setNegativeButton(getString(R.string.abort), (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void showCustomDialog(String title, String message, CallBackDialog callBackDialog) {
        showCustomDialog(title, message, callBackDialog, "Annulla", "Ignora");
    }

    private void showCustomDialog(String title, String message, CallBackDialog callBackDialog, String negativeText, String positiveText) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (dialogInterface, i) -> callBackDialog.onPositiveClick())
                .setNegativeButton(negativeText, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private interface CallBackDialog {
        void onPositiveClick();
    }

    private interface CallBackInput {
        void onPositiveClick(String inputText);
    }

    public interface OnAccountEditedListener {
        void onAccountEdited();
    }

    private void handleError(ErrorType error) {
        DefaultErrorHandler.getInstance(null).showError(error);
    }

    public static class AccountEditNotifier {
        private static AccountEditNotifier instance;
        private OnAccountEditedListener listener;

        private AccountEditNotifier() {
        }

        public static AccountEditNotifier getInstance() {
            if (instance == null) {
                instance = new AccountEditNotifier();
            }
            return instance;
        }

        public OnAccountEditedListener getListener() {
            return listener;
        }

        public void setListener(OnAccountEditedListener listener) {
            this.listener = listener;
        }
    }

}
