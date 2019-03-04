package com.example.mani.beatpolice.LoginRelated;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

public class LoginSessionManager {

    private Context mCtx;
    private SharedPreferences pref;
    private int PRIVATE_MODE = 0;
    private SharedPreferences.Editor editor;

    private final String TAG = "LoginSessionManager";

    private final String PREF_NAME   = "LoginPreference";
    private final String IS_LOGIN    = "IsLoggedIn";

    private final String IS_ALLOTED   = "IsAlloted";
    private final String IS_SAVED_TO_ROOM   = "IsSavedToRoom";

    public static final String KEY_POLICE_ID  = "policeId";
    public static final String KEY_PASSWORD   = "password";
    public static final String KEY_NAME       = "name";
    public static final String KEY_PHONE      = "phone";
    public static final String KEY_PIC        = "pic";

    public static final String KEY_ALLOT_ID   = "allotmentId";
    public static final String KEY_A_ID       = "aId";
    public static final String KEY_A_TIME     = "aTime";
    public static final String KEY_AREA       = "aName";
    public static final String KEY_DES        = "aDes";
    public static final String KEY_COORD      = "aCoord";

    public static final String KEY_AllOT_HIST_ID    = "allotHistId";

    public static final String TODO_UPDATED_ON = "todoUpdatedOn";


    public LoginSessionManager(Context context){
        mCtx = context;
        pref = mCtx.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String policeId, String password, String name, String phone, String pic) {

        editor.putBoolean(IS_LOGIN,true);

        editor.putString(KEY_POLICE_ID,policeId);
        editor.putString(KEY_PASSWORD,password);
        editor.putString(KEY_NAME,name);
        editor.putString(KEY_PHONE,phone);
        editor.putString(KEY_PIC,pic);

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

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Toast.makeText(mCtx,"Logged Out",Toast.LENGTH_SHORT).show();
        mCtx.startActivity(i);
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isAlloted(){
        return pref.getBoolean(IS_ALLOTED, false);
    }

    public boolean isSavedToRoom(){
        return pref.getBoolean(IS_SAVED_TO_ROOM, false);
    }

    public void setSavedToRoom(){
        editor.putBoolean(IS_SAVED_TO_ROOM,true);
        editor.commit();
    }

    public void clearAllotedArea(){

        editor.putBoolean(IS_ALLOTED,false);
        editor.putBoolean(IS_SAVED_TO_ROOM,false);

        editor.putString(KEY_ALLOT_ID,"");
        editor.putString(KEY_A_ID,"");
        editor.putString(KEY_A_TIME,"");
        editor.putString(KEY_AREA,"");
        editor.putString(KEY_DES,"");
        editor.putString(KEY_COORD,"");

        editor.putString(KEY_AllOT_HIST_ID,"");


        editor.commit();
    }

    public HashMap<String,String> getPoliceDetailsFromPref(){

        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_POLICE_ID, pref.getString(KEY_POLICE_ID, ""));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, ""));

        user.put(KEY_NAME, pref.getString(KEY_NAME, ""));
        user.put(KEY_PHONE, pref.getString(KEY_PHONE, ""));
        user.put(KEY_PIC, pref.getString(KEY_PIC, ""));

        return user;
    }

    public void updateProfilePicName(String picName){

        editor.putString(KEY_PIC,picName);

        editor.commit();

    }

    public void saveAllotmentDetails(String id, String a_id, String aTime,String aName,String aDes,String aCoord){

        editor.putBoolean(IS_ALLOTED,false);

        editor.putString(KEY_ALLOT_ID,"");
        editor.putString(KEY_A_ID,"");
        editor.putString(KEY_A_TIME,"");
        editor.putString(KEY_AREA,"");
        editor.putString(KEY_DES,"");
        editor.putString(KEY_COORD,"");

        Log.e(TAG, "called : saveAllotedDetails");
        editor.putBoolean(IS_ALLOTED,true);

        editor.putString(KEY_ALLOT_ID,id);
        editor.putString(KEY_A_ID,a_id);
        editor.putString(KEY_A_TIME,aTime);
        editor.putString(KEY_AREA,aName);
        editor.putString(KEY_DES,aDes);
        editor.putString(KEY_COORD,aCoord);

        editor.commit();
    }

    public HashMap<String,String> getAllotmentDetails(){

        HashMap<String, String> details = new HashMap<String, String>();

        details.put(KEY_ALLOT_ID, pref.getString(KEY_ALLOT_ID, ""));
        details.put(KEY_A_ID, pref.getString(KEY_A_ID, ""));
        details.put(KEY_A_TIME, pref.getString(KEY_A_TIME, ""));
        details.put(KEY_AREA, pref.getString(KEY_AREA, ""));
        details.put(KEY_DES, pref.getString(KEY_DES, ""));
        details.put(KEY_COORD, pref.getString(KEY_COORD, ""));

        details.put(KEY_AllOT_HIST_ID, pref.getString(KEY_AllOT_HIST_ID, ""));

        Log.e("check", pref.getString(KEY_ALLOT_ID, "")+" "+pref.getString(KEY_AREA, ""));
        return  details;
    }


    public void setTodoUpdatedate(String date){
        editor.putString(TODO_UPDATED_ON,date);
        editor.commit();
    }

    public String getTodoUpdateDate(){
        return  pref.getString(TODO_UPDATED_ON,"");
    }

    public void saveAllotmentHistoryId(String id){
        editor.putString(KEY_AllOT_HIST_ID,id);
        editor.commit();
    }

}
