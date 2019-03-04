package com.example.mani.beatpolice.SyncRelated;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mani.beatpolice.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSyncIssues extends Fragment {


    public FragmentSyncIssues() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sync_issues, container, false);
    }

}
