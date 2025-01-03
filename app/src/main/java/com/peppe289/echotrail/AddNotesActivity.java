package com.peppe289.echotrail;

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
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityAddNotesBinding;
import com.peppe289.echotrail.utils.LocationHelper;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class AddNotesActivity extends AppCompatActivity {

    private ActivityAddNotesBinding binding;
    private boolean canPush = true;
    private LocationHelper locationHelper;
    private SwitchMaterial switchAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationHelper = new LocationHelper(this);
        if (!locationHelper.locationPermissionIsGranted(this)) {
            Toast.makeText(AddNotesActivity.this, "Permesso alla posizone non concesso!", Toast.LENGTH_SHORT).show();
            finish();
        }

        setUpToolBar();
        setUpAnonymousUserOptions();
        setupFocusOnTextArea();
    }

    private void setUpAnonymousUserOptions() {
        switchAnonymous = binding.switchAnonymous;
        switchAnonymous.setActivated(false);
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

        locationHelper.getCurrentLocation(this, this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationUpdated(GeoPoint location) {
                data.put("latitude", location.getLatitude());
                data.put("longitude", location.getLongitude());
                data.put("city", LocationHelper.getCityName(AddNotesActivity.this, location.getLatitude(),
                        location.getLongitude()));

                UserController.getUsername(username -> {
                    // save username only if the user is not anonymous
                    if (!switchAnonymous.isChecked())
                        data.put("username", username);

                    NotesController.saveNote(data, () -> {
                        Toast.makeText(AddNotesActivity.this, "Nota Condivisa!", Toast.LENGTH_SHORT).show();
                        // like mutex to avoid multiple click on save button.
                        canPush = true;
                        finish();
                    });
                });

            }

            @Override
            public void onLocationError(String error) {
                Toast.makeText(AddNotesActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
