package com.peppe289.echotrail;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.peppe289.echotrail.controller.user.FriendsController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityFriendsBinding;
import com.peppe289.echotrail.model.FriendItem;
import com.peppe289.echotrail.utils.FriendsCustomAdapter;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.MoveActivity;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class FriendsActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private FriendsCustomAdapter adapter;
    private ListView listView;
    private LoadingManager loadingManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityFriendsBinding binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.toolbar);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadingManager = new LoadingManager(binding.getRoot());
        loadingManager.showLoading();

        listView = findViewById(R.id.friend_list);
        adapter = new FriendsCustomAdapter(this, R.layout.friend_item, new ArrayList<>());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            FriendItem friendItem = adapter.getItem(position);
            if (friendItem != null) {
                MoveActivity.addActivity(this, AddNotesActivity.class, intent -> {
                    intent.putExtra("friendId", friendItem.getUid());
                    intent.putExtra("friendName", friendItem.getName());
                });
            }
        });

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        adapter.setCallback(new FriendsCustomAdapter.OnFriendCallback() {
            @Override
            public void onAllowClick(String friendId, int position) {
                FriendsController.acceptRequest(friendId, success -> {
                    if (success) {
                        FriendItem fi = adapter.getItem(position);
                        fi.setOnPendingRequest(false);
                        fi.setFriend(true);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(FriendsActivity.this, "Errore durante l'accettazione dell'amicizia", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRemoveClick(String friendId, int position, boolean isFriends) {
                FriendsController.removeFriend(friendId, () -> {
                    adapter.remove(adapter.getItem(position));
                    adapter.notifyDataSetChanged();
                });
            }
        });

        FriendsController.searchPendingRequests(pendingFriends -> {
            if (pendingFriends != null) {
                runOnUiThread(() -> {
                    for (String id : pendingFriends) {
                        String finalId = id.trim();
                        UserController.getUserInfoByUID(finalId, user -> {
                            adapter.add(new FriendItem((String) user.get("username"), true, false, finalId));
                            adapter.notifyDataSetChanged();
                        });
                    }
                });
            }

            AtomicReference<Integer> size = new AtomicReference<>(0);
            // check at first and all time when have trigger event to update friends list
            FriendsController.getUIDFriendsList(friendList ->
                    FriendsController.getUIDFriendsList(friends -> {
                        // is better if I clean the list before retrieve the new data
                        adapter.clear();
                        if (friends != null) {
                            runOnUiThread(() -> {
                                size.set(friends.size());
                                if (size.get() >= 0)
                                    loadingManager.showLoading();

                                for (String id : friends) {
                                    String finalId = id.trim();
                                    UserController.getUserInfoByUID(finalId, user -> {
                                        adapter.add(new FriendItem((String) user.get("username"), false, true, finalId));
                                        adapter.notifyDataSetChanged();
                                        size.getAndSet(size.get() - 1);
                                        if (size.get() <= 0) {
                                            loadingManager.hideLoading();
                                        }
                                    });
                                }
                            });
                        }
                    }));
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed(); // Torna alla schermata precedente
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_toolbar_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Cerca qui...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Log.d("SearchView", "onQueryTextSubmit: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.d("SearchView", "onQueryTextChange: " + newText);
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }
}
