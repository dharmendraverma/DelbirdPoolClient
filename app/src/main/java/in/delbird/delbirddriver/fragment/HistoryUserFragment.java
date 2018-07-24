package in.delbird.delbirddriver.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.adapter.HistoryAdapter;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.enums.RideStatus;
import in.delbird.delbirddriver.model.HistoryModel;
import in.delbird.delbirddriver.utils.Show;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dharmendra on 28/1/16.
 */
public class HistoryUserFragment extends Fragment implements AbsListView.OnScrollListener {
    ArrayList<HistoryModel> userArrayList = new ArrayList<>();
    ListView UserList;

    HistoryAdapter adapter;
    SharedPreferences preferences;
    long lastRequestTime;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.history_userfragment, container, false);
        preferences = getActivity().getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        lastRequestTime = System.currentTimeMillis();
        UserList = (ListView) v.findViewById(R.id.user_list);
        adapter = new HistoryAdapter(getActivity(), userArrayList);
        UserList.setAdapter(adapter);

        UserList.setOnScrollListener(this);
        getUserResponse(lastRequestTime);

        return v;
    }

    private void getUserResponse(long endTripTime) {
        lastRequestTime = endTripTime;
        final ProgressDialog mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("Loading Your History");
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.POST, Urls.GET_RIDES_HISTORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Show.showLog("USER HISTORY", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array_package = jsonObject.getJSONArray("UserRides");
                    for (int i = 0; i < array_package.length(); i++) {
                        JSONObject packageObject = array_package.optJSONObject(i);
                        HistoryModel history = new HistoryModel();

                        history.setId(packageObject.getLong("id"));
                        if (packageObject.has("creation_time") && !packageObject.isNull("creation_time"))
                            history.setCreationTime(packageObject.getLong("creation_time"));
                        history.setRideType("Ride");
                        if (packageObject.has("state") && !packageObject.isNull("state")) {
                            if (RideStatus.CANCELLED.toString().equalsIgnoreCase(packageObject.getString("state"))) {
                                history.setStatus("Cancelled");
                            } else {
                                history.setStatus("Delivered");
                            }
                        }
                        if (packageObject.has("ride_cost")) {
                            history.setCost(packageObject.getLong("ride_cost"));
                            history.setPaymentMode(packageObject.getString("payment_mode"));
                        }
                        if (packageObject.has("user") && !packageObject.isNull("user")) {
                            JSONObject userObject = packageObject.getJSONObject("user");
                            if (userObject.has("pic_url") && userObject.getString("pic_url").length() > 0)
                                history.setPicUrl(userObject.getString("pic_url"));
                        }
                        history.setEndTripTime(packageObject.getLong("endtrip_time"));
                        userArrayList.add(history);
                    }
                    adapter.notifyDataSetChanged();
                    mProgressDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    mProgressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "Unable to parse json array");
                mProgressDialog.dismiss();
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
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Show.showLog("firstVisibleItem", firstVisibleItem + " ");
        Show.showLog("visibleItemCount", visibleItemCount + " ");
        Show.showLog("totalItemCount", totalItemCount + " ");
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        Show.showLog("Last Visible Item", lastVisibleItem + "");

        if (userArrayList.size() > 0
                && !(lastRequestTime == userArrayList.get(userArrayList.size() - 1).getEndTripTime())) {
            if (lastVisibleItem == totalItemCount) {
                Show.showLog("Last Visible Item", lastVisibleItem + "");
                getUserResponse(userArrayList.get(userArrayList.size() - 1).getEndTripTime());
            }
        }
    }
}
