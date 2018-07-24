package in.delbird.delbirddriver.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Dharmendra on 1/20/16.
 */
public class GothamMediumTextView extends TextView {
    public GothamMediumTextView(Context context) {
        super(context);
    }

    public GothamMediumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GothamMediumTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        setTypeface(Typeface.createFromAsset(context.getAssets(), "gotham_medium.TTF"));
    }
}
