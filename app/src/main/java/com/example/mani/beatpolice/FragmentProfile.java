package com.example.mani.beatpolice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;

import java.util.HashMap;

import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_AREA;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_NAME;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_PHONE;


public class FragmentProfile extends Fragment {

    private  String TAG = "FragmentProfile";
    private HomePage mActivity;

    private LoginSessionManager mSession;

    public FragmentProfile() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "called : onCreate");

        mActivity = (HomePage) getActivity();
        mActivity.getSupportActionBar().setTitle("Profile");
        mActivity.getSupportActionBar().show();

        mSession = new LoginSessionManager(mActivity);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(TAG, "called : onCreateView");
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        EditText et_name  = view.findViewById(R.id.name);
        EditText et_pass  = view.findViewById(R.id.password);
        EditText et_area  = view.findViewById(R.id.area);
        EditText et_phone = view.findViewById(R.id.phone);

        HashMap<String, String> info = mSession.getPoliceDetailsFromPref();

        et_name.setText(info.get(KEY_NAME));
        et_area.setText(info.get(KEY_AREA));
        et_phone.setText(info.get(KEY_PHONE));




        return view;
    }

}
