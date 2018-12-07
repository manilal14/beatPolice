package com.example.mani.beatpolice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_ALLOT_ID;

public class HomePage extends AppCompatActivity  {

    private  String TAG = this.getClass().getSimpleName();
    Fragment mFragmentMap;
    Fragment mFragmentProfile;

    private final  String TERMINATE_URL = BASE_URL + "terminate_allotment.php";


    LoginSessionManager mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Log.e(TAG, "called : onCreate");

        mSession = new LoginSessionManager(HomePage.this);

        if(!mSession.isLoggedIn()){
            mSession.checkLogin();
            finish();
            return;
        }

        clickLister();

        mFragmentMap        = new FragmentMap();
        mFragmentProfile    = new FragmentProfile();

        loadFragment(mFragmentMap);


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
                        loadFragment(mFragmentMap);
                        return true;
                    case R.id.nav_add_tag:return true;
                    case R.id.nav_note:
                        return true;
                    case R.id.nav_profile:
                        loadFragment(mFragmentProfile);
                        return true;

                }

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_homepage,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.logout: mSession.logout(); finish(); return true;
//            case R.id.terminate:
//                openDialogBox();
//                return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {

        Log.e(TAG, "called : loadFragment");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "called : onBackPressed");
        super.onBackPressed();
    }

    private void openDialogBox(){

        AlertDialog.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(HomePage.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(HomePage.this);
        }

        builder.setTitle("Want to terminate this Beat ?")
                .setMessage("All details related to beat area will be removed from App and you will be logged out.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        terminateBeat();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }

    private void terminateBeat(){

        Log.e(TAG,"called : terminateBeat");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, TERMINATE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"terminateBeat - response : "+ response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jb = jsonArray.getJSONObject(0);

                    int rc = jb.getInt("response_code");
                    if(rc<=0){
                        Toast.makeText(HomePage.this,"Beat can't be terminated",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(HomePage.this,"Beat Successfully terminated",Toast.LENGTH_SHORT).show();
                    mSession.logout();
                    finish();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"onResponse : "+error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Log.e(TAG,"getParams");
                HashMap<String,String> params = new HashMap<>();

                String allotId = mSession.getAllotmentDetails().get(KEY_ALLOT_ID);
                Log.e("we2","allot_id - "+allotId);

                params.put("allot_id",allotId);
                return params;

            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(HomePage.this).addToRequestQueue(stringRequest);


    }


}
