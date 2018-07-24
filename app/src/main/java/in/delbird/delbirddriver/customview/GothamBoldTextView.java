package in.delbird.delbirddriver.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Dharmendra on 1/20/16.
 */
public class GothamBoldTextView extends TextView {
    public GothamBoldTextView(Context context) {
        super(context);
    }

    public GothamBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GothamBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private void init(Context context) {
        setTypeface(Typeface.createFromAsset(context.getAssets(), "gotham_bold.TTF"));
    }
}
