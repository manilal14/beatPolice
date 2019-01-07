package com.example.mani.beatpolice.CommonPackage;

import android.content.Context;

import com.example.mani.beatpolice.GPSTracker;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class CommanVariablesAndFunctuions {

    //values
    public static String KEY_LATLNG = "latlng";

    public static final int RETRY_SECONDS = 5;
    public static final int NO_OF_RETRY = 0;

    //public static final String BASE_URL    = "http://192.168.1.11/beatPolice/";    //trogen_wifi
    //public static final String BASE_URL  = "http://192.168.43.153/beatPolice/";  //trojen
    //public static final String BASE_URL  = "http://192.168.100.103/beatPolice/"; // codebucket

    public static final String BASE_URL = "http://beatpolice.esy.es/beatu5081/";

    public static final String PROFILE_PIC_URL = BASE_URL + "profilePic/";
    public static final String TAG_PIC_URL = BASE_URL + "photos/tags/";

    public static final String DATE_FORMAT = "dd/MM/yyyy,hh:mma";


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
}













