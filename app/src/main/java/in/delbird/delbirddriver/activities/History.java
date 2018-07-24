package in.delbird.delbirddriver.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.adapter.HistoryAdapter;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.enums.RideStatus;
import in.delbird.delbirddriver.model.HistoryModel;
import in.delbird.delbirddriver.utils.Show;

/**
 * Created by shahil on 3/8/16.
 */
public class History extends AppCompatActivity implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    ListView history_list;
    ArrayList<HistoryModel> packagesList = new ArrayList<>();
    HistoryAdapter adapter;
    long lastRequestTime;
    SharedPreferences preferences;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_packagescreen);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("History");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lastRequestTime = System.currentTimeMillis();
        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        history_list = (ListView) findViewById(R.id.history_list);
        history_list.setOnItemClickListener(this);
        adapter = new HistoryAdapter(this, packagesList);
        history_list.setAdapter(adapter);
        history_list.setOnScrollListener(this);
        getHistoryResponse(lastRequestTime);
    }

    public void getHistoryResponse(long endTripTime) {
        lastRequestTime = endTripTime;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Your History");
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.POST, Urls.GET_RIDES_HISTORY, new Response.Listener<String>() {
            public void onResponse(String response) {

                Show.showLog("USER HISTORY", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array_package = jsonObject.getJSONArray("PackageRides");
                    for (int i = 0; i < array_package.length(); i++) {
                        JSONObject packageObject = array_package.optJSONObject(i);
                        HistoryModel history = new HistoryModel();
                        history.setId(packageObject.getLong("id"));
                        if (packageObject.has("creation_time") && !packageObject.isNull("creation_time"))
                            history.setCreationTime(packageObject.getLong("creation_time"));
                        history.setRideType("Package");
                        if (packageObject.has("state") && !packageObject.isNull("state")
                                && packageObject.getString("state").equalsIgnoreCase(RideStatus.CANCELLED.toString())) {
                            history.setStatus("Cancelled");
                        } else {
                            history.setStatus("Delivered");
                        }
                        if (packageObject.has("ride_cost")) {
                            history.setCost(packageObject.getLong("ride_cost"));
                            history.setPaymentMode(packageObject.getString("payment_mode"));
                        }

                        if (packageObject.has("user") && !packageObject.isNull("user")) {
                            JSONObject userObject = packageObject.getJSONObject("user");
                            history.setPicUrl(userObject.getString("pic_url"));
                        }
                        history.setEndTripTime(packageObject.getLong("endtrip_time"));
                        packagesList.add(history);
                    }
                    if (packagesList.size() > 0)
                        lastRequestTime = packagesList.get(packagesList.size() - 1).getEndTripTime();
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("Error", "Unable to parse json array");
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("driver_id", preferences.getLong(Constants.DRIVER_ID, -1) + "");
                params.put("time", lastRequestTime + "");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(History.this, ViewHistoryParcel.class);
        intent.putExtra("rideId", packagesList.get(position).getId());
        startActivity(intent);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Show.showLog("firstVisibleItem", firstVisibleItem + " ");
        Show.showLog("visibleItemCount", visibleItemCount + " ");
        Show.showLog("totalItemCount", totalItemCount + " ");
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        Show.showLog("Last Visible Item", lastVisibleItem + "");

        if (packagesList.size() > 0 && !(lastRequestTime == packagesList.get(packagesList.size() - 1).getEndTripTime())) {
            if (lastVisibleItem == totalItemCount) {
                Show.showLog("Last Visible Item", lastVisibleItem + "");
                getHistoryResponse(packagesList.get(packagesList.size() - 1).getEndTripTime());
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(History.this, HomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
