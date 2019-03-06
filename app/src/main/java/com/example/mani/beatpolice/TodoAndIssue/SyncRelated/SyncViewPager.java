package com.example.mani.beatpolice.TodoAndIssue.SyncRelated;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class SyncViewPager extends FragmentPagerAdapter {

    List<Fragment> mFragmentList;

    public SyncViewPager(FragmentManager fm, List<Fragment> mFragmentList) {
        super(fm);
        this.mFragmentList = mFragmentList;
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0: return mFragmentList.get(0);
            case 1: return mFragmentList.get(1);
        }
        return null;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0: return "Todo";
            case 1: return "Issues";

        }

        return null;
    }
}
