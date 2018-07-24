package in.delbird.delbirddriver.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by deepak on 14/5/15.
 */
public class CheckConnection {
 Context context;

    public CheckConnection(Context context) {
        this.context = context;
    }

    public boolean isConnectionAvailable() {

        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null)
        {
           // NetworkInfo[] networkInfo=connectivityManager.getAllNetworkInfo();

            NetworkInfo info=connectivityManager.getActiveNetworkInfo();
            if(info!=null)
            {
               String type=info.getTypeName();
                if(type.equalsIgnoreCase("WIFI")||type.equalsIgnoreCase("MOBILE")){
                    //Toast.makeText(context,"connected",Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }

        return false;
    }

}