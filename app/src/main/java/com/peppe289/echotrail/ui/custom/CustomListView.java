package com.peppe289.echotrail.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import com.peppe289.echotrail.adapter.NoteCustomAdapter;

public class CustomListView extends android.widget.ListView {


    public CustomListView(Context context) {
        super(context);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * @deprecated This method not work when we have MaterialCardView so,
     *              ignore this. Just put callback function when create adapter
     *              with {@link NoteCustomAdapter}.
     */
    @Override
    @Deprecated
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        // ignore this
    }
}