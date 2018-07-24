package in.delbird.delbirddriver.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Messages;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.enums.NotificationType;
import in.delbird.delbirddriver.enums.RideStatus;
import in.delbird.delbirddriver.enums.RideType;
import in.delbird.delbirddriver.utils.AddMarker;
import in.delbird.delbirddriver.utils.Show;
import in.delbird.delbirddriver.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shahil on 1/28/16.
 */
public class EnrouteScreen extends BaseActivity implements View.OnClickListener {
    public static boolean ENRTOURE_SCREEN_STATUS = false;
    TextView pickupAddres, userName, etaTime, rideStatus;
    ImageView userPic;
    ImageButton call;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private Handler m_handler;
    private GoogleMap googleMap;
    private Runnable m_handlerTask;
    private LatLng latLng;
    private String rideType = "";
    private Dialog cancelRideDialog;
    private String phoneNumber = "";
    private SharedPreferences preferences;
    private Marker pickupMarker = null, currentMarker = null;
    private ImageButton cancelRide;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (intent.getAction().equalsIgnoreCase(NotificationType.RIDE_STATE.toString().toLowerCase())) {
                    getRideByRide();
                } else if (intent.getAction().equalsIgnoreCase("cancel_ride_dialog")) {
                    Intent intent1 = new Intent(EnrouteScreen.this, ReceiptScreen.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent1);
                    finish();
                    cancelRideDialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private Runnable runnable;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enroute_screen);
        ENRTOURE_SCREEN_STATUS = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationType.RIDE_STATE.toString().toLowerCase());
        intentFilter.addAction("cancel_ride_dialog");   // Not GCM .its  for util purpose.
        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
        init();
        initilizeMap();

    }

    private void init() {
        handler = new Handler();
        progressDialog = new ProgressDialog(this);
        m_handler = new Handler();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Enroute");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        pickupAddres = (TextView) findViewById(R.id.pickup_address);
        userName = (TextView) findViewById(R.id.user_name);
        etaTime = (TextView) findViewById(R.id.eta_time);
        rideStatus = (TextView) findViewById(R.id.ride_status);
        userPic = (ImageView) findViewById(R.id.user_pic);
        cancelRide = (ImageButton) findViewById(R.id.cancel_ride);
        call = (ImageButton) findViewById(R.id.call);
        cancelRide.setOnClickListener(this);
        rideStatus.setOnClickListener(this);
        call.setOnClickListener(this);
    }

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
//            googleMap.setMyLocationEnabled(true);


            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }

            m_handlerTask = new Runnable() {
                @Override
                public void run() {
                    // do something
                    progressDialog.show();
                    latLng = currentLatLng();
                    Log.e("latlng", latLng + "");
                    m_handler.postDelayed(m_handlerTask, 3000);
                    if (Double.compare(latLng.latitude, 0.0) == 0) {
                    } else {
//                        getETA(latLng);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13);
                        googleMap.animateCamera(cameraUpdate);
                        currentMarker = AddMarker.addMarker(googleMap, currentMarker, latLng, R.drawable.current_marker, "");

                        progressDialog.dismiss();
                        m_handler.removeMessages(0);
                        m_handler.removeCallbacks(m_handlerTask);

                    }
                }
            };
            m_handlerTask.run();

            //googleMap.setOnCameraChangeListener(this);
        }
    }

    public LatLng currentLatLng() {
        return new LatLng(lat, lng);
//        return new LatLng(18.9750, 72.8258);
    }

    @Override
    public void onResume() {
        super.onResume();
        ENRTOURE_SCREEN_STATUS = true;
        getRideByRide();
        updateDriverLocation();

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ENRTOURE_SCREEN_STATUS = false;


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
        AppController.getInstance().addToRequestQueue(request);

    }

    private void parseRideByIdResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject userObject = jsonObject.getJSONObject("user");
            JSONObject driverObject=jsonObject.getJSONObject("driver");
            if (jsonObject.getString("state").equalsIgnoreCase(RideStatus.ENROUTE.toString())) {
                toolbar.setTitle(RideStatus.ENROUTE.toString());
                rideStatus.setText(RideStatus.ARRIVED.toString());
            } else if (jsonObject.getString("state").equalsIgnoreCase(RideStatus.ARRIVED.toString())) {
                toolbar.setTitle(RideStatus.ARRIVED.toString());
                rideStatus.setText(RideStatus.ONTRIP.toString());
            } else if (jsonObject.getString("state").equalsIgnoreCase(RideStatus.CANCELLED.toString())) {
                // Show Alert dialog.
                cancelRideDialog = Utils.cancelRideDialog(EnrouteScreen.this);
                cancelRideDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            finish();
                        }

                        return false;
                    }
                });
            }
            setSupportActionBar(toolbar);
            rideType = jsonObject.getString("type");
            setIsRide(rideType);
            pickupAddres.setText(jsonObject.getString("from_address"));
            LatLng pickupLatLng = new LatLng(jsonObject.getDouble("from_lat"), jsonObject.getDouble("from_lon"));
            pickupMarker = AddMarker.addMarker(googleMap, pickupMarker, pickupLatLng, R.drawable.location_icon, "");
            if (Double.compare(lat, 0.0) == 0) {
            } else {
                currentMarker = AddMarker.addMarker(googleMap, currentMarker, new LatLng(lat, lng), R.drawable.current_marker, "");
                adjustZoomLevel();
            }

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pickupLatLng, 14);
            googleMap.animateCamera(cameraUpdate);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.USER_NAME, userObject.getString("first_name") + " " + userObject.getString("last_name"));
            editor.putString(Constants.USER_PIC, userObject.getString("pic_url"));
            editor.putString(Constants.USER_PHONE, userObject.getString("phone"));
            if (driverObject.getString("status").equalsIgnoreCase("online")) {
                editor.putBoolean(Constants.IS_BIKE_ONLINE, true);
            } else {
                editor.putBoolean(Constants.IS_BIKE_ONLINE, false);
            }
            editor.commit();

            userName.setText(userObject.getString("first_name") + " " + userObject.getString("last_name"));
            if (userObject.has("pic_url") && userObject.getString("pic_url").length() > 0)
                Picasso.with(EnrouteScreen.this).load(userObject.getString("pic_url")).fit().placeholder(R.drawable.pro_pic).into(userPic);

            phoneNumber = userObject.getString("phone");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ride_status:
                if (rideStatus.getText().toString().equalsIgnoreCase(RideStatus.ARRIVED.toString())) {
                    makeUpdateRideStatusRequest(rideStatus.getText().toString());
                    rideStatus.setText(RideStatus.ONTRIP.toString());


                } else if (rideStatus.getText().toString().equalsIgnoreCase(RideStatus.ONTRIP.toString())) {
                    makeUpdateRideStatusRequest(rideStatus.getText().toString());

                }
                break;
            case R.id.call:
                Utils.makeCall(EnrouteScreen.this, phoneNumber);
                break;
            case R.id.cancel_ride:
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(EnrouteScreen.this);
                builder.setTitle(Messages.RIDE_CANCELLED);
                builder.setMessage(Messages.CANCELLED_RIDE);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show location settings when the user acknowledges the alert dialog
                        cancelRide();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

                break;
        }
    }

    private void cancelRide() {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.CANCEL_RIDE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("Status") && jsonObject.getString("Status").equalsIgnoreCase("success")) {
                        Intent intent = new Intent(EnrouteScreen.this, ReceiptScreen.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
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
                params.put("ride_id", preferences.getLong(Constants.RIDE_ID, -1) + "");
                Log.e("Cancel Ride", params.toString());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);

    }

    private void makeUpdateRideStatusRequest(final String rideStatus) {
        final ProgressDialog progressDialog = new ProgressDialog(EnrouteScreen.this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String url = Urls.RIDE_STATE_UPDATE;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Ride status update response", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("status") && jsonObject.getString("status").equalsIgnoreCase("fail")) {
                        Toast.makeText(EnrouteScreen.this, jsonObject.getString("reason") + " ", Toast.LENGTH_LONG).show();
                    } else {
                        toolbar.setTitle(rideStatus);
                        setSupportActionBar(toolbar);
                        if (rideStatus.equalsIgnoreCase(RideStatus.ONTRIP.toString())) {
                            if (rideType.equalsIgnoreCase(RideType.PACKAGE.toString())) {
                                Intent intent = new Intent(EnrouteScreen.this, ViewParcel.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(EnrouteScreen.this, OnTrip.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                    progressDialog.dismiss();
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
                params.put("state", rideStatus);
                params.put("ride_id", preferences.getLong(Constants.RIDE_ID, -1) + "");
                params.put("is_user_ride", preferences.getBoolean(Constants.IS_RIDE, false) + "");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);
    }

    private void setIsRide(String requestType) {
        SharedPreferences.Editor editor = preferences.edit();
        if (requestType.equalsIgnoreCase("PACKAGE")) {
            editor.putBoolean(Constants.IS_RIDE, false);
        } else {
            editor.putBoolean(Constants.IS_RIDE, true);

        }
        editor.commit();
    }

    private void adjustZoomLevel() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(pickupMarker.getPosition());
        builder.include(currentMarker.getPosition());
        final LatLngBounds bounds = builder.build();

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                googleMap.animateCamera(cu);
            }
        });


    }

    private void updateDriverLocation() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (Double.compare(lat, 0.0) == 0 && Double.compare(lng, 0.0) == 0) {

                } else {
                    StringRequest request = new StringRequest(Request.Method.POST, Urls.UPDATE_DRIVER_LOCATION, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Show.showLog("Update driver location response", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String time = jsonObject.getString("time");
                                JSONObject timeObject = new JSONObject(time);
                                etaTime.setText("ETA (" + timeObject.getString("ETA") + " Mins)");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("driver_id", preferences.getLong(Constants.DRIVER_ID, -1) + "");
                            params.put("ride_id", preferences.getLong(Constants.RIDE_ID, -1) + "");
                            params.put("lat", lat + "");
                            params.put("lon", lng + "");
                            Log.e("Update location", params.toString());
                            return params;
                        }
                    };
                    AppController.getInstance().addToRequestQueue(request);
                }
                handler.postDelayed(runnable, Constants.TIME_INTERVAL);
            }
        };
        runnable.run();
    }


}
