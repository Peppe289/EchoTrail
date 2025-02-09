package com.peppe289.echotrail.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.adapter.LanguageAdapter;
import com.peppe289.echotrail.ui.custom.CardListView;
import com.peppe289.echotrail.utils.LanguageUtils;

import java.util.List;
import java.util.Locale;

public class LanguagesFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_languages, container, false);
        CardListView listView = v.findViewById(R.id.languages_list);

        List<Locale> locales = LanguageUtils.getAvailableLocales();
        LanguageAdapter adapter = new LanguageAdapter(requireContext(), R.layout.language_item, locales);

        adapter.setCallback(locale -> {
            Toast.makeText(requireContext(), getString(R.string.change_language_success), Toast.LENGTH_SHORT).show();
            LanguageUtils.setAppLanguage(requireContext(), locale.getLanguage());
            requireActivity().recreate();
        });

        listView.setAdapter(adapter);

        return v;
    }
}
