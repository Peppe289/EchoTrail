package com.peppe289.echotrail;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    // Data and adapter
    private final List<SuggestionsAdapter.CityProprieties> suggestions = new ArrayList<>();
    // Handlers and helpers
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    // UI components
    private com.google.android.material.search.SearchView searchView;
    private com.google.android.material.search.SearchBar searchBar;
    private RecyclerView suggestionsList;
    private FloatingActionButton floatingActionButton;
    private SuggestionsAdapter adapter;
    private MapHelper mapHelper;
    private LocationHelper locationHelper;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        initializeUI(view);
        initializeHelpers();
        requestLocationPermission();
        fetchNotes();

        return view;
    }

    // Initialize UI components
    private void initializeUI(View view) {
        // Floating Action Button setup
        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(e -> MoveActivity.addActivity(getActivity(), AddNotesActivity.class));

        // Map setup
        MapView mapView = view.findViewById(R.id.map);
        mapHelper = new MapHelper(mapView);
        mapHelper.initializeMap(requireContext());

        // Suggestions list setup
        suggestionsList = view.findViewById(R.id.suggestions_list);
        suggestionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SuggestionsAdapter(suggestions, this::onSuggestionSelected);
        suggestionsList.setAdapter(adapter);

        // SearchView and SearchBar setup
        searchView = view.findViewById(R.id.search_view);
        searchBar = view.findViewById(R.id.search_bar);
        setupSearchView();
    }

    // Setup SearchView behavior
    private void setupSearchView() {
        searchView.addTransitionListener((sView, oldState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWN) {
                suggestionsList.setVisibility(View.VISIBLE);
            } else if (newState == SearchView.TransitionState.HIDDEN) {
                suggestionsList.setVisibility(View.GONE);
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
                handleSearchQuery(s.toString());
            }
        });
    }

    // Initialize helpers and request permission
    private void initializeHelpers() {
        locationHelper = new LocationHelper(requireContext());
    }

    private void requestLocationPermission() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                setDefaultLocation();
            } else {
                Toast.makeText(requireContext(), "Permesso alla posizione negato!", Toast.LENGTH_SHORT).show();
            }
        });

        locationHelper.requestLocationPermission(requestPermissionLauncher);
    }

    // Fetch notes and add markers
    private void fetchNotes() {
        NotesController.getAllNotes(documentSnapshot -> {
            com.google.firebase.firestore.GeoPoint coordinates = documentSnapshot.getGeoPoint("coordinates");
            String userID = UserController.getUid();
            String noteUserID = documentSnapshot.getString("userId");

            if (coordinates == null || userID.equals(noteUserID)) return;

            mapHelper.addMarker(new GeoPoint(coordinates.getLatitude(), coordinates.getLongitude()), documentSnapshot.getId(), (markerCounter, point) -> {
                GeoPoint clickedPoint = new GeoPoint(point.getLatitude(), point.getLongitude());

                // Handle marker click
                List<GeoPoint> points = new ArrayList<>(markerCounter.keySet());
                for (GeoPoint geoPoint : points) {
                    for (String id : markerCounter.get(geoPoint)) {
                        if (MapHelper.arePointsClose(geoPoint, clickedPoint)) {
                            Log.i("MapFragment", "Note ID: " + id);
                        }
                    }
                }
                return true;
            });
        });
    }

    // Handle search query
    private void handleSearchQuery(String query) {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
        }

        scheduledFuture = executorService.schedule(() -> MapHelper.fetchSuggestions(query, new MapHelper.OnFetchSuggestions() {
            @Override
            public void onFetchSuggestions(String responseBody) throws JSONException {
                processSuggestionsResponse(responseBody);
            }

            @Override
            public void onErrorMessage(String error) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
            }
        }), 300, TimeUnit.MILLISECONDS);
    }


    // Process suggestions response
    private void processSuggestionsResponse(String responseBody) throws JSONException {
        JSONArray results = new JSONArray(responseBody);
        suggestions.clear();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String displayName = result.getString("display_name");
            suggestions.add(new SuggestionsAdapter.CityProprieties(displayName, result.getDouble("lat"), result.getDouble("lon")));
        }

        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    // Set default location
    private void setDefaultLocation() {
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

    // Handle suggestion selection
    private void onSuggestionSelected(String cityName, double latitude, double longitude) {
        searchBar.setText(cityName);
        searchView.hide();
        suggestionsList.setVisibility(View.GONE);
        mapHelper.setMapView(new GeoPoint(latitude, longitude));
    }
}
