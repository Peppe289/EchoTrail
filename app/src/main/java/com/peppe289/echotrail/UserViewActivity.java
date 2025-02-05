package com.peppe289.echotrail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.peppe289.echotrail.controller.user.FriendsController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.user.FriendsDAO;
import com.peppe289.echotrail.databinding.ActivityUserViewBinding;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.UserLinksAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserViewActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private ActivityUserViewBinding binding;
    private LoadingManager loadingManager;
    private MaterialButton sendFriendRequestButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.toolbar);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        initialize();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed(); // Torna alla schermata precedente
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initialize() {
        sendFriendRequestButton = findViewById(R.id.send_friend_request);

        loadingManager = new LoadingManager(binding.getRoot());
        loadingManager.showLoading();

        String UID = getIntent().getStringExtra("UID");

        ListView listView = findViewById(R.id.list_links);
        UserLinksAdapter adapter = new UserLinksAdapter(this, R.layout.personal_link_row, new ArrayList<>());
        listView.setAdapter(adapter);

        // if the user is already a friend, disable the button
        sendFriendRequestButton.setOnClickListener(v -> {
            FriendsController.requestToBeFriends(UID, success -> {
                if (success) {
                    sendFriendRequestButton.setIconResource(R.drawable.check_24px);
                    sendFriendRequestButton.setText("Richiesta inviata!");
                } else {
                    Toast.makeText(this, ErrorType.SEND_FRIEND_REQUEST_ERROR.getMessage(getApplicationContext()), Toast.LENGTH_SHORT).show();
                }
            });
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String link = adapter.getItem(position);
            if (link != null) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(link));
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(this, ErrorType.CANNOT_OPEN_LINK_ERROR.getMessage(getApplicationContext()), Toast.LENGTH_SHORT).show();
                }
            }
        });


        UserController.getUserInfoByUID(UID, userInfo -> {
            if (userInfo != null) {
                setTextViewIfNotNull(binding.usernameTextView, userInfo.getUsername());
                setTextViewIfNotNull(binding.notesRead, userInfo.getReadedNotes().size());
                setTextViewIfNotNull(binding.notesPublished, userInfo.getNotes().size());
                UserController.getUserLinks(UID, links -> {
                    if (links != null) {
                        for (String lk : links) {
                            adapter.add(lk);
                        }
                    }
                });
            }

            FriendsController.getUIDFriendsList(new FriendsDAO.GetFriendsCallback() {

                @Override
                public void onFriendsRetrieved(List<String> friends) {
                    if (friends != null) {
                        for (String fr : friends) {
                            if (fr.equals(UID)) {
                                // disable the button if the user is already a friend
                                sendFriendRequestButton.setIconResource(R.drawable.check_24px);
                                sendFriendRequestButton.setText("Amico");
                                sendFriendRequestButton.setEnabled(false);
                            }
                        }
                    }
                    loadingManager.hideLoading();
                }

                @Override
                public void onError(ErrorType error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(getApplicationContext()), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setTextViewIfNotNull(TextView textView, Object value) {
        if (value != null) {
            textView.setText(value.toString());
        }
    }
}
