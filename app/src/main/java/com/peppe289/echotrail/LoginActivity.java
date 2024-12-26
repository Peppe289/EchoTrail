package com.peppe289.echotrail;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityLoginBinding;
import com.peppe289.echotrail.utils.MoveActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.textInputEditTextEmail);
        passwordEditText = findViewById(R.id.textInputEditTextPassword);

        emailLayout = findViewById(R.id.textInputLayoutEmail);
        passwordLayout = findViewById(R.id.textInputLayoutPassword);


        binding.submitButton.setOnClickListener(v -> verifyLoginOnSubmit());
    }

    protected void verifyLoginOnSubmit() {
        if (validateInputs()) {
            UserController.login(Objects.requireNonNull(emailEditText.getText()).toString(), Objects.requireNonNull(passwordEditText.getText()).toString(), (result) -> {
                if (result) {
                    MoveActivity.rebaseActivity(LoginActivity.this, DispatcherActivity.class);
                } else {
                    passwordLayout.setError("Email o password errati");
                }
            });
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Inserisci un'email valida.");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        String password = Objects.requireNonNull(passwordEditText.getText()).toString();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordLayout.setError("La password deve avere almeno 6 caratteri.");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }
}
