package com.peppe289.echotrail.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.FriendsController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityFriendsBinding;
import com.peppe289.echotrail.model.Friend;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.adapter.FriendsCustomAdapter;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.NavigationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
            Friend friend = adapter.getItem(position);
            if (friend != null) {
                NavigationHelper.addActivity(this, AddNotesActivity.class, intent -> {
                    intent.putExtra("friendId", friend.getUid());
                    intent.putExtra("friendName", friend.getName());
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
                FriendsController.acceptRequest(friendId, new ControllerCallback<Void, ErrorType>() {
                    @Override
                    public void onSuccess(Void result) {
                        Friend fi = adapter.getItem(position);
                        fi.setOnPendingRequest(false);
                        fi.setFriend(true);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ErrorType error) {
                        Toast.makeText(FriendsActivity.this,
                                ErrorType.ACCEPT_FRIEND_REQUEST_ERROR
                                        .getMessage(getApplicationContext()), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRemoveClick(String friendId, int position, boolean isFriends) {
                FriendsController.removeFriend(friendId, new ControllerCallback<Void, ErrorType>() {
                    @Override
                    public void onSuccess(Void result) {
                        adapter.remove(adapter.getItem(position));
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ErrorType error) {
                        Toast.makeText(FriendsActivity.this,
                                error.getMessage(getApplicationContext()), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        loadFriendsList();
    }

    private void loadFriendsList() {
        loadingManager.showLoading();

        FriendsController.loadFriends(new ControllerCallback<List<Friend>, ErrorType>() {
            @Override
            public void onSuccess(List<Friend> friends) {
                adapter.clear();

                if (friends == null || friends.isEmpty()) {
                    findViewById(R.id.empty_list).setVisibility(View.VISIBLE);
                    loadingManager.hideLoading();
                    return;
                }

                runOnUiThread(() -> {
                    AtomicInteger size = new AtomicInteger();
                    size.set(friends.size());
                    for (Friend fr : friends) {
                        UserController.getUserInfoByUID(fr.getUid(), new ControllerCallback<User, ErrorType>() {
                            @Override
                            public void onSuccess(User result) {
                                if (result != null && result.getColorIndex() != null && result.getImageIndex() != null) {
                                    fr.setColorIndex(result.getColorIndex());
                                    fr.setImageIndex(result.getImageIndex());
                                } else {
                                    fr.setColorIndex(0);
                                    fr.setImageIndex(0);
                                }

                                adapter.notifyDataSetChanged();

                                // hide loading bar when finish syncing all notes
                                size.getAndSet(size.get() - 1);
                                if (size.get() <= 0) {
                                    loadingManager.hideLoading();
                                }
                            }

                            @Override
                            public void onError(ErrorType error) {
                                fr.setColorIndex(0);
                                fr.setImageIndex(0);
                                adapter.notifyDataSetChanged();

                                // hide loading bar when finish syncing all notes
                                size.getAndSet(size.get() - 1);
                                if (size.get() <= 0) {
                                    loadingManager.hideLoading();
                                }
                            }
                        });
                        adapter.add(fr);
                        if (fr.isFriend())
                            adapter.remove(fr.getUid(), true);
                    }
                });
            }

            @Override
            public void onError(ErrorType errorType) {
                runOnUiThread(() -> {
                    Toast.makeText(FriendsActivity.this, errorType.getMessage(getApplicationContext()), Toast.LENGTH_SHORT).show();
                    loadingManager.hideLoading();
                });
            }
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
