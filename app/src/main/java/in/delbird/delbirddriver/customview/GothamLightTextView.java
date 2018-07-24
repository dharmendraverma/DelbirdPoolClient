package in.delbird.delbirddriver.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Dharmendra on 9/6/15.
 */
public class GothamLightTextView extends TextView {


    public GothamLightTextView(Context context) {
        this(context, null);
    }

    public GothamLightTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GothamLightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setTypeface(Typeface.createFromAsset(context.getAssets(), "gotham_light.TTF"));
    }

}
