package in.delbird.delbirddriver.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Messages;
import in.delbird.delbirddriver.controller.MyProgressBar;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.utils.AddMarker;
import in.delbird.delbirddriver.utils.Show;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dharmendra on 1/28/16.
 */
public class AcceptTask extends BaseActivity implements View.OnClickListener {

    Boolean isAccepted = null;
    SharedPreferences preferences;
    GoogleMap googleMap;
    CountDownTimer countDownTime;
//    MediaPlayer mediaPlayer;
    private Toolbar toolbar;
    private MyProgressBar progressBar;
    private TextView remainingTime, pickupAddress, etaTime;
    private LinearLayout parent;
    private RelativeLayout acceptRequest, rejectRequest;
    private Marker pickupMarker = null;
    private ProgressDialog progressDialog;

    private void initilizeMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.googleMap)).getMap();
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return;
            }
            googleMap.setMyLocationEnabled(true);
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }


            //googleMap.setOnCameraChangeListener(this);
        }
    }

    private void parseRideByIdResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            pickupAddress.setText(jsonObject.getString("from_address"));
            LatLng pickupLatLng = new LatLng(jsonObject.getDouble("from_lat"), jsonObject.getDouble("from_lon"));
            pickupMarker = AddMarker.addMarker(googleMap, pickupMarker, pickupLatLng, R.drawable.location_icon, "");

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pickupLatLng, 13);
            googleMap.animateCamera(cameraUpdate);

            JSONObject userObject = jsonObject.getJSONObject("user");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.USER_PHONE, userObject.getString("phone"));
            editor.putString(Constants.USER_NAME, (userObject.getString("first_name") + " " + userObject.getString("last_name")));
            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_task);
//        mediaPlayer = MediaPlayer.create(AcceptTask.this, R.raw.sound);
        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(AcceptTask.this);
        progressDialog.setMessage("Loading ..");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Accept Task");
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);

        mediaPlayer.start();
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        parent = (LinearLayout) findViewById(R.id.parent);
        acceptRequest = (RelativeLayout) findViewById(R.id.accept);
        rejectRequest = (RelativeLayout) findViewById(R.id.reject);
        progressBar = (MyProgressBar) findViewById(R.id.progressBar);
        remainingTime = (TextView) findViewById(R.id.remaining_time);
        pickupAddress = (TextView) findViewById(R.id.pickup_address);
        etaTime = (TextView) findViewById(R.id.eta_time);
        acceptRequest.setOnClickListener(this);
        rejectRequest.setOnClickListener(this);
        etaTime.setText(preferences.getString(Constants.ETA + " Minutes", "1 Minutes"));
        initilizeMap();
        getRideByRide();
        countDownTime = new CountDownTimer(Messages.DRIVER_COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

//                mediaPlayer.start();
                int remaining = (int) ((millisUntilFinished) / 1000);
                Show.showLog("Remaining", remaining + " ");
                progressBar.setProgress(remaining * 10);
                remainingTime.setText(remaining + "\nLEFT");

            }

            @Override
            public void onFinish() {
                Toast.makeText(AcceptTask.this, Messages.REQUEST_TIMEOUT, Toast.LENGTH_LONG).show();
//                mediaPlayer.stop();
                progressBar.setProgress(0 * 10);
                remainingTime.setText(0 + "\nLEFT");

                goToHome();

            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.accept:
//                mediaPlayer.stop();
                countDownTime.cancel();
                isAccepted = true;
                makeAcceptRejectRequest(isAccepted);
                break;
            case R.id.reject:
//                mediaPlayer.stop();
                countDownTime.cancel();
                isAccepted = false;
                makeAcceptRejectRequest(isAccepted);
                break;
        }

    }

    private void makeAcceptRejectRequest(final Boolean isAccepted) {
        progressDialog.show();
        String url = Urls.ACCEPT_REJECT_REQUEST;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Accept reject request", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("status") && jsonObject.getString("status").equalsIgnoreCase("fail")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AcceptTask.this);
                        builder.setTitle(Messages.REQUEST_NOT_AVAILABLE);
                        builder.setMessage(jsonObject.getString("reason"));
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Show location settings when the user acknowledges the alert dialog
                                progressDialog.dismiss();
                                goToHome();
                            }
                        });

                        Dialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        if (!((Activity) AcceptTask.this).isFinishing()) {
                            //show dialog
                            alertDialog.show();
                        }
                    } else {
                        if (isAccepted) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(AcceptTask.this, EnrouteScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            progressDialog.dismiss();
                            goToHome();

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("driver_id", preferences.getLong(Constants.DRIVER_ID, -1) + "");
                params.put("ride_id", preferences.getLong(Constants.RIDE_ID, -1) + "");
                params.put("is_user_ride", true + "");
                params.put("response", isAccepted + "");
                params.put("json", "1,2,3");
                Show.showLog("AcceptReject params", params.toString());
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 0));
        AppController.getInstance().addToRequestQueue(request);
    }

    private void getRideByRide() {
        String url = Urls.GET_RIDE_BY_ID;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Get by id response", response);
                parseRideByIdResponse(response);
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
                params.put("ride_id", preferences.getLong(Constants.RIDE_ID, -1) + "");
                Show.showLog("get by id params", params.toString());
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 0));
        AppController.getInstance().addToRequestQueue(request);

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onResume() {
        super.onResume();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(0);
    }

    private void goToHome() {

        makeBikeStatusRequest(true);

    }

    private void makeBikeStatusRequest(final boolean isOnline) {
        final ProgressDialog progressDialog = new ProgressDialog(AcceptTask.this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String url = Urls.SET_BIKE_ONLINE_STATUS;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Bike online status", response);

                Intent intent = new Intent(AcceptTask.this, HomeScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("is_online", isOnline + "");
                params.put("id", preferences.getLong(Constants.DRIVER_ID, -1) + "");
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

}
