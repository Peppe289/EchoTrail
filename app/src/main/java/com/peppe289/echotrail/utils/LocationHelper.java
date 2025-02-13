package com.peppe289.echotrail.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.peppe289.echotrail.controller.callback.LocationCallback;
import com.peppe289.echotrail.utils.callback.HelperCallback;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import static android.content.Context.LOCATION_SERVICE;

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
     * @param callback  the callback to handle the city name or error
     */
    public static void getCityName(Context context, double latitude, double longitude, HelperCallback<String, ErrorType> callback) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addresses = null;
        // for android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // idk why but seems this is needed
            geocoder.getFromLocation(latitude, longitude, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    try {
                        callback.onSuccess(addresses.get(0).getLocality());
                    } catch (Exception e) {
                        callback.onSuccess("");
                    }
                }
            });
        } else {
            // only for android 12 and 12L
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                callback.onError(ErrorType.POSITION_NOT_FOUND_ERROR);
            }

            if (addresses != null && !addresses.isEmpty()) {
                callback.onSuccess(addresses.get(0).getLocality());
            } else {
                callback.onSuccess("");
            }
        }
    }

    /**
     * Requests location permissions from the user if not already granted.
     *
     * @param requestPermissionLauncher the launcher for requesting location permissions
     */
    public void requestLocationPermission(ActivityResultLauncher<String[]> requestPermissionLauncher) {
        requestPermissionLauncher.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    public boolean locationPermissionIsGranted(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
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
    public void getCurrentLocation(Context context, Activity activity, @NonNull LocationCallback<GeoPoint, ErrorType> locationCallback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationCallback.onError(ErrorType.POSITION_PERMISSION_ERROR);
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(activity, location -> {
            if (location != null && location.isMock()) {
                locationCallback.onError(ErrorType.POSITION_MOCK_ERROR);
            } else if (location != null) {
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                locationCallback.onSuccess(startPoint);
            } else {
                locationCallback.onError(ErrorType.POSITION_NOT_FOUND_ERROR);
            }
        }).addOnFailureListener(e -> locationCallback.onError(ErrorType.UNKNOWN_ERROR));
    }
}
