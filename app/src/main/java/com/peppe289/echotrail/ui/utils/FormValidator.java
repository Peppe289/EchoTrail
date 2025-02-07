package com.peppe289.echotrail.ui.utils;

import android.text.TextUtils;
import android.util.Patterns;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Function;

public class FormValidator {

    public static boolean isValidUsername(String username) {
        return !TextUtils.isEmpty(username);
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    public static boolean validateField(TextInputEditText editText, TextInputLayout layout, Function<String, Boolean> validationRule, String errorMessage) {
        String input = editText.getText() != null ? editText.getText().toString().trim() : "";
        if (!validationRule.apply(input)) {
            layout.setError(errorMessage);
            return false;
        } else {
            layout.setError(null);
            return true;
        }
    }
}
