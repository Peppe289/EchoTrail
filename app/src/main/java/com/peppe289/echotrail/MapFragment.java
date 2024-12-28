package com.peppe289.echotrail;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.peppe289.echotrail.utils.MoveActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        requestLocationPermission();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(e -> MoveActivity.addActivity(getActivity(), AddNotesActivity.class));

        initializeMap(view);
        return view;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapView.getController().setCenter(startPoint);
                Toast.makeText(requireContext(), "Location found at: " + location.getLatitude() + " : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
                // set default if location not found. (should be Rome)
                mapView.getController().setCenter(new GeoPoint(41.8919, 12.5113));
            }
        });
    }

    public void initializeMap(View view) {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.OpenTopo);


        // enable pinch-to-zoom
        mapView.setMultiTouchControls(true);

        mapView.getController().setZoom(15.0);
        mapView.setMinZoomLevel(6.0);
        mapView.setMaxZoomLevel(25.0);

        getCurrentLocation();
    }
}