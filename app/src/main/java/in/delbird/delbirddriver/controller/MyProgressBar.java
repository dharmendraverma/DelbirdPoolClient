package in.delbird.delbirddriver.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by machine2 on 2/3/16.
 */
public class MyProgressBar extends ProgressBar {
    public MyProgressBar(Context context) {
        super(context);
    }

    public MyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.scale(-1f, 1f, super.getWidth() * 0.5f, super.getHeight() * 0.5f);
        super.onDraw(canvas);
    }
}
