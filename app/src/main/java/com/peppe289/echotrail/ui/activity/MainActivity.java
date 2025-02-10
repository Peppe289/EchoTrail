package com.peppe289.echotrail.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.controller.user.FriendsController;
import com.peppe289.echotrail.controller.user.PreferencesController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityMainBinding;
import com.peppe289.echotrail.utils.*;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private ProgressBar progressBar;

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    @Override
    protected void attachBaseContext(Context base) {
        PreferencesController.init(base);
        super.attachBaseContext(LanguageUtils.setAppLanguage(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserController.init();
        NotesController.init();
        FriendsController.init();
        DefaultErrorHandler.getInstance(getApplicationContext());
        FirebaseApp.initializeApp(getApplicationContext());

        com.peppe289.echotrail.databinding.ActivityMainBinding binding =
                ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // if the user is already logged (from android sdk) skipp this first page.
        if (UserController.isLoggedIn() &&
                UniqueIDHelper.getUUID(getApplicationContext()) != null) {
            // this allow to access offline.
            NavigationHelper.rebaseActivity(MainActivity.this, DispatcherActivity.class, null);
            UserController.getSession(UniqueIDHelper.getUUID(getApplicationContext()), new ControllerCallback<Boolean, ErrorType>() {
                @Override
                public void onSuccess(Boolean result) {
                    // Don't put lunch activity here because can make delay.
                }

                @Override
                public void onError(ErrorType error) {
                    // if preferences are invalid with a server database, some stuff when wrong, so invalidate this login.
                    try {
                        // when done if necessary, back to the login page without cause an error.
                        UserController.logout(getApplicationContext());
                    } catch (RuntimeException ignore) {
                    }
                }
            });
        }

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

        progressBar = findViewById(R.id.progressBar);

        binding.registrationBtn.setOnClickListener(view -> NavigationHelper.addActivity(MainActivity.this, RegistrationActivity.class, null));
        findViewById(R.id.loginBtn).setOnClickListener(view -> verifyLoginOnSubmit());
    }

    protected void verifyLoginOnSubmit() {
        if (validateInputs()) {
            progressBar.setVisibility(View.VISIBLE);
            UserController.login(
                    Objects.requireNonNull(emailEditText.getText()).toString(),
                    Objects.requireNonNull(passwordEditText.getText())
                            .toString(), new ControllerCallback<Void, ErrorType>() {
                        @Override
                        public void onSuccess(Void result) {
                            UniqueIDHelper.addSessionAtLogin(getApplicationContext());
                            NavigationHelper.rebaseActivity(getApplication(), DispatcherActivity.class, null);
                        }

                        @Override
                        public void onError(ErrorType error) {
                            passwordLayout.setError(ErrorType.INVALID_CREDENTIALS_ERROR.getMessage(getApplicationContext()));
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(ErrorType.INVALID_EMAIL_ERROR.getMessage(getApplicationContext()));
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        String password = Objects.requireNonNull(passwordEditText.getText()).toString();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordLayout.setError(ErrorType.INVALID_PASSWORD_ERROR.getMessage(getApplicationContext()));
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }
}
