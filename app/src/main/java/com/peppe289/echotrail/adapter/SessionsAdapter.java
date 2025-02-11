package com.peppe289.echotrail.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.firebase.Timestamp;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.model.Session;
import com.peppe289.echotrail.utils.UniqueIDHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionsAdapter extends ArrayAdapter<Session> {

    private final LayoutInflater inflater;

    public SessionsAdapter(@NonNull Context context, int resourceId, @NonNull List<Session> objects) {
        super(context, resourceId, objects);
        inflater = LayoutInflater.from(context);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.session_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Session session = getItem(position);
        if (session != null) {
            viewHolder.coordinate.setText(getContext().getString(R.string.position) + ": " + session.getPosition());
            viewHolder.android_version.setText("Android: " + session.getVersion());
            viewHolder.last_access_time.setText(formatDate(session.getTime()));
            if (UniqueIDHelper.getUniqueID(getContext()).compareTo(session.getId()) == 0)
                viewHolder.device.setText("(" + getContext().getString(R.string.current_session) + ") " + session.getDevice());
            else
                viewHolder.device.setText(session.getDevice());
        }

        return convertView;
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    static class ViewHolder {
        final TextView device;
        final TextView android_version;
        final TextView coordinate;
        final TextView last_access_time;

        ViewHolder(View view) {
            device = view.findViewById(R.id.device_name);
            android_version = view.findViewById(R.id.android_version);
            coordinate = view.findViewById(R.id.coordinate);
            last_access_time = view.findViewById(R.id.last_access_time);
        }
    }
}
