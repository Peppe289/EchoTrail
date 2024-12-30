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

/**
 * A utility class for handling location-related functionalities in Android.
 * <p>
 * This class leverages Google Play Services' {@link FusedLocationProviderClient} for obtaining
 * the user's current location and provides additional utilities like requesting location
 * permissions and converting coordinates to city names.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *     <li>Handles runtime permissions for location access.</li>
 *     <li>Fetches the user's last known location with error handling.</li>
 *     <li>Supports conversion of geographic coordinates to human-readable city names using {@link Geocoder}.</li>
 *     <li>Provides a callback interface for location updates and errors.</li>
 * </ul>
 * </p>
 */
public class LocationHelper {
    private final FusedLocationProviderClient fusedLocationClient;

    /**
     * Constructs a new {@link LocationHelper} instance.
     *
     * @param context the application context used to initialize the location client
     */
    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Converts geographic coordinates (latitude and longitude) into a human-readable city name.
     * <p>
     * This method uses the {@link Geocoder} class to fetch address information for the provided
     * coordinates. If no city name is found, or if an error occurs, it returns {@code null}.
     * </p>
     *
     * @param context   the application context for accessing system services
     * @param latitude  the latitude of the location
     * @param longitude the longitude of the location
     * @return the city name corresponding to the provided coordinates, or {@code null} if unavailable
     */
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

    /**
     * Requests location permissions from the user if not already granted.
     *
     * @param activity the current activity context used to request permissions
     */
    public void requestLocationPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    /**
     * Retrieves the user's current location asynchronously.
     * <p>
     * This method checks for location permissions and invokes the appropriate callback methods
     * in the provided {@link LocationCallback} implementation.
     * </p>
     *
     * @param context          the application context for permission checks
     * @param activity         the activity context for the location client
     * @param locationCallback the callback to handle location updates or errors
     */
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

    /**
     * A callback interface for handling location updates and errors.
     * <p>
     * Implement this interface to define custom behavior for successful location retrievals
     * and error handling scenarios.
     * </p>
     */
    public interface LocationCallback {
        /**
         * Called when the location is successfully retrieved.
         *
         * @param location a {@link GeoPoint} representing the user's current location
         */
        void onLocationUpdated(GeoPoint location);

        /**
         * Called when an error occurs while retrieving the location.
         *
         * @param error a descriptive error message
         */
        void onLocationError(String error);
    }
}
