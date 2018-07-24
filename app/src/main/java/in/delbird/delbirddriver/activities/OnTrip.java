package in.delbird.delbirddriver.activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.Marker;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.enums.PackageStatus;
import in.delbird.delbirddriver.enums.RideStatus;
import in.delbird.delbirddriver.model.ParcelModel;
import in.delbird.delbirddriver.utils.AddMarker;
import in.delbird.delbirddriver.utils.Show;
import in.delbird.delbirddriver.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dharmendra on 1/28/16.
 */
public class OnTrip extends BaseActivity implements View.OnClickListener {
    SharedPreferences preferences;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private Handler m_handler;
    private GoogleMap googleMap;
    private Runnable m_handlerTask;
    private LatLng latLng;
    private ImageView viewPArcel;
    private TextView parcelCount, pickupAddress, receiverName, receiverlocation, receiverNumber;
    private LinearLayout parcelLayout, parcelStateLayout;
    private Marker pickupMarker = null;
    private Marker currentMarker = null;
    private TextView userName;
    private ImageButton call;
    private TextView etaTime;
    private String phoneNumber = "";
    private ImageView userPic;
    private Button cancelledPackage, deliveredPackage;
    private Runnable runnable;
    private Handler handler;
    private TextView endRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.on_trip_screen);
        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        init();
        initilizeMap();

        Intent intent = this.getIntent();
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("bundle");
            if (bundle != null) {
                ParcelModel parcelModel = (ParcelModel) bundle.getSerializable("parcel");
                setVisibilityForWidget(parcelModel);
                setParcelValue(parcelModel);
            } else {
                parcelStateLayout.setVisibility(View.GONE);
                parcelLayout.setVisibility(View.GONE);
            }
        }

        if (preferences.getBoolean(Constants.IS_RIDE, false)) {
            endRide.setVisibility(View.VISIBLE);
        } else {
            endRide.setVisibility(View.GONE);
        }

    }

    private void setParcelValue(ParcelModel parcelModel) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.PACKAGE_ID, parcelModel.getParcelId());
        editor.commit();
        receiverName.setText(parcelModel.getReceiverName());
        receiverlocation.setText(parcelModel.getFlatNumber() + ", " + parcelModel.getCity() + ", " + parcelModel.getState() + ", " + parcelModel.getPinCode());
        receiverNumber.setText(parcelModel.getReceiverPhone());


    }

    private void setVisibilityForWidget(ParcelModel parcelModel) {
        if (preferences.getBoolean(Constants.IS_RIDE, false)) {
            viewPArcel.setVisibility(View.GONE);
            parcelCount.setVisibility(View.GONE);
            parcelStateLayout.setVisibility(View.GONE);
            parcelLayout.setVisibility(View.GONE);
        } else {
            viewPArcel.setVisibility(View.VISIBLE);
            parcelCount.setVisibility(View.VISIBLE);
            if (parcelModel.getStatus().equalsIgnoreCase(PackageStatus.DELIVERED.toString()) ||
                    parcelModel.getStatus().equalsIgnoreCase(PackageStatus.CANCELLED.toString())) {
                parcelStateLayout.setVisibility(View.GONE);
            } else {
                parcelStateLayout.setVisibility(View.VISIBLE);
            }
            parcelLayout.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        handler = new Handler();
        progressDialog = new ProgressDialog(this);
        m_handler = new Handler();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(RideStatus.ONTRIP.toString());
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        endRide = (TextView) findViewById(R.id.end_ride);
        cancelledPackage = (Button) findViewById(R.id.cancelled_package);
        deliveredPackage = (Button) findViewById(R.id.delivered_package);
        userName = (TextView) findViewById(R.id.user_name);
        userPic = (ImageView) findViewById(R.id.user_pic);
        call = (ImageButton) findViewById(R.id.call);
        viewPArcel = (ImageView) findViewById(R.id.view_parcel);
        pickupAddress = (TextView) findViewById(R.id.pickup_address);

        receiverName = (TextView) findViewById(R.id.receiver_name);
        receiverlocation = (TextView) findViewById(R.id.receiver_location);
        receiverNumber = (TextView) findViewById(R.id.receiver_number);
        parcelCount = (TextView) findViewById(R.id.count);
        parcelLayout = (LinearLayout) findViewById(R.id.parcel_layout);
        parcelStateLayout = (LinearLayout) findViewById(R.id.parcel_state_layout);
        viewPArcel.setOnClickListener(this);
        parcelCount.setOnClickListener(this);
        call.setOnClickListener(this);
        cancelledPackage.setOnClickListener(this);
        deliveredPackage.setOnClickListener(this);
        receiverNumber.setOnClickListener(this);
        endRide.setOnClickListener(this);

    }

    private void initilizeMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.googleMap)).getMap();
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
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
                    try {
                        progressDialog.show();
                        latLng = currentLatLng();
                        Log.e("latlng", latLng + "");
                        m_handler.postDelayed(m_handlerTask, 3000);
                        if (Double.compare(latLng.latitude, 0.0) == 0) {
                        } else {
//                        getETA(latLng);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13);
                            googleMap.animateCamera(cameraUpdate);
//                        pickupMarker = AddMarker.addMarker(googleMap, pickupMarker, latLng, R.drawable.location, "");

                            progressDialog.dismiss();
                            m_handler.removeMessages(0);
                            m_handler.removeCallbacks(m_handlerTask);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // do something


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


            setIsRide(jsonObject.getString("type"));

            pickupAddress.setText(jsonObject.getString("from_address"));
            LatLng pickupLatLng = new LatLng(jsonObject.getDouble("from_lat"), jsonObject.getDouble("from_lon"));
            pickupMarker = AddMarker.addMarker(googleMap, pickupMarker, pickupLatLng, R.drawable.location_icon, "");
            if (Double.compare(lat, 0.0) == 0) {
            } else {
                currentMarker = AddMarker.addMarker(googleMap, currentMarker, new LatLng(lat, lng), R.drawable.current_marker, "");
            }
            parcelCount.setText(jsonObject.getInt("no_of_packages") + "");

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pickupLatLng, 14);
            googleMap.animateCamera(cameraUpdate);


            userName.setText(userObject.getString("first_name") + " " + userObject.getString("last_name"));
            if (userObject.has("pic_url") && userObject.getString("pic_url").length() > 0)
                Picasso.with(OnTrip.this).load(userObject.getString("pic_url")).fit().placeholder(R.drawable.pro_pic).into(userPic);

            phoneNumber = userObject.getString("phone");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.view_parcel:
                viewPArcelList();
                break;
            case R.id.count:
                viewPArcelList();
                break;
            case R.id.call:
                Utils.makeCall(OnTrip.this, phoneNumber);
                break;
            case R.id.cancelled_package:
                updatePackageStatusRequest(PackageStatus.CANCELLED.toString());
                break;
            case R.id.delivered_package:
                updatePackageStatusRequest(PackageStatus.DELIVERED.toString());
                break;
            case R.id.end_ride:
                makeUpdateRideStatusRequest(RideStatus.ENDRIDE.toString());
                break;


        }
    }

    private void makeUpdateRideStatusRequest(final String rideStatus) {
        final ProgressDialog progressDialog = new ProgressDialog(OnTrip.this);
        progressDialog.setMessage("loading ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String url = Urls.RIDE_STATE_UPDATE;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Ride status update response", response);
                if (rideStatus.equalsIgnoreCase(RideStatus.ENDRIDE.toString())) {
                    Intent intent = new Intent(OnTrip.this, ReceiptScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
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
                params.put("state", rideStatus);
                params.put("ride_id", preferences.getLong(Constants.RIDE_ID, -1) + "");
                params.put("is_user_ride", preferences.getBoolean(Constants.IS_RIDE, false) + "");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);
    }


    private void updatePackageStatusRequest(final String packageStatus) {
        final ProgressDialog progressDialog = new ProgressDialog(OnTrip.this);
        progressDialog.setMessage("loading ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String url = Urls.PACKAGE_STATE_UPDATE;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Response", response);
                Intent intent = new Intent(OnTrip.this, ViewParcel.class);
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
                params.put("package_id", preferences.getLong(Constants.PACKAGE_ID, -1) + "");
                params.put("state", packageStatus);
                Log.e("Package status params", params.toString());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);
    }

    private void viewPArcelList() {
        Intent intent = new Intent(OnTrip.this, ViewParcel.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }

    private void setIsRide(String requestType) {
        SharedPreferences.Editor editor = preferences.edit();
        if (requestType.equalsIgnoreCase("PACKAGE")) {
            editor.putBoolean(Constants.IS_RIDE, false);
            viewPArcel.setVisibility(View.VISIBLE);
            parcelCount.setVisibility(View.VISIBLE);
        } else {
            editor.putBoolean(Constants.IS_RIDE, true);
            viewPArcel.setVisibility(View.GONE);
            parcelCount.setVisibility(View.GONE);
        }
        editor.commit();

    }

    @Override
    public void onResume() {
        super.onResume();
        getRideByRide();

        updateDriverLocation();

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(0);
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

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(runnable);
        handler.removeMessages(0);
    }
}
