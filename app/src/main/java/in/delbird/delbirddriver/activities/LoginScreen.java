package in.delbird.delbirddriver.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.controller.AppController;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Messages;
import in.delbird.delbirddriver.controller.Urls;
import in.delbird.delbirddriver.utils.Show;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dharmendra on 1/28/16.
 */
public class LoginScreen extends AppCompatActivity implements View.OnClickListener {
    Button loginButton;
    EditText phoneNumberET, passwordET;
    LinearLayout parent;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        preferences = getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        parent = (LinearLayout) findViewById(R.id.parent);
        phoneNumberET = (EditText) findViewById(R.id.phone_number);
        passwordET = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.login_button:
                if (phoneNumberET.getText().toString().length() == 10 && passwordET.getText().toString().length() > 0) {
                    makeLoginRequest();
                } else {
                    Show.showSnackbar(parent, Messages.INVALID_CREDENTIALS);
                }


                break;
        }
    }

    private void makeLoginRequest() {
        String url = Urls.DRIVER_LOGIN;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Show.showLog("Login Response", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.has("status") && jsonObject.getString("status").equalsIgnoreCase("fail")) {
                        Show.showSnackbar(parent, jsonObject.getString("reason"));
                    } else {
                        String driver = jsonObject.getString("driver");
                        JSONObject driverObject = new JSONObject(driver);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(Constants.IS_LOGGED_IN, true);
                        editor.putString(Constants.DRIVER, jsonObject.getString("driver"));
                        editor.putLong(Constants.DRIVER_ID, driverObject.getLong("id"));
                        editor.putString(Constants.DRIVER_NAME, driverObject.getString("name"));
                        editor.putString(Constants.DRIVER_PIC, driverObject.getString("pic_url"));
                        editor.putString(Constants.DRIVER_STATUS,driverObject.getString("status"));
                        if (driverObject.getString("status").equalsIgnoreCase("online")) {
                            editor.putBoolean(Constants.IS_BIKE_ONLINE, true);
                        } else {
                            editor.putBoolean(Constants.IS_BIKE_ONLINE, false);
                        }
                        editor.putString(Constants.BIKE, jsonObject.getString("Bike"));
                        editor.commit();
                        Intent intent = new Intent(LoginScreen.this, HomeScreen.class);
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
                params.put("phone", phoneNumberET.getText().toString());
                params.put("password", passwordET.getText().toString());
                params.put("gcm_id", preferences.getString("gcm", ""));
                Log.e("Login params", params.toString());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);
    }
}
