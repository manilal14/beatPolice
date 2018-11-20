package com.example.mani.beatpolice.LoginRelated;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mani.beatpolice.HomePage;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
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
    private LoginSessionManager mLoginSessionManager;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        Log.e(TAG,"called : onCreate");

        getSupportActionBar().hide();
        mProgressDialog = new ProgressDialog(LoginPage.this);
        mProgressDialog.setMessage("Please wait");

        mLoginSessionManager = new LoginSessionManager(LoginPage.this);

        TextView login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCredentails();
            }
        });



    }

    private void verifyCredentails() {

        Log.e(TAG, "called : verifyCredentials");
        AutoCompleteTextView et_policeId = findViewById(R.id.police_id);
        final EditText et_password = findViewById(R.id.password);

        final String policeId = et_policeId.getText().toString();
        final String password = et_password.getText().toString();

        if (policeId.equals("") || password.equals("")) {
            Toast.makeText(LoginPage.this, "Insert both field", Toast.LENGTH_SHORT).show();
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

                    String aId    = jsonObject.getString("a_id");
                    String name   = jsonObject.getString("p_name");
                    String phone  = jsonObject.getString("p_phone");

                    String pic    = jsonObject.getString("p_pic");

                    String aName  = jsonObject.getString("a_name");
                    String des    = jsonObject.getString("a_des");
                    String coord  = jsonObject.getString("coord");

                    Log.e("response ",aId+name+phone+aName+des+coord);

                    mLoginSessionManager.createLoginSession(policeId,password,name,phone,pic,aId,aName,des,coord);

                    startActivity(new Intent(LoginPage.this,HomePage.class));
                    finish();


                } catch (JSONException e) {
                    e.printStackTrace();
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

                params.put("p_id",policeId);
                params.put("pass",password);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy( RETRY_SECONDS, NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(LoginPage.this).addToRequestQueue(stringRequest);

    }


}
