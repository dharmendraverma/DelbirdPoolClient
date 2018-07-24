package in.delbird.delbirddriver.utils;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

/**
 * Created by machine2 on 1/15/16.
 */
public class Show {
    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public static void showLog(String key, String value) {
        Log.e(key, value);
    }
}
