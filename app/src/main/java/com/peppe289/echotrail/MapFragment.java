package com.peppe289.echotrail;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchView;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.utils.LocationHelper;
import com.peppe289.echotrail.utils.MapHelper;
import com.peppe289.echotrail.utils.MoveActivity;
import com.peppe289.echotrail.utils.SuggestionsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private final List<SuggestionsAdapter.CityProprieties> suggestions = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    com.google.android.material.search.SearchView searchView;
    com.google.android.material.search.SearchBar searchBar;
    private RecyclerView suggestionsList;
    private SuggestionsAdapter adapter;
    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private MapHelper mapHelper;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LocationHelper locationHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(e -> MoveActivity.addActivity(getActivity(), AddNotesActivity.class));

        MapView mapView = view.findViewById(R.id.map);
        mapHelper = new MapHelper(mapView);
        mapHelper.initializeMap(requireContext());

        adapter = new SuggestionsAdapter(suggestions, this::onSuggestionSelected);
        suggestionsList = view.findViewById(R.id.suggestions_list);
        suggestionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        suggestionsList.setAdapter(adapter);

        searchView = view.findViewById(R.id.search_view);
        searchBar = view.findViewById(R.id.search_bar);

        searchView.addTransitionListener((sView, oldState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWN) {
                suggestionsList.setVisibility(View.VISIBLE);
            } else if (newState == SearchView.TransitionState.HIDDEN) {
                suggestionsList.setVisibility(View.GONE);
            }
        });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        setDefatulLocation();
                    } else {
                        Toast.makeText(requireContext(), "Permesso alla posizione negato!", Toast.LENGTH_SHORT).show();
                    }
                });

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> fetchSuggestions(query);
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });

        locationHelper = new LocationHelper(requireContext());
        locationHelper.requestLocationPermission(requestPermissionLauncher);

        NotesController.getAllNotes(documentSnapshot -> {
            com.google.firebase.firestore.GeoPoint coordinates = documentSnapshot.getGeoPoint("coordinates");
            String userID = UserController.getUid();
            String noteUserID = documentSnapshot.getString("userId");

            if (coordinates == null || userID.equals(noteUserID)) {
                return;
            }

            String title = documentSnapshot.getString("city");
            mapHelper.addMarker(new GeoPoint(coordinates.getLatitude(), coordinates.getLongitude()), title);
        });

        return view;
    }

    private void setDefatulLocation() {
        locationHelper.getCurrentLocation(requireContext(), requireActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onLocationUpdated(GeoPoint location) {
                mapHelper.setMapCenter(location);
            }

            @Override
            public void onLocationError(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                mapHelper.setDefaultCenter(); // Set default position.
            }
        });
    }

    private void fetchSuggestions(String query) {
        String url = "https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&addressdetails=1";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Errore nella richiesta", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray results = new JSONArray(responseBody);
                        suggestions.clear();
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject result = results.getJSONObject(i);
                            String displayName = result.getString("display_name");
                            suggestions.add(new SuggestionsAdapter.CityProprieties(
                                    displayName,
                                    result.getDouble("lat"),
                                    result.getDouble("lon")
                            ));
                        }

                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Errore nel parsing della risposta", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    private void onSuggestionSelected(String cityName, double latitude, double longitude) {
        searchBar.setText(cityName);
        searchView.hide();
        suggestionsList.setVisibility(View.GONE);
        mapHelper.setMapView(new GeoPoint(latitude, longitude));
    }
}