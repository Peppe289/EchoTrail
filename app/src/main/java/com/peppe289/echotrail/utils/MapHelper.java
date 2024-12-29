package com.peppe289.echotrail.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.peppe289.echotrail.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.HashMap;

public class MapHelper {
    private final MapView mapView;
    private Marker marker;
    private final HashMap<GeoPoint, Integer> markerCounts;
    private Context context;

    public MapHelper(MapView mapView) {
        this.mapView = mapView;
        marker = null;
        this.markerCounts = new HashMap<>();
    }

    public void initializeMap(Context context) {
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        this.context = context;
        mapView.setTileSource(TileSourceFactory.OpenTopo);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.setMinZoomLevel(6.0);
        mapView.setMaxZoomLevel(25.0);
    }

    public void setMapCenter(GeoPoint point) {
        if (mapView != null) {
            mapView.getController().setCenter(point);
            if (marker == null) {
                marker = new Marker(mapView);
                marker.setTitle("La mia posizione");
                //marker.setIcon(mapView.getContext().getDrawable(android.R.drawable.ic_menu_mylocation));
                //marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setPosition(point);
                mapView.getOverlays().add(marker);
            } else {
                marker.setPosition(point);
            }
        }
    }

    public void addMarker(GeoPoint point, String title) {
        if (mapView != null) {
            GeoPoint matchedPoint = null;
            for (GeoPoint existingPoint : markerCounts.keySet()) {
                if (arePointsClose(existingPoint, point)) {
                    matchedPoint = existingPoint;
                    break;
                }
            }

            if (matchedPoint != null) {
                int count = markerCounts.get(matchedPoint) + 1;
                markerCounts.put(matchedPoint, count);

                updateMarkerIcon(matchedPoint, count);
            } else {
                Marker newMarker = new Marker(mapView);
                newMarker.setTitle(title);
                newMarker.setPosition(point);
                mapView.getOverlays().add(newMarker);

                markerCounts.put(point, 1);
            }
            mapView.invalidate();
        }
    }

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

    private boolean arePointsClose(GeoPoint p1, GeoPoint p2) {
        double distance = p1.distanceToAsDouble(p2);
        return distance < 10;
    }

    public void setDefaultCenter() {
        setMapCenter(new GeoPoint(41.8919, 12.5113));
    }
}
