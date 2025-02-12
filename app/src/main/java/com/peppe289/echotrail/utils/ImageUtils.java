package com.peppe289.echotrail.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * A utility class for applying background colors and resizing images,
 * along with cropping the image to a circular shape. This is especially
 * useful for customizing profile images with backgrounds.
 */
public class ImageUtils {

    /**
     * Applies a background color to an image and resizes the image to fit within
     * the specified width and height. After resizing, the image is cropped to
     * a circular shape.
     *
     * @param context    The context to access the resources.
     * @param imageResId The resource ID of the image to be processed.
     * @param bgColor    The background color to be applied to the image.
     * @param width      The width to which the image should be resized.
     * @param height     The height to which the image should be resized.
     * @return A new Bitmap that contains the image with the applied background
     * and cropped to a circular shape.
     */
    public static Bitmap applyBackgroundToImage(Context context, int imageResId, int bgColor, int width, int height) {
        // Load the original image from resources
        Drawable drawable = context.getResources().getDrawable(imageResId, null);
        Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();

        // Create a new bitmap with the specified width and height
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);

        // Draw the background color
        canvas.drawColor(bgColor);

        // Calculate the scaling factor to resize the image while maintaining the aspect ratio
        float scaleX = (float) width / originalBitmap.getWidth();
        float scaleY = (float) height / originalBitmap.getHeight();
        float scale = Math.max(scaleX, scaleY); // Maintain the aspect ratio without distortion

        // Calculate the scaled dimensions
        int scaledWidth = Math.round(originalBitmap.getWidth() * scale);
        int scaledHeight = Math.round(originalBitmap.getHeight() * scale);

        // Calculate the position to center the image in the new bitmap
        int left = (width - scaledWidth) / 2;
        int top = (height - scaledHeight) / 2;

        // Draw the resized image onto the canvas
        Paint paint = new Paint();
        canvas.drawBitmap(originalBitmap, null, new Rect(left, top, left + scaledWidth, top + scaledHeight), paint);

        // Now crop the image to a circular shape
        return getRoundedCroppedBitmap(newBitmap);
    }

    /**
     * Crops the given bitmap into a circular shape.
     *
     * @param bitmap The bitmap to be cropped.
     * @return A new Bitmap that is cropped into a circular shape.
     */
    private static Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
        // Calculate the diameter of the circular shape (smallest dimension)
        int diameter = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);

        // Create a canvas to draw the circular shape
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true); // Ensure smooth edges
        paint.setFilterBitmap(true);
        paint.setDither(true);

        // Draw a circle on the canvas
        canvas.drawARGB(0, 0, 0, 0); // Clear canvas
        paint.setColor(Color.parseColor("#BAB399")); // Set background color (can be customized)
        canvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, paint);

        // Apply the bitmap to the circular mask
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return output;
    }

    /**
     * Applies a background color to an image, resizes it to fit the dimensions of
     * the given ImageView, and then crops it to a circular shape before setting it
     * as the image of the ImageView.
     *
     * @param imageView  The ImageView to which the processed image will be set.
     * @param imageResId The resource ID of the image to be processed.
     * @param bgColor    The background color to be applied to the image.
     */
    public static void setImageWithBackground(ImageView imageView, int imageResId, int bgColor) {
        Context context = imageView.getContext();

        // Get the dimensions of the ImageView
        int width = imageView.getWidth();
        int height = imageView.getHeight();

        // If the dimensions have not been determined (e.g., the layout has not been measured yet),
        // use default values to avoid division by zero.
        if (width == 0 || height == 0) {
            width = 500; // Use fallback value to avoid zero width or height
            height = 500;
        }

        // Apply the background color and resize the image
        Bitmap bitmapWithBg = applyBackgroundToImage(context, imageResId, bgColor, width, height);
        imageView.setImageBitmap(bitmapWithBg);
    }
}
