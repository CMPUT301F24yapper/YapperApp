package ca.yapper.yapperapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WorldMapPinsOverlay extends View {

    private final Paint pinPaint = new Paint();
    private final List<float[]> pinCoordinates = new ArrayList<>();

    public WorldMapPinsOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        pinPaint.setColor(0xFFFF0000); // Red for pins
        pinPaint.setStyle(Paint.Style.FILL);
    }

    public void setPinCoordinates(List<float[]> coordinates) {
        pinCoordinates.clear();
        pinCoordinates.addAll(coordinates);
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pinCoordinates.isEmpty()) return;

        for (float[] coords : pinCoordinates) {
            canvas.drawCircle(coords[0], coords[1], 10, pinPaint); // Draw pin as red circle
        }
    }
}
