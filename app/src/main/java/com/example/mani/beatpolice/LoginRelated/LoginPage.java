package com.example.mani.beatpolice.LoginRelated;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.HomePage;
import com.example.mani.beatpolice.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;

public class LoginPage extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private LoginSessionManager mSession;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_page);

        Log.e(TAG,"called : onCreate");

        //getSupportActionBar().hide();
        mProgressDialog = new ProgressDialog(LoginPage.this);
        mProgressDialog.setMessage("Please wait");

        mSession = new LoginSessionManager(LoginPage.this);
        
        getMobileUniqueNumber();

        TextView login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCredentials();
            }
        });
    }

    private void getMobileUniqueNumber() {
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.e("android id=",androidId);
    }

    private void verifyCredentials() {

        Log.e(TAG, "called : verifyCredentials");

        EditText et_mobile         = findViewById(R.id.mobile);
        final EditText et_password = findViewById(R.id.password);

        final String phone         = et_mobile.getText().toString().trim();
        final String password      = et_password.getText().toString().trim();

        if (phone.equals("") || password.equals("")) {
            Toast.makeText(LoginPage.this, "Insert both field", Toast.LENGTH_SHORT).show();
            return;
        }

        if(phone.length()!=10){
            Toast.makeText(LoginPage.this, "Phone number not valid", Toast.LENGTH_SHORT).show();
            et_mobile.setText("");
            et_password.setText("");
            return;
        }

        String LOGIN_URL = BASE_URL + "login.php";

        mProgressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e(TAG,response);
                mProgressDialog.dismiss();

                try {

                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    int response_code = jsonObject.getInt("response_code");

                    if(response_code<=0){
                        String message = jsonObject.optString("message");
                        Toast.makeText(LoginPage.this,message,Toast.LENGTH_SHORT).show();
                        et_password.setText("");
                        return;
                    }

                    String name  = jsonObject.getString("p_name");
                    String pid   = jsonObject.getString("p_pid");
                    String pic   = jsonObject.getString("p_pic");

                    mSession.createLoginSession(pid,password,name,phone,pic);

                    startActivity(new Intent(LoginPage.this,HomePage.class));
                    finish();


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"verifyCredentials : Exception cought  " + e );
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                Log.e("error",error.toString());
                Toast.makeText(LoginPage.this,"Login failed",Toast.LENGTH_SHORT).show();
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<>();

                params.put("phone",phone);
                params.put("pass",password);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy( RETRY_SECONDS*1000, NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(LoginPage.this).addToRequestQueue(stringRequest);

    }


}
