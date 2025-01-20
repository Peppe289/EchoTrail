package com.peppe289.echotrail.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.peppe289.echotrail.R;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;

public class UserLinksAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;
    private final List<String> links;

    public UserLinksAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        links = objects;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.personal_link_row, parent, false);
            viewHolder = convertView.findViewById(R.id.link_text_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TextView) convertView.getTag();
        }

        String link = getItem(position);
        if (link != null) {
            viewHolder.setText(link);
        }

        return convertView;
    }
}
