package com.example.mani.beatpolice.FCMPackage;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefFcm {

    private static final String SHARED_PREF_FCM = "sharedpreffcm";
    private static final String KEY_ACCESS_TOKEN = "token";

    private static Context mCtx;
    private static SharedPrefFcm mInstance;

    private SharedPrefFcm(Context context){
        mCtx = context;
    }

    public static synchronized SharedPrefFcm getmInstance(Context context){
        if(mInstance == null)
            mInstance = new SharedPrefFcm(context);
        return mInstance;
    }

    public boolean storeToken(String token){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_FCM, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN,token);
        editor.apply();
        return true;
    }


    public String getToken(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_FCM,Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ACCESS_TOKEN,null);
    }

}




