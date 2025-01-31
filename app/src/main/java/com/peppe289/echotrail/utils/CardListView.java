package com.peppe289.echotrail.utils;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;

public class CardListView extends android.widget.ListView {


    public CardListView(Context context) {
        super(context);
    }

    public CardListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CardListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * @deprecated This method not work when we have MaterialCardView so,
     *              ignore this. Just put callback function when create adapter
     *              with {@link CardItemAdapter}.
     */
    @Override
    @Deprecated
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        // ignore this
    }
}