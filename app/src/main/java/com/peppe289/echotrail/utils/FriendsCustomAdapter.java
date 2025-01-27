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

    public FriendsCustomAdapter(@NonNull Context context, int resource, @NonNull List<FriendItem> objects) {
        super(context, resource, objects);
        inflater = LayoutInflater.from(context);
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

                });
            } else {
                holder.allowButton.setVisibility(View.GONE);
            }
            holder.remove.setOnClickListener((v) -> {

            });
        }

        return convertView;
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
