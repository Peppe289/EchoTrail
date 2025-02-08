package com.peppe289.echotrail.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.callback.LocationCallback;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityAddNotesBinding;
import com.peppe289.echotrail.model.Friend;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LocationHelper;

import com.peppe289.echotrail.utils.callback.HelperCallback;
import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class AddNotesActivity extends AppCompatActivity {

    private ActivityAddNotesBinding binding;
    private boolean canPush = true;
    private LocationHelper locationHelper;
    private SwitchMaterial switchAnonymous;
    private Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom + imeInsets.bottom
            );

            return insets;
        });

        if (getIntent().getExtras() != null) {
            friend = new Friend();
            friend.setName(getIntent().getExtras().getString("friendName"));
            friend.setUid(getIntent().getExtras().getString("friendId"));
        }

        locationHelper = new LocationHelper(this);
        if (!locationHelper.locationPermissionIsGranted(this)) {
            Toast.makeText(AddNotesActivity.this,
                    ErrorType.POSITION_PERMISSION_ERROR.getMessage(getApplicationContext()), Toast.LENGTH_SHORT).show();
            finish();
        }

        setUpToolBar();
        setUpAnonymousUserOptions();
        setupFocusOnTextArea();
    }

    private void setUpAnonymousUserOptions() {
        switchAnonymous = binding.switchAnonymous;
        UserController.getDefaultAnonymousPreference(
                new ControllerCallback<Boolean, ErrorType>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        switchAnonymous.setChecked(result);
                    }

                    @Override
                    public void onError(ErrorType error) {
                        switchAnonymous.setChecked(false);
                    }
                });
    }

    private void setupFocusOnTextArea() {
        TextInputEditText editText = binding.inputTextNote;
        editText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // consider this just for focus input text. the keyboard will be shown automatically using
                // android:windowSoftInputMode="stateVisible" in AndroidManifest.
                editText.requestFocus();
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

        if (friend != null) {
            toolbar.setTitle("Per: " + friend.getName());
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_notes_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (canPush) {
                canPush = false;
                saveNote();
            }
        } else return super.onOptionsItemSelected(item);

        return true;
    }

    private void saveNote() {
        TextInputEditText editText = binding.inputTextNote;

        if (editText.getText() == null && editText.getText().toString().isEmpty()) {
            canPush = true;
            return;
        }

        String note = editText.getText().toString();

        Map<String, Object> data = new HashMap<>();

        data.put("content", note);

        locationHelper.getCurrentLocation(this, this, new LocationCallback<>() {
            @Override
            public void onSuccess(GeoPoint location) {
                data.put("latitude", location.getLatitude());
                data.put("longitude", location.getLongitude());


                LocationHelper.getCityName(AddNotesActivity.this, location.getLatitude(),
                        location.getLongitude(), new HelperCallback<String, ErrorType>() {
                            @Override
                            public void onSuccess(String locationString) {
                                UserController.getUsername(new ControllerCallback<String, ErrorType>() {
                                    @Override
                                    public void onSuccess(String username) {
                                        data.put("city", locationString);
                                        // save username only if the user is not anonymous
                                        if (!switchAnonymous.isChecked())
                                            data.put("username", username);

                                        if (friend != null) {
                                            data.put("send_to", friend.getUid());
                                        }

                                        NotesController.saveNote(data, (errorType) -> {
                                            // like mutex to avoid multiple click on save button.
                                            canPush = true;
                                            if (errorType == null) {
                                                Toast.makeText(AddNotesActivity.this, "Nota Condivisa!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(AddNotesActivity.this, errorType.getMessage(getApplicationContext()), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(ErrorType error) {
                                        // TODO: handle error
                                    }
                                });
                            }

                            @Override
                            public void onError(ErrorType error) {
                                // TODO: handle error
                            }
                        });
            }

            @Override
            public void onError(ErrorType error) {
                // TODO: handle error
            }
        });
    }
}
