package com.peppe289.echotrail.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.model.NoteItem;

import java.util.List;

public class CardItemAdapter extends ArrayAdapter<NoteItem> {
    private final LayoutInflater inflater;
    private final List<NoteItem> items;
    @Nullable
    private final CardClickCallback callback;

    public CardItemAdapter(@NonNull Context context, int resourceId, @NonNull List<NoteItem> objects, @Nullable CardClickCallback callback) {
        super(context, resourceId, objects);
        inflater = LayoutInflater.from(context);
        this.items = objects;
        this.callback = callback;
    }

    @Override
    public void add(@Nullable NoteItem object) {
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
        NoteItem item = getItem(position);
        if (item != null) {
            viewHolder.title.setText(item.getAuthor());
            viewHolder.description.setText(item.getDescription());
            viewHolder.city.setText(item.getCity());
            viewHolder.date.setText(item.getDate());

            if (!item.isDedicated()) {
                viewHolder.dedicatedTag.setVisibility(View.GONE);
            }

            // because MaterialCardView is used for click event,
            // I can't use the setOnItemClickListener in listView.
            // And yeah ignore event and return false not work. DON'T
            // CHANGE THIS AND DON'T USE ITEM CLICK EVENT IN LISTVIEW.
            clickAdapter((MaterialCardView) convertView, item.getUserId());
        }

        return convertView;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void clickAdapter(MaterialCardView card, String userID) {
        TextView textView = card.findViewById(R.id.content);
        ImageView imageView = card.findViewById(R.id.expand_btn);
        if (callback != null) {
            imageView.setOnTouchListener((v, event) -> {
                onTouchExpandEvent(imageView, event, textView);
                return true;
            });

            card.setOnClickListener(v -> callback.onClick(userID));
        } else {
            card.setOnTouchListener((v, event) -> {
                onTouchExpandEvent(imageView, event, textView);
                return false;
            });
        }
    }

    private void onTouchExpandEvent(ImageView imageView, MotionEvent event, TextView textView) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (textView.getMaxLines() != Integer.MAX_VALUE) {
                textView.setMaxLines(Integer.MAX_VALUE);
            } else {
                textView.setMaxLines(1);
            }

            imageView.setRotation(imageView.getRotation() + 180);
        }
    }

    public List<NoteItem> getItems() {
        return items;
    }

    @Override
    public void remove(@Nullable NoteItem object) {
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
        final TextView dedicatedTag;

        ViewHolder(View view) {
            title = view.findViewById(R.id.authors);
            description = view.findViewById(R.id.content);
            city = view.findViewById(R.id.city);
            date = view.findViewById(R.id.date);
            dedicatedTag = view.findViewById(R.id.is_dedicata);
        }
    }
}
