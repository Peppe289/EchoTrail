package com.peppe289.echotrail;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.Manifest;

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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
@SuppressWarnings("FieldCanBeLocal")
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
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

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
        floatingActionButton.setOnClickListener(e -> MoveActivity.addActivity(getActivity(), AddNotesActivity.class, null));

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
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts
                        .RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_COARSE_LOCATION,false);
                    if (fineLocationGranted != null && fineLocationGranted) {
                        setDefaultLocation();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        Toast.makeText(requireContext(), "Permesso di localizzazione approssimata concesso", Toast.LENGTH_SHORT).show();
                        setDefaultLocation();
                    } else {
                        Toast.makeText(requireContext(), "Permesso di localizzazione non concesso", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        locationHelper.requestLocationPermission(requestPermissionLauncher);
    }

    // Fetch notes and add markers
    @SuppressLint("NewApi")
    private void fetchNotes() {
        NotesController.getAllNotes(documentSnapshot -> {
            com.google.firebase.firestore.GeoPoint coordinates = documentSnapshot.getGeoPoint("coordinates");
            String userID = UserController.getUid();
            String noteUserID = documentSnapshot.getString("userId");

            // Skip if coordinates are null or note belongs to the current user
            if (coordinates == null || userID.equals(noteUserID)) return;

            GeoPoint noteLocation = new GeoPoint(coordinates.getLatitude(), coordinates.getLongitude());

            mapHelper.addMarker(noteLocation, documentSnapshot.getId(), (markerCounter, point) -> {
                GeoPoint clickedPoint = new GeoPoint(point.getLatitude(), point.getLongitude());

                // Preliminary filtering of nearby markers
                 List<Map.Entry<GeoPoint, List<String>>> nearbyMarkers = markerCounter.entrySet().stream()
                        .filter(entry -> MapHelper.arePointsClose(entry.getKey(), clickedPoint, MapHelper.MarkerDistance.CLOSE))
                        .toList();

                // No relevant markers
                if (nearbyMarkers.isEmpty()) return true;

                locationHelper.getCurrentLocation(requireContext(), requireActivity(), new LocationHelper.LocationCallback() {
                    @Override
                    public void onLocationUpdated(GeoPoint currentLocation) {
                        List<String> readyToSeeIDs = nearbyMarkers.stream()
                                .filter(entry -> MapHelper.arePointsClose(currentLocation, entry.getKey(), MapHelper.MarkerDistance.CLOSE))
                                .flatMap(entry -> entry.getValue().stream()) // Flatten IDs
                                .distinct() // Remove duplicates
                                .toList();

                        // Launch activity if there are notes to see
                        if (!readyToSeeIDs.isEmpty()) {
                            launchReadNotesActivity(readyToSeeIDs);
                        } else {
                            Toast.makeText(requireContext(), "Raggiungi il luogo per leggere la nota!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLocationError(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });

                return true;
            });
        });
    }

    /**
     * Launches the ReadNotesActivity with the given note IDs.
     */
    private void launchReadNotesActivity(List<String> noteIDs) {
        for (String noteID : noteIDs) {
            NotesController.updateReadNotesList(noteID);
        }

        MoveActivity.addActivity(requireActivity(), ReadNotesActivity.class, intent -> {
            intent.putStringArrayListExtra("notes", new ArrayList<>(noteIDs));
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
    @SuppressLint("NotifyDataSetChanged")
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
