package com.peppe289.echotrail.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void requestLocationPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    public void getCurrentLocation(Context context, Activity activity, @NonNull LocationCallback locationCallback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationCallback.onLocationError("Location permission not granted.");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
            if (location != null) {
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                locationCallback.onLocationUpdated(startPoint);
            } else {
                locationCallback.onLocationError("Location not found.");
            }
        }).addOnFailureListener(e -> {
            locationCallback.onLocationError("Failed to get location: " + e.getMessage());
        });
    }

    public static String getCityName(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            Log.e("LocationHelper", "Error getting city name: " + e.getMessage());
        }
        return null;
    }

    public interface LocationCallback {
        void onLocationUpdated(GeoPoint location);

        void onLocationError(String error);
    }
}
