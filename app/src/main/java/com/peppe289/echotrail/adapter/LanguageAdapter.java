package com.peppe289.echotrail.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.peppe289.echotrail.R;

import javax.security.auth.callback.Callback;
import java.util.List;
import java.util.Locale;

public class LanguageAdapter extends ArrayAdapter<Locale> {
    private final LayoutInflater inflater;
    private int selectedPosition = -1;
    private ChangedLanguage setCallback;

    public LanguageAdapter(@NonNull Context context, int resource, @NonNull List<Locale> objects) {
        super(context, resource, objects);
        inflater = LayoutInflater.from(context);
    }

    public void setCallback(ChangedLanguage callback) {
        this.setCallback = callback;
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        View.OnClickListener callback = v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            if (setCallback != null) {
                setCallback.onChangedLanguage(getItem(position));
            }
        };

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.language_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Locale item = getItem(position);
        if (item != null) {
            viewHolder.lang.setText(item.getDisplayLanguage());
            viewHolder.radioButton.setChecked(position == selectedPosition);
            viewHolder.radioButton.setOnClickListener(callback);
            viewHolder.layout.setOnClickListener(callback);
        }

        return convertView;
    }

    public interface ChangedLanguage {
        public void onChangedLanguage(Locale locale);
    }

    static class ViewHolder {
        final ConstraintLayout layout;
        final TextView lang;
        final RadioButton radioButton;

        ViewHolder(View view) {
            layout = view.findViewById(R.id.languageItem);
            lang = view.findViewById(R.id.languageName);
            radioButton = view.findViewById(R.id.radioButton);
        }
    }
}
