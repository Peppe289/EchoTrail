package com.peppe289.echotrail.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.peppe289.echotrail.R;

import org.json.JSONException;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A utility class for managing and customizing a {@link MapView} instance using the OSMdroid library.
 * <p>
 * This class provides a set of methods to initialize and configure the map, manage markers,
 * and handle user interactions. It supports adding multiple markers, clustering markers
 * that are close to each other, and customizing marker icons to display a count of markers
 * at the same location.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *     <li>Initialization of the map with multi-touch controls and predefined zoom levels.</li>
 *     <li>Setting the map center to a specific geographic location.</li>
 *     <li>Adding markers dynamically to the map, with clustering functionality for markers
 *     close to each other.</li>
 *     <li>Custom marker icons with dynamic content (e.g., displaying a number).</li>
 *     <li>Support for default map centering and custom overlays.</li>
 * </ul>
 * </p>
 */
public class MapHelper {
    private final MapView mapView;
    private final HashMap<GeoPoint, List<String>> markerCounts;
    private Marker marker;
    private Context context;
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Constructs a new instance of the {@link MapHelper} class.
     *
     * @param mapView the {@link MapView} instance that this helper will manage
     */
    public MapHelper(MapView mapView) {
        this.mapView = mapView;
        this.marker = null;
        this.markerCounts = new HashMap<>();
    }

    /**
     * Initializes the {@link MapView} with default settings such as tile source, zoom levels,
     * and multi-touch controls.
     *
     * @param context the application context, used for configuration
     */
    public void initializeMap(Context context) {
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        this.context = context;
        mapView.setTileSource(TileSourceFactory.OpenTopo);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.setMinZoomLevel(6.0);
        mapView.setMaxZoomLevel(25.0);
    }

    /**
     * Sets the center of the map to the specified geographic point and optionally adds a marker
     * at that location labeled as "La mia posizione".
     *
     * @param point the {@link GeoPoint} to center the map on
     */
    public void setMapCenter(GeoPoint point, boolean setView) {
        if (mapView != null) {
            if (setView) mapView.getController().setCenter(point);
            if (marker == null) {
                marker = new Marker(mapView);
                marker.setTitle("La mia posizione");
                marker.setPosition(point);
                mapView.getOverlays().add(marker);
            } else {
                marker.setPosition(point);
            }
        }
    }

    public void setMapCenter(GeoPoint point) {
        setMapCenter(point, true);
    }

    /**
     * Centers the map view on the given geographic point without adding or modifying markers.
     *
     * @param point the {@link GeoPoint} to center the map view on
     */
    public void setMapView(GeoPoint point) {
        if (mapView != null) {
            mapView.getController().animateTo(point);
        }
    }

    /***
     * This call back should be have list of all notes and GeoPoint for actual note.
     * This can be usefully used to show all notes in default range.
     */
    public interface OnMarkerClickListener {
        boolean onMarkerClick(HashMap<GeoPoint, List<String>> markerCounts, GeoPoint point);
    }

    /**
     * Adds a marker at the specified geographic point with the given title. If a marker already
     * exists near the specified point, it increments a counter on the existing marker instead of
     * adding a new one.
     *
     * @param point the {@link GeoPoint} where the marker should be added
     * @param noteID the ID of the note associated with the marker
     * @param callback the callback to be invoked when the marker is clicked.
     *                 This should be open new activity for show all details of notes.
     */
    public void addMarker(GeoPoint point, String noteID, OnMarkerClickListener callback) {
        if (mapView != null) {
            GeoPoint matchedPoint = null;
            for (GeoPoint existingPoint : markerCounts.keySet()) {
                if (arePointsClose(existingPoint, point)) {
                    matchedPoint = existingPoint;
                    break;
                }
            }

            if (matchedPoint != null) {
                // here should not be never null
                List<String> notesID = markerCounts.get(matchedPoint);

                assert notesID != null; // this should never be null
                if (notesID.contains(noteID))
                    return;

                int count = Objects.requireNonNull(notesID).size() + 1;
                notesID.add(noteID);
                markerCounts.put(matchedPoint, notesID);

                updateMarkerIcon(matchedPoint, count);
            } else {
                Marker newMarker = new Marker(mapView);
                newMarker.setPosition(point);
                newMarker.setOnMarkerClickListener((marker, mapView) -> callback.onMarkerClick(markerCounts, point));
                newMarker.setIcon(createMarkerWithNumber(1));
                mapView.getOverlays().add(newMarker);
                List<String> notesID = new ArrayList<>();
                notesID.add(noteID);
                markerCounts.put(point, notesID);
            }
            mapView.invalidate();
        }
    }

    /**
     * Updates the icon of an existing marker to display a count of markers at its location.
     *
     * @param point the {@link GeoPoint} of the marker to update
     * @param count the number to display on the marker
     */
    private void updateMarkerIcon(GeoPoint point, int count) {
        for (Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof Marker) {
                Marker marker = (Marker) overlay;
                if (arePointsClose(marker.getPosition(), point)) {
                    marker.setIcon(createMarkerWithNumber(count));
                }
            }
        }
    }

    public static void fetchSuggestions(String query, OnFetchSuggestions callback) {
        String url = "https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&addressdetails=1";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onErrorMessage("Errore nella richiesta");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() == null ? "" : response.body().string();
                    try {
                        callback.onFetchSuggestions(responseBody);
                    } catch (Exception e) {
                        callback.onErrorMessage("Errore nel parsing della risposta");
                    }
                }
            }
        });
    }

    public interface OnFetchSuggestions {
        void onFetchSuggestions(String responseBody) throws JSONException;
        void onErrorMessage(String error);
    }

    /**
     * Creates a custom Drawable for a marker, displaying the specified number.
     *
     * @param number the number to display on the marker
     * @return a {@link Drawable} representing the marker icon
     */
    private Drawable createMarkerWithNumber(int number) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View markerView = inflater.inflate(R.layout.custom_mark, null);

        TextView markerNumber = markerView.findViewById(R.id.marker_number);
        markerNumber.setText(String.valueOf(number));

        markerView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public enum MarkerDistance {
        CLOSE,
        TOO_CLOSE,
    }

    /**
     * Determines if two geographic points are close to each other within a threshold distance.
     *
     * @param p1 the first {@link GeoPoint}
     * @param p2 the second {@link GeoPoint}
     * @return true if the points are within a 3-meter distance, false otherwise
     */
    public static boolean arePointsClose(GeoPoint p1, GeoPoint p2) {
        return arePointsClose(p1, p2, MarkerDistance.TOO_CLOSE);
    }

    /**
     * Determines if two geographic points are close to each other within a threshold distance.
     *
     * @param p1 the first {@link GeoPoint}
     * @param p2 the second {@link GeoPoint}
     * @param markerDistance the distance threshold to use for comparison
     * @return true if the points are within a 3-meter or 100-meter distance, false otherwise
     */
    public static boolean arePointsClose(GeoPoint p1, GeoPoint p2, MarkerDistance markerDistance) {
        double distance = p1.distanceToAsDouble(p2);
        return markerDistance == MarkerDistance.TOO_CLOSE ? distance < 3 : distance < 100;
    }

    /**
     * Sets the map's center to a default location (Rome, Italy).
     */
    public void setDefaultCenter() {
        setMapCenter(new GeoPoint(41.8919, 12.5113));
    }
}
