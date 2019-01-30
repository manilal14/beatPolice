package com.example.mani.beatpolice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;

import java.util.HashMap;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_ALLOT_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_TIME;

public class MyService extends Service {

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 30*1000;
    private static final float LOCATION_DISTANCE = 0;

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            String lat = String.valueOf(location.getLatitude());
            String lon = String.valueOf(location.getLongitude());

            boolean isAlloted = new LoginSessionManager(getApplicationContext()).isAlloted();

            if(isAlloted){
                if(checkAllotmentTime()){
                    if((!lat.equals("") || lon.equals(""))) {
                        Log.e(TAG, "sending");
                        Toast.makeText(getApplicationContext(), location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        sendToDatabase(lat, lon);
                    }
                    else{
                        Log.e(TAG,"lat is null");
                    }


                }
                else {
                    Log.e(TAG,"time is not in range");
                }


            }
            else {
                Log.e(TAG,"not alloted");
            }


        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void sendToDatabase(String latitude, String longitude) {

        Log.e(TAG,"called : sendToDatabase");

        final String allotId = new LoginSessionManager(getApplicationContext()).getAllotmentDetails().get(KEY_ALLOT_ID);

        final String pos = "[" +latitude+","+longitude+",";

        String SEND_URL = BASE_URL + "sent_periodic_location.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SEND_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
                Toast.makeText(getApplicationContext(),"Problem sending location",Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("allot_id",allotId);
                params.put("pos",pos);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private boolean checkAllotmentTime() {

        String aTime = new LoginSessionManager(getApplicationContext()).getAllotmentDetails().get(KEY_A_TIME);

        Log.e(TAG,aTime);

        String[] s2;
        long sTime = 0;
        long eTime = 0;

        try {
            s2 = aTime.split(",");

            sTime = Long.valueOf(s2[0]);
            eTime = Long.valueOf(s2[1]);

        }catch (Exception e){
            Log.e(TAG,"Exception cought 2");
        }

        long currUnixTime = System.currentTimeMillis()/1000L;


        if( ! (currUnixTime >= sTime && currUnixTime <= eTime ))
            return false;


        return true;

    }
}


























