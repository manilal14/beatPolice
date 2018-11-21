package com.example.mani.beatpolice;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.TagsRelated.AddTag;
import com.example.mani.beatpolice.TagsRelated.Tag;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.KEY_LATLNG;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_TIME;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_COORD;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_POLICE_ID;


public class FragmentMap extends Fragment implements OnMapReadyCallback {

    private String TAG = "FragmentMap";
    private GoogleMap mMap;
    private HomePage mActivity;

    private final String TAG_URL = BASE_URL +"fetch_tags.php";
    private final String ALLOTEMENT_URL = BASE_URL + "fetch_allotment_details.php";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private static final float GPS_ZOOM =18f;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LatLng mMyLocation;
    private LoginSessionManager mSession;



    public FragmentMap() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (HomePage) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "Called : onCreate");
        mActivity.getSupportActionBar().hide();
        mSession = new LoginSessionManager(getActivity());

        fetchAllotmentDetails();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "Called : onCreateView");
        View view =  inflater.inflate(R.layout.fragment_map, container, false);

        getLocationPermission();

        return view;
    }

    private void initMap(){

        Log.e(TAG, "Called : initMap");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(mapFragment == null){
            Log.e(TAG, "mapFragment is null");
            return;
        }
        else {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.e(TAG, "Called : onMapReady");
        mMap = googleMap;
        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        mMap.setMyLocationEnabled(true);

        // Set up polygon
        final PolygonOptions polygonOption  = new PolygonOptions();
        String coord = mSession.getAllotmentDetails().get(KEY_COORD);
        String s[];
        List<LatLng> latlnglist = new ArrayList<>();

        try {

            s = coord.split(",");
            for(int i=0;i<s.length;i=i+2){
                LatLng latLng = new LatLng(Double.valueOf(s[i]),Double.valueOf(s[i+1]));
                latlnglist.add(latLng);
            }

            for(int i=0;i<latlnglist.size();i++)
                polygonOption.add(latlnglist.get(i));

            polygonOption.strokeColor(Color.RED)
                    .fillColor(getResources().getColor(R.color.map_alloted_color))
                    .zIndex(5.0f);
            mMap.addPolygon(polygonOption);

        } catch (Exception e){
            Log.e(TAG,"Exception cought 1");
        }

        fetchTagsFromDatabase();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                Log.e(TAG,"Map is clicked");
                boolean isInside = PolyUtil.containsLocation(latLng,polygonOption.getPoints(),false);
                if(!isInside)
                    return;
                Log.e(TAG,"inside");

                String aTime = mSession.getAllotmentDetails().get(KEY_A_TIME);
                String[] s2;
                double sTime = 0;
                double eTime = 0;

                try {
                    s2 = aTime.split(",");
                    sTime = Double.valueOf(s2[0]);
                    eTime = Double.valueOf(s2[1]);

                }catch (Exception e){
                    Log.e(TAG,"Exception cought 2");
                }

                long currUnixTime = System.currentTimeMillis()/1000L;
                Log.e("asd ",currUnixTime+"");

                if( ! (currUnixTime >= sTime && currUnixTime <= eTime )){
                    Toast.makeText(getActivity(),"You don't have permisssion to tag now",Toast.LENGTH_SHORT).show();
                    return;
                }

                DateFormat df = new SimpleDateFormat("dd/MM/yyyy,hh:mma");
                String fdate = df.format(Calendar.getInstance().getTime());

                String[] s = fdate.split(",");
                final String date = s[0];
                final String time = s[1];

                Log.e(TAG,date+" "+time);

                AlertDialog.Builder builder;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getActivity());
                }

                builder.setTitle("Want to tag this place?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(getActivity(),AddTag.class);
                                i.putExtra(KEY_LATLNG,latLng);
                                i.putExtra("date",date);
                                i.putExtra("time",time);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

    }

    private void getLocationPermission() {

        Log.e(TAG, "getLocationPermission");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(mActivity, FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(mActivity,
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mLocationPermissionsGranted = true;
                initMap();

            } else {
                requestPermissions(permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
                requestPermissions(permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {

        Log.e(TAG, "getDeviceLocation");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Log.e(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            mMyLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            //mMap.addMarker(new MarkerOptions().position(mMyLocation));
                            Log.e("TAG","Current Location : "+mMyLocation.toString());

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMyLocation, GPS_ZOOM));

                        } else {
                            Log.e(TAG, "onComplete: current location is null");
                            Toast.makeText(mActivity, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.e(TAG, "onRequestPermissionsResult: called.");

        mLocationPermissionsGranted = false;
        switch (requestCode) {

            case LOCATION_PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.e(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.e(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    initMap();
                }

                break;

        }
    }

    private void fetchTagsFromDatabase(){

        Log.e(TAG,"called : fetchTags");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, TAG_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                List<Tag> tagList = new ArrayList<>();

                Log.e(TAG,"fetchTags : on Response "+response);

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    JSONObject dbResponse = jsonArray.getJSONObject(0);
                    int rc        = dbResponse.getInt("response_code");
                    String  mess  = dbResponse.getString("message");

                    if(rc<=0){
                        Log.e(TAG,"Response Code : "+rc +" message :" +mess);
                        return;
                    }

                    Log.e(TAG,"2");

                    for(int i=1;i<jsonArray.length();i++){

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        int id = Integer.parseInt(jsonObject.getString("id"));
                        String title = jsonObject.getString("t_title");
                        String coord = jsonObject.getString("t_coord");
                        String imageName = jsonObject.getString("image_name");
                        String des = jsonObject.getString("t_des");
                        int status = Integer.parseInt(jsonObject.getString("t_status"));

                        tagList.add(new Tag(id,title,des,coord,imageName,status));
                    }

                    addTagsToMap(tagList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"Exception cought "+e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                String aId = mSession.getAllotmentDetails().get(KEY_A_ID);
                params.put("a_id",aId);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void addTagsToMap(List<Tag> tagList){

        for(int i=0;i<tagList.size();i++){

            Tag tagDetails = tagList.get(i);

            String coord = tagDetails.getCoord();
            String[] s;
            try {
                s = coord.split(",");
                LatLng latLng = new LatLng(Double.valueOf(s[0]),Double.valueOf(s[1]) );
                Log.e(TAG,latLng.toString());

                Drawable drawable =  getResources().getDrawable(R.drawable.marker_red);
                if(tagDetails.getStatus() == 1 )
                    drawable = getResources().getDrawable(R.drawable.marker_ok);

                Bitmap bitmap = drawableToBitmap(drawable);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(tagDetails.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                mMap.addMarker(markerOptions);


            }catch (Exception e) {
                Log.e(TAG,"Exception cought 3");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTagsFromDatabase();
    }

    private void fetchAllotmentDetails(){

        Log.e(TAG,"called : fetchAllotmentDetails");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ALLOTEMENT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "allotment details : "+response);

                try {

                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject res = jsonArray.getJSONObject(0);

                    int     rc  = res.getInt("response_code");
                    String mess = res.getString("message");

                    if(rc <= 0){
                        Log.e(TAG,"fetchAllotmentDetails : "+mess);
                        return;
                    }
                    //Pick the last alloted area (more than one alloted area may be there)
                    JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length()-1);

                    String a_id    = jsonObject.getString("a_id");
                    String a_time  = jsonObject.getString("a_time");
                    String a_name  = jsonObject.getString("a_name");
                    String a_des   = jsonObject.getString("des");
                    String a_coord = jsonObject.getString("coord");

                    mSession.saveAllotmentDetails(a_id,a_time,a_name,a_des,a_coord);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"fetchAllotement : exception cought");
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG,"onErrorResponse :"+error);


            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<>();

                String p_id = mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID);
                params.put("p_id",p_id);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
