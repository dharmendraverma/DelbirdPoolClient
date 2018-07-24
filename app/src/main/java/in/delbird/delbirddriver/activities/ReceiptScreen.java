package in.delbird.delbirddriver.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.SplashScreen;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.customview.CircularImageView;
import in.delbird.delbirddriver.enums.PaymentMode;
import in.delbird.delbirddriver.utils.Show;
import in.delbird.delbirddriver.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shahil on 2/4/16.
 */
public class ReceiptScreen extends AppCompatActivity implements View.OnClickListener {
    SharedPreferences preferences;
    Toolbar toolbar;
    TextView button, date, fare, paymentMode;
    CircularImageView userPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt_screen);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Fare Summary");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        date = (TextView) findViewById(R.id.date);
        fare = (TextView) findViewById(R.id.fare);
        userPic = (CircularImageView) findViewById(R.id.user_pic);
        paymentMode = (TextView) findViewById(R.id.payment_mode);
        button = (TextView) findViewById(R.id.submit);
        button.setOnClickListener(this);
        getFareSummary();
    }

    public void getFareSummary() {
        String url = Urls.FARE_SUMMARY;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Fare summary response", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    date.setText(Utils.convertLongTimeToStringDate(jsonObject.getLong("CurrentTime")));
                    fare.setText(jsonObject.getInt("Cost") + "");
                    JSONObject rideObject = new JSONObject(jsonObject.getString("Ride"));
                    if (rideObject.getString("payment_mode").equalsIgnoreCase(PaymentMode.CASH.toString())) {
                        paymentMode.setText(PaymentMode.CASH.toString());
                    } else if (rideObject.getString("payment_mode").equalsIgnoreCase(PaymentMode.Delbird_WALLET.toString())) {
                        paymentMode.setText(PaymentMode.Delbird_WALLET.toString());
                        paymentMode.setVisibility(View.GONE);
                    }
                    JSONObject userObject = rideObject.getJSONObject("user");
                    if (userObject.getString("pic_url").length() > 4) {
                        Picasso.with(ReceiptScreen.this).load(userObject.getString("pic_url")).fit().placeholder(R.drawable.pro_pic).into(userPic);
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
                params.put("id_user_ride", preferences.getBoolean(Constants.IS_RIDE, false) + "");
                Show.showLog("Fara summary response", params.toString());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);
    }


    public void setSuccessfulPayment() {
        String url = Urls.PAYMENT_SUCCESSFULL;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Respon se", response);
                Intent intent = new Intent(ReceiptScreen.this, SplashScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
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
                params.put("is_successful", true + "");
                Show.showLog("params", params.toString());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.submit:
                setSuccessfulPayment();
                break;
        }
    }
}
