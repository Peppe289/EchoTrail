package com.peppe289.echotrail.utils;

import android.content.Context;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapHelper {
    private MapView mapView;

    public MapHelper(MapView mapView) {
        this.mapView = mapView;
    }

    public void initializeMap(Context context) {
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        mapView.setTileSource(TileSourceFactory.OpenTopo);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.setMinZoomLevel(6.0);
        mapView.setMaxZoomLevel(25.0);
    }

    public void setMapCenter(GeoPoint point) {
        if (mapView != null) {
            mapView.getController().setCenter(point);
        }
    }

    public void setDefaultCenter() {
        // Rome as default.
        setMapCenter(new GeoPoint(41.8919, 12.5113));
    }
}
