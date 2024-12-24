package com.peppe289.echotrail;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.utils.MoveActivity;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);
        Button logoutBtn = rootView.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(view -> {
            UserController.logout();
            MoveActivity.rebaseActivity(getActivity(), MainActivity.class);
            Log.i("AccountFragment", "User logged out");
        });
        return rootView;
    }
}