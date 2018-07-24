package in.delbird.delbirddriver.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

import in.delbird.delbirddriver.controller.Messages;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by machine2 on 1/18/16.
 */
public class Utils {

    // Check is GPS is enabled or not.
    public static boolean isGPSEnabled(final Context context) {


        LocationManager lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //   GPS is OFF.
            return false;
        } else {
            // GPS is ON.
            return true;
        }
    }


    public static Dialog noGpsEnabledDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Location Services Not Active");
        builder.setMessage("Please enable Location Services and GPS");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);

            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        return alertDialog;
    }

    public static void makeCall(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static Dialog noInternetConnection(final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Messages.NO_INTERNET_CONNECTION);
        builder.setMessage(Messages.On_INTERNET_CONNECTION);
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent("retry");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("ok");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        return alertDialog;
    }

    public static Dialog cancelRideDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Messages.RIDE_CANCELLED);
        builder.setMessage(Messages.RIDE_CANCELLED_MESSAGE);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent("cancel_ride_dialog");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        if (!((Activity) context).isFinishing()) {
            //show dialog
            alertDialog.show();
        }

        return alertDialog;

    }





    public static String convertLongTimeToStringDate(long time) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy hh:mm a");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(time + (11 * 60 * 30 * 1000));
        return (dateFormat.format(date));
    }
}
