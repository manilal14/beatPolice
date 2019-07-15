package com.example.mani.beatpolice.LoginRelated;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefTodo {

    private static final String SHARED_PREF_TODO = "sharedprefTodo";
    private static final String KEY_SLOT_ZERO_TIME_FOR_TODO = "slotZeroTimeForTodo";

    private static Context mCtx;
    private static SharedPrefTodo mInstance;

    private SharedPrefTodo(Context context){
        mCtx = context;
    }

    public static synchronized SharedPrefTodo getmInstance(Context context){
        if(mInstance == null)
            mInstance = new SharedPrefTodo(context);
        return mInstance;
    }

    public boolean storeZeroSlotTime(String time){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_TODO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SLOT_ZERO_TIME_FOR_TODO,time);
        editor.apply();
        return true;
    }


    public String getZeroTimeSlotForTodo(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_TODO,Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SLOT_ZERO_TIME_FOR_TODO,"");
    }

}
