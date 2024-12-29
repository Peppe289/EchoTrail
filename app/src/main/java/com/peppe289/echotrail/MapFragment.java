package com.peppe289.echotrail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.auth.User;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.utils.LocationHelper;
import com.peppe289.echotrail.utils.MapHelper;
import com.peppe289.echotrail.utils.MoveActivity;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

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
        MapHelper mapHelper = new MapHelper(mapView);
        mapHelper.initializeMap(requireContext());

        LocationHelper locationHelper = new LocationHelper(requireContext());
        locationHelper.requestLocationPermission(getActivity());

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
}