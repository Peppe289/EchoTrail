package com.peppe289.echotrail.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentPersonalInfoBinding;
import com.peppe289.echotrail.utils.DefaultErrorHandler;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.adapter.UserLinksAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonalInfoFragment extends Fragment {

    private TextView changePassword;
    private FragmentPersonalInfoBinding binding;
    private com.google.android.material.textfield.TextInputEditText usernameEditText;
    private String currentUsername;
    private com.google.android.material.button.MaterialButton saveButton;
    private com.google.android.material.button.MaterialButton cancelButton;
    private ListView linksView;
    private UserLinksAdapter userLinksAdapter;
    private LinearLayout addLinkLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        initialization();
        loadDefaultValue();
        setUpButton();

        return rootView;
    }

    private void setUpButton() {
        saveButton.setOnClickListener(v -> {
            String newUsername = Objects.requireNonNull(usernameEditText.getText()).toString();
            if (!newUsername.equals(currentUsername)) {
                UserController.setUsername(newUsername);
                requireActivity().finish();
            }
        });

        cancelButton.setOnClickListener(v -> requireActivity().finish());
        addLinkLayout.setOnClickListener(v -> shouwCustomInput(R.string.put_link, R.string.link, (str) -> {
            userLinksAdapter.add(str);
            UserController.updateUserLinks(str);
        }));
    }

    private void initialization() {
        changePassword = binding.changePassword;
        usernameEditText = binding.usernameEditText;
        saveButton = binding.saveButton;
        cancelButton = binding.cancelButton;
        addLinkLayout = binding.addLink;
        linksView = binding.linksList;
        userLinksAdapter = new UserLinksAdapter(requireContext(), R.layout.personal_link_row, new ArrayList<>());
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

        changePassword.setOnClickListener(v ->
                shouwCustomInput(R.string.write_old_password, R.string.password,
                        (oldPassword) -> shouwCustomInput(R.string.write_new_password, R.string.password,
                                (newPassword) ->
                                        UserController.changePassword(oldPassword, newPassword, new ControllerCallback<Void, ErrorType>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                Toast.makeText(requireContext(), getString(R.string.success_change_password), Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onError(ErrorType error) {
                                                Toast.makeText(requireContext(), error.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
                                            }
                                        }))));

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

    private void shouwCustomInput(int stringsID, int hint, CallBackInput callBackDialog) {
        View customView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextInputEditText inputEditText = customView.findViewById(R.id.link_edit_text);
        inputEditText.setHint(getString(hint));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(stringsID))
                .setView(customView)
                .setPositiveButton(getString(R.string.add), (dialogInterface, i) -> {
                    String inputText = inputEditText.getText() != null ? inputEditText
                            .getText().toString().replaceAll("[\\n\\r]", "") : "";

                    callBackDialog.onPositiveClick(inputText);
                })
                .setNegativeButton(getString(R.string.abort), (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void showCustomDialog(String title, String message, CallBackDialog callBackDialog, String negativeText, String positiveText) {
        new MaterialAlertDialogBuilder(requireContext())
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

    private void handleError(ErrorType error) {
        DefaultErrorHandler.getInstance(null).showError(error);
    }
}
