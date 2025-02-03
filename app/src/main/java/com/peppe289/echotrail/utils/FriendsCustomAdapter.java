package com.peppe289.echotrail.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.button.MaterialButton;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.model.FriendItem;

import java.util.List;

public class FriendsCustomAdapter extends ArrayAdapter<FriendItem> {
    private final LayoutInflater inflater;
    private OnFriendCallback callback;
    private final List<FriendItem> friendItemsList;

    public FriendsCustomAdapter(@NonNull Context context, int resource, @NonNull List<FriendItem> objects) {
        super(context, resource, objects);
        inflater = LayoutInflater.from(context);
        friendItemsList = objects;
    }

    public void setCallback(OnFriendCallback callback) {
        this.callback = callback;
    }

    /**
     * See if is present also pending request for this friend. In this case,
     * the request is accepted from user2 but the user1 don't know yet.
     * Don't keep this request in the list.
     *
     * @param id        id of the friend
     * @param isPending true if is a pending request, false if is a friend
     *                  In the most of the case is false for my logic.
     */
    public void remove(String id, boolean isPending) {
        for (FriendItem item : friendItemsList) {
            if (item.getUid().equals(id) && item.isOnPendingRequest() == isPending) {
                friendItemsList.remove(item);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public @NonNull View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.friend_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FriendItem item = getItem(position);
        if (item != null) {
            holder.userName.setText(item.getName());
            if (item.isOnPendingRequest()) {
                holder.allowButton.setOnClickListener((v) -> {
                    if (callback != null) {
                        callback.onAllowClick(item.getUid(), position);
                    }
                });
            } else {
                holder.allowButton.setVisibility(View.GONE);
            }
            holder.remove.setOnClickListener((v) -> {
                if (callback != null) {
                    callback.onRemoveClick(item.getUid(), position, item.isFriend());
                }
            });
        }

        return convertView;
    }

    public interface OnFriendCallback {
        void onAllowClick(String friendId, int position);

        void onRemoveClick(String friendId, int position, boolean isFriends);
    }

    static class ViewHolder {
        private final TextView userName;
        private final MaterialButton allowButton;
        private final ImageView remove;

        ViewHolder(View view) {
            userName = view.findViewById(R.id.friend_name);
            allowButton = view.findViewById(R.id.accept_button);
            remove = view.findViewById(R.id.remove_button);
        }
    }
}
