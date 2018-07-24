package in.delbird.delbirddriver.controller;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.SplashScreen;
import in.delbird.delbirddriver.activities.AcceptTask;
import in.delbird.delbirddriver.activities.EnrouteScreen;
import in.delbird.delbirddriver.activities.HomeScreen;
import in.delbird.delbirddriver.enums.NotificationType;

import java.net.URL;


/**
 * Created by Dharmendra on 7/7/15.
 */
public class GcmMessageHandler extends IntentService {
    final static String GROUP_KEY_EMAILS = "group_key_emails";
    GoogleCloudMessaging gcm;
    Bundle extras;
    NotificationCompat.Builder mNotifyBuilder;
    NotificationManager mNotificationManager;
    SharedPreferences preferences;

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
        extras = intent.getExtras();
        String message_type = gcm.getMessageType(intent);

        String notificationType = extras.getString("type");
        String message = extras.getString("message");
        Log.e("MESSAGE", notificationType + " " + message);
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                .equals(message_type)) {
        }
//        final MediaPlayer mp = MediaPlayer.create(this, R.raw.sound);
//        mp.start();
        if (notificationType != null && notificationType.equalsIgnoreCase(NotificationType.REQUEST_DRIVER.toString())
                && preferences.getBoolean(Constants.IS_BIKE_ONLINE, true)) {
            String rideId = extras.getString("ride_id");
            if (HomeScreen.homeScreenStatus) {
                sendBroadcastMessage(notificationType);
            } else {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(Constants.RIDE_ID, Long.parseLong(rideId));
                editor.commit();
                Intent intent1 = new Intent(getApplicationContext(), AcceptTask.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                showNotification("Delbird", message, intent1);

                Intent intent2 = new Intent(this, AcceptTask.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
            }


        } else if (notificationType != null && notificationType.equalsIgnoreCase(NotificationType.RIDE_STATE.toString())) {
            String rideId = extras.getString("ride_id");
            if (EnrouteScreen.ENRTOURE_SCREEN_STATUS) {
                sendBroadcastMessage(notificationType);
            } else {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(Constants.RIDE_ID, Long.parseLong(rideId));
                editor.commit();
                Intent intent1 = new Intent(getApplicationContext(), EnrouteScreen.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                showNotification("Delbird", message, intent1);
            }
        } else if (notificationType != null && notificationType.equalsIgnoreCase("other")) {
            Intent intent1 = new Intent(getApplicationContext(), SplashScreen.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            bigPictureNotification("Delbird", message, extras.getString("url"), intent1);
        }

    }


    private void showNotification(String appName, String title, Intent intnt) {

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                intnt, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Uri alramSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound);     // Add custom sound


        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(appName)
                .setContentText(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(appName))

                .setSmallIcon(R.drawable.delbird_icon)      // small logo
//                .setSound(alramSound)         // for add custom sound
//                .setLights(3, 2000,4000)      // Change the legs lights color
                        // ic_action_add_person needs to be changed
                .setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.delbird_icon))
                .setGroup(GROUP_KEY_EMAILS)
                .setGroupSummary(true);

        // Set pending intent
        mNotifyBuilder.setContentIntent(resultPendingIntent);

        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;
        defaults = defaults | Notification.FLAG_AUTO_CANCEL;

        mNotifyBuilder.setDefaults(defaults);

        mNotifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(title));

        // Set the content for Notification
        mNotifyBuilder.setContentText(title);
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        // Post a notification
        mNotificationManager.notify(0, mNotifyBuilder.build());
//        mNotificationManager.notify((int) System.currentTimeMillis(), mNotifyBuilder.build());
        ;
    }

    // Unique Action perform by local broadcaster && Only for chat.
    private void sendBroadcastMessage(String action) {

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        Intent intent = new Intent(action);
        intent.putExtra(Constants.RIDE_ID, Long.parseLong(extras.getString("ride_id")));
        broadcastManager.sendBroadcast(intent);


    }

    private void bigPictureNotification(String appName, String title, String imageUrl, Intent intent) {
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Uri alramSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound);     // Add custom sound
        URL url = null;
        try {
            url = new URL(imageUrl);
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());


            mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(appName)
                    .setContentText(title)
                    .setStyle(new NotificationCompat.BigPictureStyle(mNotifyBuilder).bigPicture(image).setBigContentTitle(title))
                    .setSmallIcon(R.drawable.delbird_icon)      // small logo
//                    .setSound(alramSound)         // for add custom sound
                    .setGroup(GROUP_KEY_EMAILS)

                    .setGroupSummary(true);

            // Set pending intent
            mNotifyBuilder.setContentIntent(resultPendingIntent);

            // Set Vibrate, Sound and Light
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            defaults = defaults | Notification.DEFAULT_SOUND;
            defaults = defaults | Notification.FLAG_AUTO_CANCEL;

            mNotifyBuilder.setDefaults(defaults);
            // Set autocancel
            mNotifyBuilder.setAutoCancel(true);
            // Post a notification
            mNotificationManager.notify(0, mNotifyBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
