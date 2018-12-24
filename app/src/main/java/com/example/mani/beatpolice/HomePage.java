package com.example.mani.beatpolice;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;

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

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }


}
