package com.example.mani.beatpolice.CommonPackage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.mani.beatpolice.GPSTracker;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_TIME;

public class CommanVariablesAndFunctuions {

    //values
    public static String KEY_LATLNG = "latlng";

    public static final int RETRY_SECONDS = 5;
    public static final int NO_OF_RETRY = 0;

    public static final String BASE_URL = "http://beatpolice.esy.es/beatu5081/";

    public static final String PROFILE_PIC_URL = BASE_URL + "profilePic/";
    public static final String TAG_PIC_URL = BASE_URL + "photos/tags/";

    public static final String DATE_FORMAT = "dd/MM/yyyy,hh:mma";
    public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";

    public static final String TIME_FORMAT  = "hh:mm a";
    public static final String TODAY        = "Today";
    public static  final String LOCATION_NOT_FOUND      = "can't find the location";

    public static LatLng getDeviceLocation(Context context) throws Exception {

            LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)
                    .setFastestInterval(1 * 1000);

            GPSTracker tracker = new GPSTracker(context);

            if (!tracker.canGetLocation()) {
                tracker.showSettingsAlert();
            }

            return new LatLng(tracker.getLatitude(), tracker.getLongitude());
    }

    // 06:45 PM
    public static String getFormattedTime(String TAG, String s) {

        String time = "NA";
        Date date;
        SimpleDateFormat sdf = new java.text.SimpleDateFormat(TIME_FORMAT);

        try {
            Long unix   = Long.valueOf(s);
            date        = new java.util.Date(unix*1000L);
            time        = sdf.format(date);

        }catch (Exception e){
            Log.e(TAG,"Exception cought while converting time : "+e.toString());

        }
        return time;

    }

    //24th Feb
    public static String getFormattedDate(String TAG, String unix) {

        String formatedstring = "NA";

        SimpleDateFormat sdf = new SimpleDateFormat("d");
        Long longUnix = null;

        try {

            longUnix = Long.valueOf(unix);
            String date = sdf.format(new Date(longUnix * 1000L));

            if (date.endsWith("1") && !date.endsWith("11"))
                sdf = new SimpleDateFormat("d'st' MMM");
            else if (date.endsWith("2") && !date.endsWith("12"))
                sdf = new SimpleDateFormat("d'nd' MMM");
            else if (date.endsWith("3") && !date.endsWith("13"))
                sdf = new SimpleDateFormat("d'rd' MMM");
            else
                sdf = new SimpleDateFormat("d'th' MMM");

        }catch (Exception e){
            Log.e(TAG,"Exception cought while converting time : "+e.toString());
        }


        formatedstring = sdf.format(new Date(longUnix*1000L));


        return formatedstring;



    }

    public static boolean isCurrentTimeBetweenAllotedTime(Context context) {

        LoginSessionManager session = new LoginSessionManager(context);
        String aTime = session.getAllotmentDetails().get(KEY_A_TIME);

        String[] s2;
        long sTime = 0;
        long eTime = 0;

        try {
            s2 = aTime.split(",");

            sTime = Long.valueOf(s2[0]);
            eTime = Long.valueOf(s2[1]);

        } catch (Exception e) {
            Log.e("CommonVarAndFun", "Exception cought 2");
        }

        long currUnixTime = System.currentTimeMillis() / 1000L;


        if (!(currUnixTime >= sTime && currUnixTime <= eTime))
            return false;


        return true;

    }

    public static void sendNavigateIntent(Context context, double latitude, double longitude){

        String uri = "google.navigation:q="+latitude+","+longitude;

        Uri gmmIntentUri = Uri.parse(uri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }
}













