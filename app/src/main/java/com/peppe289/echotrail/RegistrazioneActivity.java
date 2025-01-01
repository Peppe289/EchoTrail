package com.peppe289.echotrail;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityRegistrazioneBinding;
import com.peppe289.echotrail.utils.MoveActivity;

import java.util.Objects;

public class RegistrazioneActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;

    private TextInputLayout usernameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    private ActivityRegistrazioneBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegistrazioneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.textInputEditTextUsername);
        emailEditText = findViewById(R.id.textInputEditTextEmail);
        passwordEditText = findViewById(R.id.textInputEditTextPassword);

        usernameLayout = findViewById(R.id.textInputLayoutUsername);
        emailLayout = findViewById(R.id.textInputLayoutEmail);
        passwordLayout = findViewById(R.id.textInputLayoutPassword);

        binding.submitButton.setOnClickListener(v -> {
            submitRegistrazione();
        });
    }

    // submit method. check if is valid, then use controller for registration.
    protected void submitRegistrazione() {
        if (validateInputs()) {
            try {
                UserController.register(Objects.requireNonNull(emailEditText.getText()).toString(),
                        Objects.requireNonNull(passwordEditText.getText()).toString(),
                        Objects.requireNonNull(usernameEditText.getText()).toString(),
                        success -> {
                            if (success) {
                                MoveActivity.rebaseActivity(this, DispatcherActivity.class, null);
                            } else {
                                Toast.makeText(this, "Errore durante la registrazione",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                Log.e("RegistrazioneActivity", "Errore durante la registrazione", e);
                Toast.makeText(this, "Errore durante la registrazione", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Dati non validi", Toast.LENGTH_SHORT).show();
        }
    }

    // validate registration form
    private boolean validateInputs() {
        boolean isValid = true;

        String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Il campo username è obbligatorio.");
            isValid = false;
        } else {
            usernameLayout.setError(null);
        }

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
