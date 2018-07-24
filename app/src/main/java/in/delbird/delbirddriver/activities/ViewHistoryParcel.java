package in.delbird.delbirddriver.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.adapter.ParcelAdapter;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.model.ParcelModel;
import in.delbird.delbirddriver.utils.Show;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shahil on 2/10/16.
 */
public class ViewHistoryParcel extends AppCompatActivity {
    Toolbar toolbar;
    ListView parcelList;
    ArrayList<ParcelModel> parcelModelArrayList = new ArrayList<>();
    ParcelAdapter parcelAdapter;
    SharedPreferences preferences;
    long rideId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_history_parcel);

        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("All Parcel");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        parcelList = (ListView) findViewById(R.id.parcel_list);

        parcelAdapter = new ParcelAdapter(this, parcelModelArrayList);
        parcelList.setAdapter(parcelAdapter);

        Intent intent = this.getIntent();
        if (intent != null) {
            rideId = intent.getLongExtra("rideId", -1);
        }

        getParcelList();
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
                        parcelModel.setIsOptionButtonDisable(true);
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
                params.put("ride_id", rideId + "");
                Show.showLog("GET parcel list params", params.toString());
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ViewHistoryParcel.this, History.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
}
