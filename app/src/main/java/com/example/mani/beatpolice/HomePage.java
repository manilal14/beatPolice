package com.example.mani.beatpolice;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;

public class HomePage extends FragmentActivity{

    private  String TAG = this.getClass().getSimpleName();
    Fragment mFragmentMap;
    Fragment mFragmentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Log.e(TAG, "called : onCreate");

        clickLister();

        mFragmentMap  = new FragmentMap();
        mFragmentNote = new FragmentNote();

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
                    case R.id.nav_add_tag: return true;
                    case R.id.nav_note:
                        loadFragment(mFragmentNote);
                        return true;
                    case R.id.nav_profile: return true;

                }

                return false;
            }
        });
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
}
