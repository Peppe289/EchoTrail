package com.peppe289.echotrail.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.peppe289.echotrail.AddNotesActivity;
import com.peppe289.echotrail.FriendsActivity;
import com.peppe289.echotrail.NotesListActivity;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.utils.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final ScheduledExecutorService esFetchNotes = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService esUpdateGPS = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    // UI components
    private com.google.android.material.search.SearchView searchView;
    private com.google.android.material.search.SearchBar searchBar;
    private RecyclerView suggestionsList;
    private FloatingActionButton addNewNoteFloatingBtn;
    private FloatingActionButton updatePositionFloatingBtn;
    private SuggestionsAdapter adapter;
    private MapHelper mapHelper;
    private LocationHelper locationHelper;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ScheduledFuture<?> sFetchNotes;
    private ScheduledFuture<?> sUpdateGPS;
    private boolean isFABOpen = false;
    private ExtendedFloatingActionButton publicNotesBtn;
    private ExtendedFloatingActionButton privateNotesBtn;

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
        startFetchingNotes();
        startUpdateGPS();

        return view;
    }

    private void startUpdateGPS() {
        sUpdateGPS = esUpdateGPS.scheduleWithFixedDelay(() -> locationHelper.getCurrentLocation(requireContext(), requireActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onLocationUpdated(GeoPoint location) {
                mapHelper.setMapCenter(location, false);
            }

            @Override
            public void onLocationError(ErrorType errorType) {
                Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
            }
        }), 0, 5, TimeUnit.SECONDS);
    }

    private void startFetchingNotes() {
        sFetchNotes = esFetchNotes.scheduleWithFixedDelay(this::fetchNotes, 0, 4, TimeUnit.SECONDS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sFetchNotes != null && !sFetchNotes.isCancelled()) {
            sFetchNotes.cancel(true);
        }
        esFetchNotes.shutdown();

        if (sUpdateGPS != null && !sUpdateGPS.isCancelled()) {
            sUpdateGPS.cancel(true);
        }
        esUpdateGPS.shutdown();
    }

    // Initialize UI components
    private void initializeUI(View view) {
        privateNotesBtn = view.findViewById(R.id.privateNoteBtn);
        publicNotesBtn = view.findViewById(R.id.publicNoteBtn);

        // AppBarLayout setup
        // IDK why with material 3 the app bar scroll up and go outside of screen. Here the fix from stackoverflow <3
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.findViewById(R.id.appBarLayout).getLayoutParams();
        params.setBehavior(new AppBarLayout.Behavior());
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        assert behavior != null; // This is always true
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });

        publicNotesBtn.setOnClickListener(e -> MoveActivity.addActivity(getActivity(), AddNotesActivity.class, null));
        privateNotesBtn.setOnClickListener(e -> MoveActivity.addActivity(getActivity(), FriendsActivity.class, null));

        // Floating Action Button setup
        addNewNoteFloatingBtn = view.findViewById(R.id.addNewNoteBtn);
        addNewNoteFloatingBtn.setOnClickListener(e -> {
            // from docs this allows to animate by default when use show()
            // and hide() method.
            publicNotesBtn.setAnimateShowBeforeLayout(true);
            privateNotesBtn.setAnimateShowBeforeLayout(true);

            isFABOpen = !isFABOpen;
            if (isFABOpen) {
                addNewNoteFloatingBtn.animate().
                        setInterpolator(null).
                        setListener(null).
                        rotation(45f).
                        withLayer().
                        setDuration(300).
                        withStartAction(() -> {
                            privateNotesBtn.show();
                            publicNotesBtn.show();
                        }).start();
            } else {
                addNewNoteFloatingBtn.animate().
                        setInterpolator(null).
                        setListener(null).
                        rotation(0).
                        withLayer().
                        setDuration(300).
                        withStartAction(() -> {
                            privateNotesBtn.hide();
                            publicNotesBtn.hide();
                        }).start();
            }
        });

        // Set current position button setup
        updatePositionFloatingBtn = view.findViewById(R.id.setCurrentPositionBtn);
        updatePositionFloatingBtn.setOnClickListener(e -> setCurrentLocation());

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
                addNewNoteFloatingBtn.hide();
                updatePositionFloatingBtn.hide();
            } else if (newState == SearchView.TransitionState.HIDDEN) {
                suggestionsList.setVisibility(View.GONE);
                addNewNoteFloatingBtn.show();
                updatePositionFloatingBtn.show();
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
                            Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (fineLocationGranted != null && fineLocationGranted) {
                        setCurrentLocation();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance("Permesso di localizzazione",
                                "Per utilizzare l'applicazione non è sufficiente utilizzare il permesso di localizzazione approssimativa.");
                        bottomSheetFragment.show(requireActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
                    } else {
                        Toast.makeText(requireContext(),
                                ErrorType.POSITION_PERMISSION_ERROR.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        locationHelper.requestLocationPermission(requestPermissionLauncher);
    }

    // Fetch notes and add markers
    @SuppressLint("NewApi")
    private void fetchNotes() {
        NotesController.getAllNotes(querySnapshot -> {
            if (querySnapshot == null || querySnapshot.isEmpty()) return;
            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                com.google.firebase.firestore.GeoPoint coordinates = documentSnapshot.getGeoPoint("coordinates");
                String userID = UserController.getUid();
                String noteUserID = documentSnapshot.getString("userId");

                // Skip if coordinates are null or note belongs to the current user
                if (coordinates == null || userID.equals(noteUserID)) continue;

                try {
                    // this note isn't for me, skip...
                    String isFor = documentSnapshot.getString("send_to");
                    if (isFor != null && !isFor.equals(userID)) continue;
                } catch (Exception ignored) {
                }

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
                                BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance("Camminare fa bene!",
                                        "Raggiungi il luogo per leggere la nota.");
                                bottomSheetFragment.show(requireActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());}
                        }

                        @Override
                        public void onLocationError(ErrorType errorType) {
                            Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
                        }
                    });

                    return true;
                });
            }
        });
    }

    /**
     * Launches the ReadNotesActivity with the given note IDs.
     */
    private void launchReadNotesActivity(List<String> noteIDs) {
        for (String noteID : noteIDs) {
            NotesController.updateReadNotesList(noteID);
        }

        MoveActivity.addActivity(requireActivity(), NotesListActivity.class, intent -> intent.putStringArrayListExtra("notes", new ArrayList<>(noteIDs)));
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
            public void onErrorMessage(ErrorType errorType) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                        errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show());
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
    private void setCurrentLocation() {
        locationHelper.getCurrentLocation(requireContext(), requireActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onLocationUpdated(GeoPoint location) {
                mapHelper.setMapCenter(location);
            }

            @Override
            public void onLocationError(ErrorType errorType) {
                Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
                mapHelper.setDefaultCenter(); // Set default position.
            }
        });
    }

    // Handle suggestion selection
    private void onSuggestionSelected(String cityName, double latitude, double longitude) {
        searchBar.setText(cityName);
        searchView.hide();
        addNewNoteFloatingBtn.show();
        updatePositionFloatingBtn.show();
        suggestionsList.setVisibility(View.GONE);
        mapHelper.setMapView(new GeoPoint(latitude, longitude));
    }
}
