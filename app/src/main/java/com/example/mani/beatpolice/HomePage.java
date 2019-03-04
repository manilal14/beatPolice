package com.example.mani.beatpolice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.FCMPackage.SharedPrefFcm;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.SyncRelated.SyncHomePage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_POLICE_ID;

public class HomePage extends AppCompatActivity  {


    private  String TAG = this.getClass().getSimpleName();
    Fragment mFragmentMap;
    Fragment mFragmentProfile;

    private final  String TERMINATE_URL = BASE_URL + "terminate_allotment.php";

    private boolean mIsProfileActive = false;


    LoginSessionManager mSession;
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Log.e(TAG, "Crashlytics is initialised");
//        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());

        setContentView(R.layout.activity_home_page);
        Log.e(TAG, "called : onCreate");
        mSession = new LoginSessionManager(HomePage.this);

        if(!mSession.isLoggedIn()){
            mSession.checkLogin();
            finish();
            return;
        }

        String token = SharedPrefFcm.getmInstance(HomePage.this).getToken();
        if(token!=null){
            Log.e(TAG,"Fcm token from sharedPref: "+token);
            storeTokenToDb(token);
        }

        clickLister();
        mFragmentMap      = new FragmentMap();
        mFragmentProfile  = new FragmentProfile();
        loadFragment(mFragmentMap,false);


    }

    private void clickLister() {

        Log.e(TAG, "called : clickListener");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id){
                    case R.id.nav_home:
                        loadFragment(mFragmentMap,false);
                        return true;

                    case R.id.nav_sync:
                        startActivity(new Intent(HomePage.this,SyncHomePage.class));
                        return true;
                    case R.id.nav_profile:
                        loadFragment(mFragmentProfile,true);
                        return true;
                }

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    public void loadFragment(Fragment fragment, boolean isProfileActive) {

        Log.e(TAG, "called : loadFragment");

        mIsProfileActive = isProfileActive;

        String fragmentTag = fragment.getClass().getName();
        mFragmentManager   = getSupportFragmentManager();


        boolean fragmentPopped = mFragmentManager.popBackStackImmediate(fragmentTag , 0);

        if (!fragmentPopped && mFragmentManager.findFragmentByTag(fragmentTag) == null) {

            FragmentTransaction ftx = mFragmentManager.beginTransaction();
            ftx.replace(R.id.frame_container, fragment);
            ftx.commit();
        }
    }

    @Override
    public void onBackPressed() {

        Log.e(TAG, "called : onBackPressed");

        if(mIsProfileActive) {
            loadFragment(new FragmentMap(), false);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
        else
            super.onBackPressed();
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }

    private void storeTokenToDb(final String refreshedToken) {

        final String policeId = new LoginSessionManager(HomePage.this).getPoliceDetailsFromPref().get(KEY_POLICE_ID);
        final String URL_TOKEN = BASE_URL + "store_police_token.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_TOKEN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int rc = jsonArray.getJSONObject(0).getInt("rc");

                    if(rc==1)
                        Log.e(TAG,"refreshed token is send to db " + refreshedToken);
                    else
                        Log.e(TAG,"refreshed token cant be send ");


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Exception cougnt "+e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("p_id",policeId);
                params.put("fcm_token",refreshedToken);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
