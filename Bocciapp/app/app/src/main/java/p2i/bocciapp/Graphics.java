package p2i.bocciapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class Graphics extends View {

    public static final String TAG = "MyLogsGraphics"; //this tag is only used to sort the logs I write from the ones the app writes by itself

    Drawable customImage;
    Paint paintRound = new Paint();

    float cx, cy, r;


    public Graphics(Context context) {
        super(context);
        init();
    }
    public Graphics(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Graphics(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        customImage = getContext().getResources().getDrawable(R.drawable.backgroundterrain_flipped);
        paintRound.setColor(getResources().getColor(R.color.red_ball));
        paintRound.setStyle(Paint.Style.FILL);
        paintRound.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int minX = getWidth() / 3;
        int maxX = getWidth() - getWidth() / 10;
        cx = (int) Math.floor(Math.random() * (maxX - minX + 1) + minX);
        int minY = getHeight() / 2 + getHeight() / 30;
        int maxY = getHeight() - getHeight() / 30;
        cy = (int) Math.floor(Math.random() * (maxY - minY + 1) + minY);
        Log.i(TAG, getHeight() + "   " + getWidth());

        r = 10;

        Rect bounds = new Rect(0, getHeight()/2, getWidth(), getHeight());
        customImage.setBounds(bounds);
        customImage.draw(canvas);

        canvas.drawCircle(cx, cy, r, paintRound);

    }
}
