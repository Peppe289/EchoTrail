package com.peppe289.echotrail.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.model.CardItem;

import java.util.List;

public class CardItemAdapter extends ArrayAdapter<CardItem> {
    private final LayoutInflater inflater;
    private final List<CardItem> items;
    @Nullable
    private final CardClickCallback callback;

    public CardItemAdapter(@NonNull Context context, int resourceId, @NonNull List<CardItem> objects, @Nullable CardClickCallback callback) {
        super(context, resourceId, objects);
        inflater = LayoutInflater.from(context);
        this.items = objects;
        this.callback = callback;
    }

    @Override
    public void add(@Nullable CardItem object) {
        super.add(object);
        // add here isn't necessary because from default the add function add item to the list
        //items.add(object);
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.card_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Bind the data
        CardItem item = getItem(position);
        if (item != null) {
            viewHolder.title.setText(item.getAuthor());
            viewHolder.description.setText(item.getDescription());
            viewHolder.city.setText(item.getCity());
            viewHolder.date.setText(item.getDate());

            if (callback != null) {
                clickAdapter((MaterialCardView) convertView, item.getUserId());
            }
        }

        return convertView;
    }

    public void clickAdapter(MaterialCardView card, String userID) {
        card.setOnClickListener(v ->
        {
            // this should not be never null!!
            assert callback != null;
            callback.onClick(userID);
        });
    }

    public List<CardItem> getItems() {
        return items;
    }

    @Override
    public void remove(@Nullable CardItem object) {
        super.remove(object);
        items.remove(object);
    }

    public interface CardClickCallback {
        void onClick(String userID);
    }

    // ViewHolder class
    static class ViewHolder {
        final TextView title;
        final TextView description;
        final TextView city;
        final TextView date;

        ViewHolder(View view) {
            title = view.findViewById(R.id.authors);
            description = view.findViewById(R.id.content);
            city = view.findViewById(R.id.city);
            date = view.findViewById(R.id.date);
        }
    }
}
