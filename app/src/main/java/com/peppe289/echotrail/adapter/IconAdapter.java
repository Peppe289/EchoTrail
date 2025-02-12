package com.peppe289.echotrail.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.peppe289.echotrail.R;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private Context context;
    private List<Integer> iconList;
    private OnIconClickListener listener;

    public interface OnIconClickListener {
        void onIconClick(int iconResId, int index);
    }

    public IconAdapter(Context context, List<Integer> iconList, OnIconClickListener listener) {
        this.context = context;
        this.iconList = iconList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.icon_item, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        int iconResId = iconList.get(position);
        holder.iconImage.setImageResource(iconResId);

        holder.itemView.setOnClickListener(v -> listener.onIconClick(iconResId, position));
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public static class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.iconImage);
        }
    }
}
