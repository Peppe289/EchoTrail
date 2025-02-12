package com.peppe289.echotrail.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;

public class CircleOverlay extends Overlay {
    private GeoPoint center;
    private final double radiusMeters;
    private final Paint fillPaint;
    private final Paint strokePaint;

    /**
     * @param center        Il centro del cerchio (di solito la posizione del marker)
     * @param radiusMeters  Il raggio in metri
     */
    public CircleOverlay(GeoPoint center, double radiusMeters) {
        this.center = center;
        this.radiusMeters = radiusMeters;

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.parseColor("#4C03A9F4"));

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.rgb(0, 0, 255));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4);
    }

    // Permette di aggiornare il centro del cerchio quando il marker si sposta
    public void setCenter(GeoPoint center) {
        this.center = center;
    }

    @Override
    public void draw(Canvas canvas, Projection projection) {
        if (center == null) return;

        // Converte il centro (GeoPoint) in coordinate dello schermo
        Point screenCenter = new Point();
        projection.toPixels(center, screenCenter);

        /*
         * Per calcolare il raggio in pixel, possiamo creare un GeoPoint "offset" dal centro.
         * Usiamo una conversione approssimativa: 1 grado di latitudine ≈ 111320 metri.
         * Quindi, l’offset in latitudine corrispondente a "radiusMeters" è:
         */
        double latOffset = radiusMeters / 111320.0;
        GeoPoint edgeGeoPoint = new GeoPoint(center.getLatitude() + latOffset, center.getLongitude());
        Point screenEdge = new Point();
        projection.toPixels(edgeGeoPoint, screenEdge);
        float radiusPx = Math.abs(screenEdge.y - screenCenter.y);

        // Disegna il cerchio: prima il riempimento...
        canvas.drawCircle(screenCenter.x, screenCenter.y, radiusPx, fillPaint);
        // ...poi il bordo
        canvas.drawCircle(screenCenter.x, screenCenter.y, radiusPx, strokePaint);
    }
}
