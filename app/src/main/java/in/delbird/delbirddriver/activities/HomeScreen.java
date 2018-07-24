package in.delbird.delbirddriver.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.fragment.FragmentNavigation;
import in.delbird.delbirddriver.utils.AddMarker;
import in.delbird.delbirddriver.utils.Show;

/**
 * Created by Dharmendra on 1/28/16.
 */
public class HomeScreen extends BaseActivity implements View.OnClickListener {
    public static boolean homeScreenStatus = false;
    Handler handler;
    Runnable runnable;
    Handler handler1 = new Handler();
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FragmentNavigation drawerFragment;
    private GoogleMap googleMap;
    private Runnable m_handlerTask;
    private LatLng latLng;
    private Handler m_handler;
    private TextView bikeStatus;
    private boolean isOnline = true;          // true for online, false for offline.
    private TextView bikeNumber, bikeName, driverRating;
    private SharedPreferences preferences;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(Constants.RIDE_ID, intent.getLongExtra(Constants.RIDE_ID, -1));
            editor.commit();
            Intent intent1 = new Intent(HomeScreen.this, AcceptTask.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent1);
            finish();
        }
    };
    Runnable updateLocation = new Runnable() {
        @Override
        public void run() {
            updateDriverLocation();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        homeScreenStatus = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("request_driver");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        init();
        setData(preferences.getString(Constants.DRIVER, ""), preferences.getString(Constants.BIKE, ""));
        initilizeMap();
        isOnline = preferences.getBoolean(Constants.IS_BIKE_ONLINE, true);

        handler1.postDelayed(updateLocation, Constants.TIME_INTERVAL);
        updateLocation.run();

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
                                if (!jsonObject.isNull("time")) {
                                    String time = jsonObject.getString("time");
                                    JSONObject timeObject = new JSONObject(time);
                                    if (timeObject.has("status") && timeObject.getString("status").equalsIgnoreCase("fail")) {

                                    } else {
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString(Constants.ETA, timeObject.getString("ETA"));
                                        editor.commit();
                                    }
                                }
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

    private void setData(String driver, String bike) {
        try {

            JSONObject driverObject = new JSONObject(driver);
            JSONObject bikeObject = new JSONObject(bike);
            bikeNumber.setText(bikeObject.getString("bike_number"));
            bikeName.setText(bikeObject.getString("name"));
            driverRating.setText(preferences.getFloat(Constants.DRIVER_RATING, 0.0f) + "/5");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(Constants.DRIVER_ID, driverObject.getLong("id"));
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        handler = new Handler();
        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        m_handler = new Handler();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Delbird");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        bikeStatus = (TextView) findViewById(R.id.bike_status);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerFragment = (FragmentNavigation) getSupportFragmentManager().findFragmentById(R.id.navFragment);
        drawerFragment.setUp(R.id.navFragment, drawerLayout, toolbar);

        bikeNumber = (TextView) findViewById(R.id.bike_number);
        bikeName = (TextView) findViewById(R.id.bike_name);
        driverRating = (TextView) findViewById(R.id.diver_rating);

        bikeStatus.setOnClickListener(this);


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
            googleMap.setMyLocationEnabled(true);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13));
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }

            moveToCurrentPosition();


            //googleMap.setOnCameraChangeListener(this);
        }
    }

    public LatLng currentLatLng() {
        return new LatLng(lat, lng);
//        return new LatLng(18.9750, 72.8258);
    }

    public void moveToCurrentPosition() {
        try {
            m_handlerTask = new Runnable() {
                @Override
                public void run() {
                    latLng = currentLatLng();
                    Log.e("latlng", latLng + "");
                    m_handler.postDelayed(m_handlerTask, 300);
                    if (Double.compare(latLng.latitude, 0.0) == 0 || Double.compare(latLng.latitude, 28.5707) == 0) {
                    } else {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13);
                        googleMap.animateCamera(cameraUpdate);
                        AddMarker.addMarker(googleMap, null, new LatLng(lat, lng), R.drawable.current_bike, "");
                        m_handler.removeMessages(0);
                        m_handler.removeCallbacks(m_handlerTask);
                    }
                }
            };
            m_handlerTask.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        homeScreenStatus = true;
        moveToCurrentPosition();
        if (preferences.getBoolean(Constants.IS_BIKE_ONLINE, true)) {
            bikeStatus.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            bikeStatus.setText("You are ONLINE");
        } else {
            bikeStatus.setBackgroundColor(getResources().getColor(R.color.colorBorder));
            bikeStatus.setText("Go online");
        }
        updateDriverLocation();
        getDriverStatus();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(0);
    }

    private void getDriverStatus() {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.GET_DRIVER_STATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("STATUS", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (jsonObject.getString("status").equalsIgnoreCase("online")) {

                        editor.putBoolean(Constants.IS_BIKE_ONLINE, true);
                        bikeStatus.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        bikeStatus.setText("You are ONLINE");
                    } else {
                        editor.putBoolean(Constants.IS_BIKE_ONLINE, false);
                        bikeStatus.setBackgroundColor(getResources().getColor(R.color.colorBorder));
                        bikeStatus.setText("Go Online");
                    }
                    editor.commit();
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
                params.put("driver_id", preferences.getLong(Constants.DRIVER_ID, -1) + "");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bike_status:
                setBikeStatus(isOnline);
                break;
        }
    }

    private void setBikeStatus(boolean isOnline) {
        if (isOnline) {
            this.isOnline = false;
            makeBikeStatusRequest(this.isOnline);

        } else {
            this.isOnline = true;
            makeBikeStatusRequest(this.isOnline);

        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.IS_BIKE_ONLINE, this.isOnline);
        editor.commit();
    }

    private void makeBikeStatusRequest(final boolean isOnline) {
        final ProgressDialog progressDialog = new ProgressDialog(HomeScreen.this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String url = Urls.SET_BIKE_ONLINE_STATUS;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Bike online status", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    //Bike is online , set text to go offline.
                    if (jsonObject.getString("status").equalsIgnoreCase("online")) {
                        bikeStatus.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        bikeStatus.setText("You are ONLINE");
                    } else {
                        bikeStatus.setBackgroundColor(getResources().getColor(R.color.colorBorder));
                        bikeStatus.setText("Go Online");
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
                params.put("is_online", isOnline + "");
                params.put("id", preferences.getLong(Constants.DRIVER_ID, -1) + "");
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        homeScreenStatus = false;

        handler.removeCallbacks(runnable);
        handler.removeMessages(0);
    }


}
