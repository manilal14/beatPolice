package com.example.mani.beatpolice.TodoAndIssue.SyncRelated;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.mani.beatpolice.R;

import java.util.ArrayList;
import java.util.List;

public class SyncHomePage extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private List<Fragment> mFragmentList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_home_page);


        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Sync Data");
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        mFragmentList = new ArrayList<>();
        mFragmentList.add(new FragmentSyncTodo());
        mFragmentList.add(new FragmentSyncIssues());

        ViewPager viewPager = findViewById(R.id.viewpager_sync);

        SyncViewPager adapter = new SyncViewPager(
                getSupportFragmentManager(),mFragmentList);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout_sync);
        tabLayout.setupWithViewPager(viewPager);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


}
