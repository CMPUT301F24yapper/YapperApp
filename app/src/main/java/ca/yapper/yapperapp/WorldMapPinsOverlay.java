package ca.yapper.yapperapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * This class displays the pins on the world map indicating locations for users who have joined events
 */
public class WorldMapPinsOverlay extends View {

    private final Paint pinPaint = new Paint();
    private final List<float[]> pinCoordinates = new ArrayList<>();


    /**
     * This function sets the color and style of the coordinate pins for the map
     *
     * @param context the environmental data from the phone system
     * @param attrs a set of attributes
     */
    public WorldMapPinsOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        pinPaint.setColor(0xFFFF0000); // Red for pins
        pinPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * This function updates the list of coordinates and redraws the map with the new coordinates
     *
     * @param coordinates user coordinate list
     */
    public void setPinCoordinates(List<float[]> coordinates) {
        pinCoordinates.clear();
        pinCoordinates.addAll(coordinates);
        invalidate(); // Redraw the view
    }

    /**
     * This function draws the user coordinates on a given canvas
     *
     * @param canvas a canvas for displaying the coordinates of the users
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pinCoordinates.isEmpty()) return;

        for (float[] coords : pinCoordinates) {
            canvas.drawCircle(coords[0], coords[1], 10, pinPaint); // Draw pin as red circle
        }
    }
}
