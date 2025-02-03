package com.peppe289.echotrail;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityRegistrationBinding;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.FormValidator;
import com.peppe289.echotrail.utils.MoveActivity;

import java.util.Objects;

/**
 * Activity responsible for handling user registration.
 * This activity collects user data such as username, email, and password,
 * validates the input, and submits the registration data to the server.
 */
public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private ProgressBar progressBar;

    private TextInputLayout usernameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    /**
     * Called when the activity is created.
     * It initializes the UI components, sets up window insets, and binds the UI elements
     * to the corresponding views.
     *
     * @param savedInstanceState Bundle containing the saved instance state.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityRegistrationBinding binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind UI components with variables
        usernameEditText = findViewById(R.id.textInputEditTextUsername);
        emailEditText = findViewById(R.id.textInputEditTextEmail);
        passwordEditText = findViewById(R.id.textInputEditTextPassword);

        usernameLayout = findViewById(R.id.textInputLayoutUsername);
        emailLayout = findViewById(R.id.textInputLayoutEmail);
        passwordLayout = findViewById(R.id.textInputLayoutPassword);

        progressBar = findViewById(R.id.progressBar);

        setUpToolBar();
        // Set up the submit button click listener
        binding.registrationBtn.setOnClickListener(v -> submitRegistration());
    }

    private void setUpToolBar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Submits the registration data if the input is valid.
     * If validation fails, a toast is shown to the user.
     */
    protected void submitRegistration() {
        if (validateInputs()) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            try {
                String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
                String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();
                String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
                // Submit the registration data to the server
                UserController.register(email, password, username, this::handleRegistrationResponse);
            } catch (Exception e) {
                // Handle any error during the registration process
                handleError();
                progressBar.setVisibility(ProgressBar.GONE);
            }
        } else {
            showToast(ErrorType.INVALID_DATA_ERROR.getMessage(getApplicationContext()));
        }
    }

    /**
     * Handles the server response for the registration attempt.
     * If the registration is successful, it redirects to the next activity.
     * If it fails, an error message is shown.
     *
     * @param success true if registration is successful, false otherwise.
     */
    private void handleRegistrationResponse(boolean success) {
        if (success) {
            // Redirect to the DispatcherActivity if registration is successful
            MoveActivity.rebaseActivity(this, DispatcherActivity.class, null);
        } else {
            // Show error message if registration fails
            handleError();
        }
    }

    /**
     * Handles errors during the registration process by logging the error
     * and showing a generic error message to the user.
     */
    private void handleError() {
        showToast(ErrorType.UNKNOWN_ERROR.getMessage(getApplicationContext()));
    }

    /**
     * Displays a toast message to the user.
     *
     * @param message the message to be displayed in the toast.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Validates the input fields (username, email, and password).
     * Each field is validated using a specific rule defined in the FormValidator class.
     *
     * @return true if all fields are valid, false otherwise.
     */
    private boolean validateInputs() {
        boolean isValid = true;

        isValid &= FormValidator.validateField(
                usernameEditText,
                usernameLayout,
                FormValidator::isValidUsername,
                ErrorType.INVALID_USERNAME_ERROR.getMessage(getApplicationContext())
        );

        isValid &= FormValidator.validateField(
                emailEditText,
                emailLayout,
                FormValidator::isValidEmail,
                ErrorType.INVALID_EMAIL_ERROR.getMessage(getApplicationContext())
        );

        isValid &= FormValidator.validateField(
                passwordEditText,
                passwordLayout,
                FormValidator::isValidPassword,
                ErrorType.INVALID_PASSWORD_ERROR.getMessage(getApplicationContext())
        );

        return isValid;
    }
}