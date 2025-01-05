package com.peppe289.echotrail.utils;

import android.view.View;
import android.widget.FrameLayout;

import com.peppe289.echotrail.R;

public class LoadingManager {
    private final FrameLayout loadingOverlay;

    public LoadingManager(View rootView) {
        loadingOverlay = rootView.findViewById(R.id.loading_overlay);
    }

    public void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
}