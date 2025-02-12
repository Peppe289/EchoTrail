package com.peppe289.echotrail.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.adapter.ColorAdapter;
import com.peppe289.echotrail.adapter.IconAdapter;

import java.util.Arrays;
import java.util.List;

public class IconPickerDialog {

    public static final List<Integer> iconList = List.of(
            R.drawable.user_pic_female_1,
            R.drawable.user_pic_female_2,
            R.drawable.user_pic_female_3,
            R.drawable.user_pic_female_4,
            R.drawable.user_pic_female_5,
            R.drawable.user_pic_female_6,
            R.drawable.user_pic_female_7,
            R.drawable.user_pic_female_8,
            R.drawable.user_pic_female_9
    );
    public static final List<Integer> colorList = Arrays.asList(
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN,
            Color.DKGRAY, Color.LTGRAY, Color.BLACK, Color.WHITE, Color.parseColor("#FFA500"), Color.parseColor("#800080")
    );
    private final OnFinishSelect callback;
    /* This represents the index of the image. */
    private int imageIndex;
    /* This represents the index of color as string. */
    private int colorIndex;

    public IconPickerDialog(OnFinishSelect callback) {
        super();
        this.callback = callback;
    }

    public void show(Context context) {


        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_icon_list, null);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewIcons);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Scegli un'icona")
                .setView(dialogView)
                .setNegativeButton("Annulla", (d, which) -> d.dismiss())
                .create();

        IconAdapter adapter = new IconAdapter(context, iconList, (iconResId, position) -> {
            this.imageIndex = position;
            showColorPicker(context, dialog);
        });

        recyclerView.setAdapter(adapter);
        dialog.show();
    }

    private void showColorPicker(Context context, AlertDialog dialog) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View colorView = inflater.inflate(R.layout.dialog_color_list, null);

        AlertDialog dialog2 = new MaterialAlertDialogBuilder(context)
                .setTitle("Scegli un colore")
                .setView(colorView)
                .setNegativeButton("Annulla", (d, which) -> d.dismiss())
                .create();

        RecyclerView recyclerView = colorView.findViewById(R.id.recyclerViewColors);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        recyclerView.setAdapter(new ColorAdapter(colorList, (color, index) -> {
            dialog.dismiss();
            dialog2.dismiss();
            this.colorIndex = index;
            callback.onDone(this.imageIndex, this.colorIndex);
        }));

        dialog2.show();
    }

    public interface OnFinishSelect {
        void onDone(int imageCode, int colorCode);
    }
}
