package in.delbird.delbirddriver;



import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import in.delbird.delbirddriver.activities.EnrouteScreen;
import in.delbird.delbirddriver.activities.HomeScreen;
import in.delbird.delbirddriver.activities.LoginScreen;
import in.delbird.delbirddriver.activities.OnTrip;
import in.delbird.delbirddriver.activities.ReceiptScreen;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.enums.RideStatus;
import in.delbird.delbirddriver.enums.RideType;
import in.delbird.delbirddriver.utils.CheckConnection;
import in.delbird.delbirddriver.utils.Show;
import in.delbird.delbirddriver.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences preferences;
    CheckConnection checkConnection;
    Dialog noConnectionDialog, noGPSDialog;
    CountDownTimer countDownTimer;
    // No internet connection, actions for not internet connection dialog;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("retry")) {
                countDownTimer.onFinish();
            } else if (intent.getAction().equalsIgnoreCase("ok")) {
                noConnectionDialog.dismiss();
            }
        }
    };
    private GoogleCloudMessaging gcm;
    private String gcmID, msg;
    private String GCM_PROJECT_NUMBER = "561808516187";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("retry");
        intentFilter.addAction("ok");
        LocalBroadcastManager.getInstance(SplashScreen.this).registerReceiver(broadcastReceiver, intentFilter);
        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        checkConnection = new CheckConnection(SplashScreen.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (noGPSDialog != null && noGPSDialog.isShowing()) {
            noGPSDialog.dismiss();
        }
        countDownTimer = new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                if (checkConnection.isConnectionAvailable()) {
                    if (Utils.isGPSEnabled(SplashScreen.this)) {
                        getREGID();
                        if (preferences.getBoolean(Constants.IS_LOGGED_IN, false)) {

                            getRideByDriverId();

                        } else {
                            Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        noGPSDialog = Utils.noGpsEnabledDialog(SplashScreen.this);
                    }
                } else {
                    //Alert Dialog.
                    noConnectionDialog = Utils.noInternetConnection(SplashScreen.this);
                }

            }
        }.start();
    }

    private void getRideByDriverId() {
        String url = Urls.GET_RIDE_BY_DRIVER_ID;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("GET_RIDE_BY_DRIVER_ID response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("status") && jsonObject.getString("status").equalsIgnoreCase("fail")) {
                        Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(Constants.DRIVER_STATUS, jsonObject.getString("status"));
                        Show.showLog("DRiver status", jsonObject.getString("status"));
                        double rating = jsonObject.getDouble("rating");
                        editor.putFloat(Constants.DRIVER_RATING, Float.parseFloat(rating + ""));
                        if (jsonObject.getString("status").equalsIgnoreCase("online")) {
                            editor.putBoolean(Constants.IS_BIKE_ONLINE, true);
                        } else {
                            editor.putBoolean(Constants.IS_BIKE_ONLINE, false);
                        }
                        editor.commit();
                        if (!jsonObject.isNull("rides")) {
                            JSONArray rideArray = jsonObject.getJSONArray("rides");
                            if (rideArray.length() > 0) {
                                for (int i = 0; i < rideArray.length(); i++) {
                                    JSONObject rideObject = rideArray.getJSONObject(0);

                                    if (rideObject.getString("type").equalsIgnoreCase(RideType.PACKAGE.toString())) {
                                        editor.putBoolean(Constants.IS_RIDE, false);
                                    } else {
                                        editor.putBoolean(Constants.IS_RIDE, true);
                                    }
                                    editor.putLong(Constants.RIDE_ID, rideObject.getLong("id"));
                                    editor.commit();
                                    if (!rideObject.getBoolean("is_payment_successfull") && rideObject.getString("state").equalsIgnoreCase(RideStatus.ENDRIDE.toString())) {
                                        Intent intent = new Intent(SplashScreen.this, ReceiptScreen.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        if (rideObject.getString("state").equalsIgnoreCase(RideStatus.ENROUTE.toString())
                                                || rideObject.getString("state").equalsIgnoreCase(RideStatus.ARRIVED.toString())) {
                                            Intent intent = new Intent(SplashScreen.this, EnrouteScreen.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        } else if (rideObject.getString("state").equalsIgnoreCase(RideStatus.ONTRIP.toString())) {
                                            Intent intent = new Intent(SplashScreen.this, OnTrip.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        } else if (rideObject.getString("state").equalsIgnoreCase(RideStatus.REJECTED.toString())) {
                                            Intent intent = new Intent(SplashScreen.this, HomeScreen.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                }

                            } else {
                                Intent intent = new Intent(SplashScreen.this, HomeScreen.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Intent intent = new Intent(SplashScreen.this, HomeScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                        editor.commit();


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("driver_id", preferences.getLong(Constants.DRIVER_ID, -1) + "");
                params.put("gcm_id", preferences.getString("gcm", ""));

                Show.showLog("GET_RIDE_BY_DRIVER_ID params", params.toString());
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 0));
        AppController.getInstance().addToRequestQueue(request);
    }

    private void getREGID() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                SharedPreferences.Editor editor = preferences.edit();
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    gcmID = gcm.register(GCM_PROJECT_NUMBER);
                    System.out.print("gcmid-------------" + gcmID);
                    msg = "Device registered, registration ID=" + gcmID;
                    editor.putString("gcm", gcmID);
                    editor.commit();
                    Log.e("gcmid", gcmID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return gcmID;
            }

            @Override
            protected void onPostExecute(String gcmID) {
                super.onPostExecute(gcmID);
            }
        }.execute();
    }

}
