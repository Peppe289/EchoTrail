package com.peppe289.echotrail;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityAccountViewBinding;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.UserLinksAdapter;

import java.util.ArrayList;

public class AccountViewActivity extends AppCompatActivity {

    private ActivityAccountViewBinding binding;
    private LoadingManager loadingManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialize();
    }

    private void initialize() {
        binding.copyIdLayout.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("UID", binding.idTextView.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "UID Copied", Toast.LENGTH_SHORT).show();
        });

        loadingManager = new LoadingManager(binding.getRoot());
        loadingManager.showLoading();

        String UID = getIntent().getStringExtra("UID");
        binding.idTextView.setText(UID);

        ListView listView = findViewById(R.id.list_links);
        UserLinksAdapter adapter = new UserLinksAdapter(this, R.layout.personal_link_row, new ArrayList<>());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String link = adapter.getItem(position);
            if (link != null) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(link));
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(this, "Nessuna app in grado di aprire il link!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        UserController.getUserInfoByUID(UID, userInfo -> {
            if (userInfo != null) {
                setTextViewIfNotNull(binding.usernameTextView, userInfo.get("username"));
                setTextViewIfNotNull(binding.notesRead, userInfo.get("readedNotes"));
                setTextViewIfNotNull(binding.notesPublished, userInfo.get("notes"));
                loadingManager.hideLoading();
                UserController.getUserLinks(UID, links -> {
                    if (links != null) {
                        for (String lk : links) {
                            adapter.add(lk);
                        }
                    }
                });
            }
        });
    }

    private void setTextViewIfNotNull(TextView textView, Object value) {
        if (value != null) {
            textView.setText(value.toString());
        }
    }
}
