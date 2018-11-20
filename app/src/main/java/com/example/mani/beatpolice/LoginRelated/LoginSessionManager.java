package com.example.mani.beatpolice.LoginRelated;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.HashMap;

public class LoginSessionManager {

    private Context mCtx;
    private SharedPreferences pref;
    private int PRIVATE_MODE = 0;
    private SharedPreferences.Editor editor;

    private final String PREF_NAME   = "LoginPreference";
    private final String IS_LOGIN    = "IsLoggedIn";

    public static final String KEY_POLICE_ID  = "policeId";
    public static final String KEY_PASSWORD   = "password";
    public static final String KEY_NAME       = "name";
    public static final String KEY_PHONE      = "phone";
    public static final String KEY_PIC        = "pic";
    public static final String KEY_A_ID       = "aId";
    public static final String KEY_AREA       = "aName";
    public static final String KEY_DES        = "aDes";
    public static final String KEY_COORD      = "aCoord";

    public LoginSessionManager(Context context){
        mCtx = context;
        pref = mCtx.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String policeId, String password, String name, String phone, String pic,
                                   String aId, String aName, String aDes,String aCoord){

        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_POLICE_ID,policeId);
        editor.putString(KEY_PASSWORD,password);
        editor.putString(KEY_NAME,name);
        editor.putString(KEY_PHONE,phone);

        editor.putString(KEY_PIC,pic);

        editor.putString(KEY_A_ID,aId);
        editor.putString(KEY_AREA,aName);
        editor.putString(KEY_DES,aDes);
        editor.putString(KEY_COORD,aCoord);

        editor.commit();
    }

    public void checkLogin() {

        if (!this.isLoggedIn()) {

            Intent i = new Intent(mCtx, LoginPage.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mCtx.startActivity(i);
        }
    }

    public void logout(){

        editor.clear();
        editor.commit();


        Intent i = new Intent(mCtx, LoginPage.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        Toast.makeText(mCtx,"Logged Out",Toast.LENGTH_SHORT).show();
        mCtx.startActivity(i);
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public HashMap<String,String> getPoliceDetailsFromPref(){

        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_POLICE_ID, pref.getString(KEY_POLICE_ID, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));

        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_PHONE, pref.getString(KEY_PHONE, null));
        user.put(KEY_PIC, pref.getString(KEY_PIC, ""));

        user.put(KEY_A_ID, pref.getString(KEY_A_ID, null));
        user.put(KEY_AREA, pref.getString(KEY_AREA, null));
        user.put(KEY_DES, pref.getString(KEY_DES, null));
        user.put(KEY_COORD, pref.getString(KEY_COORD, null));

        return user;
    }

    public void updateProfilePicName(String picName){

        editor.putString(KEY_PIC,picName);
        editor.commit();

    }



}
