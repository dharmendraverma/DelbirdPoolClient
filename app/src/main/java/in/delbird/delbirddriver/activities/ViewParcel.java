package in.delbird.delbirddriver.activities;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.adapter.ParcelAdapter;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Messages;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.customview.CircularImageView;
import in.delbird.delbirddriver.enums.RideStatus;
import in.delbird.delbirddriver.model.ParcelModel;
import in.delbird.delbirddriver.utils.Show;
import in.delbird.delbirddriver.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shahil on 1/28/16.
 */
public class ViewParcel extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    Toolbar toolbar;
    ListView parcelList;
    ArrayList<ParcelModel> parcelModelArrayList = new ArrayList<>();
    ParcelAdapter parcelAdapter;
    SharedPreferences preferences;
    TextView endRide, noParcel;
    ImageButton call;
    CircularImageView userPic;
    TextView userName;
    LinearLayout parent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_parcel);

        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("All Parcel");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        parent = (LinearLayout) findViewById(R.id.parent);
        userPic = (CircularImageView) findViewById(R.id.user_pic);
        userName = (TextView) findViewById(R.id.user_name);
        call = (ImageButton) findViewById(R.id.call);
        noParcel = (TextView) findViewById(R.id.no_parcel_found);
        endRide = (TextView) findViewById(R.id.end_ride);
        parcelList = (ListView) findViewById(R.id.parcel_list);
        parcelAdapter = new ParcelAdapter(this, parcelModelArrayList);
        parcelList.setAdapter(parcelAdapter);
        parcelList.setOnItemClickListener(this);
        endRide.setOnClickListener(this);
        getParcelList();
        if (preferences.getString(Constants.USER_PIC, "").length() > 0)
            Picasso.with(ViewParcel.this).load(preferences.getString(Constants.USER_PIC, "")).placeholder(R.drawable.pro_pic).fit().into(userPic);
        userName.setText(preferences.getString(Constants.USER_NAME, ""));

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.makeCall(ViewParcel.this, preferences.getString(Constants.USER_PHONE, ""));
            }
        });
    }

    private void getParcelList() {
        String url = Urls.GET_PARCEL_LIST;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("get parcel list response", response);
                try {
                    parcelModelArrayList.clear();
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ParcelModel parcelModel = new ParcelModel();
                        parcelModel.setParcelId(jsonObject.getLong("id"));
                        parcelModel.setParcelName(jsonObject.getString("name"));
                        parcelModel.setRideId(jsonObject.getLong("ride_id"));
                        parcelModel.setReceiverName(jsonObject.getString("reciever_name"));
                        parcelModel.setReceiverPhone(jsonObject.getString("phone_of_reciever"));
                        if (jsonObject.has("is_delivered"))
                            parcelModel.setIsDelivered(jsonObject.getBoolean("is_delivered"));
                        parcelModel.setFlatNumber(jsonObject.getString("flat_number"));
                        parcelModel.setCity(jsonObject.getString("city"));
                        parcelModel.setState(jsonObject.getString("state"));
                        parcelModel.setPinCode(jsonObject.getString("pincode"));
                        if (jsonObject.has("status"))
                            parcelModel.setStatus(jsonObject.getString("status"));
                        parcelModelArrayList.add(parcelModel);
                    }
                    if (parcelModelArrayList.size() > 0) {
                        parcelList.setVisibility(View.VISIBLE);
                        noParcel.setVisibility(View.GONE);
                    } else {
                        parcelList.setVisibility(View.GONE);
                        noParcel.setVisibility(View.VISIBLE);

                    }
                    parcelAdapter.notifyDataSetChanged();
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
                Show.showLog("GET parcel list params", params.toString());
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ViewParcel.this, OnTrip.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ViewParcel.this, OnTrip.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putSerializable("parcel", parcelModelArrayList.get(position));
        intent.putExtra("bundle", bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.end_ride) {

            AlertDialog.Builder builder = new AlertDialog.Builder(ViewParcel.this);
            builder.setTitle(Messages.END_RIDE);
            builder.setMessage(Messages.RIDE_END);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    makeUpdateRideStatusRequest(RideStatus.ENDRIDE.toString());

                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();


        }
    }

    private void makeUpdateRideStatusRequest(final String rideStatus) {
        final ProgressDialog progressDialog = new ProgressDialog(ViewParcel.this);
        progressDialog.setMessage("loading ...");
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
                        Show.showSnackbar(parent, jsonObject.getString("reason"));
                    } else {
                        if (rideStatus.equalsIgnoreCase(RideStatus.ENDRIDE.toString())) {
                            Intent intent = new Intent(ViewParcel.this, ReceiptScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
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

    @Override
    public void onResume() {
        super.onResume();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(0);
    }
}
