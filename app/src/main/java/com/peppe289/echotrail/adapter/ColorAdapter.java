package com.peppe289.echotrail.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.peppe289.echotrail.R;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private final List<Integer> colorList;
    private final OnColorClickListener listener;

    public ColorAdapter(List<Integer> colorList, OnColorClickListener listener) {
        this.colorList = colorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        int color = colorList.get(position);

        // ✅ Fix: Cambiamo il colore usando backgroundTintList
        holder.colorView.setBackgroundTintList(ColorStateList.valueOf(color));

        holder.itemView.setOnClickListener(v -> listener.onColorClick(color, position));
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public interface OnColorClickListener {
        void onColorClick(int color, int index);
    }

    public static class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorView;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.colorView);
        }
    }
}
