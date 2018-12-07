package com.example.mani.beatpolice;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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
import com.example.mani.beatpolice.TagsRelated.SSTagInfo;
import com.example.mani.beatpolice.TagsRelated.Tag;
import com.example.mani.beatpolice.TagsRelated.NormalTagInfo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_TIME;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_COORD;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_POLICE_ID;


public class FragmentMap extends Fragment implements OnMapReadyCallback {

    private String TAG = "FragmentMap";
    private GoogleMap mMap;
    private HomePage mActivity;
    private View mRootView;

    private final String TAG_URL = BASE_URL +"fetch_tag.php";
    private final String ALLOTEMENT_URL = BASE_URL + "fetch_allotment_details.php";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private static final float GPS_ZOOM =18f;
    private static final float DEFAULT_ZOOM = 22f;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LatLng mMyLocation;
    private LoginSessionManager mSession;

    private List<Tag> mTagList;

    private boolean shouldExecuteOnResume;

    private PolygonOptions mPolygonOption;
    private  Marker mToAdd;




    public FragmentMap() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (HomePage) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shouldExecuteOnResume = false;

        Log.e(TAG, "Called : onCreate");
        mActivity.getSupportActionBar().hide();
        mSession = new LoginSessionManager(getActivity());

        mTagList = new ArrayList<>();

        fetchAllotmentDetails();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "Called : onCreateView");
        mRootView =  inflater.inflate(R.layout.fragment_map, container, false);

        getLocationPermission();

        return mRootView;
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
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if(!mSession.isAlloted()) {
            Log.e(TAG,"Beat area is not allocated");
            mRootView.findViewById(R.id.marker_imageview).setVisibility(View.GONE);
            return;
        }

        Log.e(TAG,"Beat area is allocated");
        mRootView.findViewById(R.id.marker_imageview).setVisibility(View.VISIBLE);

        fetchTagsFromDatabase();

        // Set up polygon
        mPolygonOption  = new PolygonOptions();
        String coord = mSession.getAllotmentDetails().get(KEY_COORD);

        if(!coord.equals("")) {

            String s[];
            List<LatLng> latlnglist = new ArrayList<>();

            try {

                s = coord.split(",");
                for (int i = 0; i < s.length; i = i + 2) {
                    LatLng latLng = new LatLng(Double.valueOf(s[i]), Double.valueOf(s[i + 1]));
                    latlnglist.add(latLng);
                }

                for (int i = 0; i < latlnglist.size(); i++)
                    mPolygonOption.add(latlnglist.get(i));

                mPolygonOption.strokeColor(Color.RED)
                        .fillColor(getResources().getColor(R.color.map_alloted_color))
                        .zIndex(5.0f);
                mMap.addPolygon(mPolygonOption);

            } catch (Exception e) {
                Log.e(TAG, "Exception cought 1 : " + e);
            }
        }



        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                /*
                    Get Latlng of marker clicked and convert it to string.
                    Match the string with latlng fetched.
                    Get the index of List.
                 */

                Log.e(TAG,"marker clicked "+marker.getPosition());

                LatLng latLng     = marker.getPosition();
                String clickedLoc = latLng.latitude+","+latLng.longitude;

                int tagIndexClicked = -1;

                for(int i=0;i<mTagList.size();i++){

                    String coord = mTagList.get(i).getCoord();
                    if(coord.equals(clickedLoc)){
                        tagIndexClicked = i;
                        break;
                    }
                }
                if(tagIndexClicked == -1){
                    Log.e(TAG,"Marker to be added is clicked");
                    showSnackBar(marker.getPosition());
                    return false;
                }
                Log.e(TAG,""+mTagList.get(tagIndexClicked).getDes());

                Intent i;

                if(mTagList.get(tagIndexClicked).getTagType() == 1)
                     i = new Intent(getActivity(),SSTagInfo.class);
                else
                    i  = new Intent(getActivity(),NormalTagInfo.class);

                i.putExtra("tagInfo",mTagList.get(tagIndexClicked));
                startActivity(i);

                return true;
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                LatLng latLng = mMap.getCameraPosition().target;
                boolean isInside = PolyUtil.containsLocation(latLng,mPolygonOption.getPoints(),false);
                if(!isInside){
                    Log.e(TAG,"Marker is outside the area alloted");
                    return;
                }

                Log.e(TAG,"inside area alloted");


                boolean isTimeAlloted = checkAllotmentTime();
                if(!isTimeAlloted) {
                    Log.e(TAG,"You don't have permisssion to tag now");
                    return;
                }

                // To ensure you are indside the alloted area

//                boolean isMeInside = PolyUtil.containsLocation(mMyLocation,mPolygonOption.getPoints(),false);
//                if(!isMeInside){
//                    Log.e(TAG,"Your Location is outside alloted area");
//                    return;
//                }
//                Log.e(TAG,"you are inside");

                showSnackBar(latLng);
            }
        });


    }




    private void fetchTagsFromDatabase(){

        Log.e(TAG,"called : fetchTags");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, TAG_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(mTagList.size() !=0)
                    mTagList.clear();

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

                    for(int i=1;i<jsonArray.length();i++){

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        int id = Integer.parseInt(jsonObject.getString("id"));
                        String coord = jsonObject.getString("t_coord");
                        int tagType = Integer.parseInt(jsonObject.getString("t_type"));

                        String name    = jsonObject.getString("name");
                        String des     = jsonObject.getString("des");
                        String phone   = jsonObject.getString("phone");
                        String gender  = jsonObject.getString("gender");
                        String n_name  = jsonObject.getString("n_name");
                        String n_phone = jsonObject.getString("n_phone");

                        String imageName = jsonObject.getString("image_name");

                        mTagList.add(new Tag(id,coord,tagType,name,des,phone,gender,n_name,n_phone,imageName));
                    }

                    addTagsToMap();

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
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void addTagsToMap(){

        Log.e(TAG,"called : addTagsToMap");

        for(int i=0;i<mTagList.size();i++){

            Tag tagDetails = mTagList.get(i);
            String coord   = tagDetails.getCoord();
            String[] s;
            try {
                s = coord.split(",");
                LatLng latLng = new LatLng(Double.valueOf(s[0]),Double.valueOf(s[1]) );
                Log.e(TAG,latLng.toString());

                Drawable drawable = getResources().getDrawable(R.drawable.marker_black);
                if(tagDetails.getTagType() == 1)
                    drawable = getResources().getDrawable(R.drawable.m_ss);

                Bitmap bitmap = drawableToBitmap(drawable);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(tagDetails.getName())
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

        if(shouldExecuteOnResume) {
            Log.e(TAG, "called : onResume");
            //fetchTagsFromDatabase();
        }

        shouldExecuteOnResume = true;
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
                        mSession.clearAllotedArea();
                        return;
                    }
                    //Pick the last alloted area (more than one alloted area may be there)
                    JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length()-1);

                    String id      = jsonObject.getString("id");
                    String a_id    = jsonObject.getString("a_id");
                    String a_time  = jsonObject.getString("a_time");
                    String a_name  = jsonObject.getString("a_name");
                    String a_des   = jsonObject.getString("des");
                    String a_coord = jsonObject.getString("coord");

                    Log.e(TAG,"Allotment AREA ID is "+id);

                    mSession.saveAllotmentDetails(id,a_id,a_time,a_name,a_des,a_coord);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"fetchAllotement : exception cought "+e);
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




    /*-------------------------------------Supporting Functions-----------------------------------------------*/

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

    private boolean checkAllotmentTime() {

        String aTime = mSession.getAllotmentDetails().get(KEY_A_TIME);
        Log.e("asd1 ",aTime);

        String[] s2;
        long sTime = 0;
        long eTime = 0;

        try {
            s2 = aTime.split(",");

            Log.e(TAG,s2[0]+" "+s2[1]);

            sTime = Long.valueOf(s2[0]);
            eTime = Long.valueOf(s2[1]);

        }catch (Exception e){
            Log.e(TAG,"Exception cought 2");
        }

        Log.e(TAG," "+sTime +" ," +eTime);

        long currUnixTime = System.currentTimeMillis()/1000L;
        Log.e(TAG,currUnixTime+"");

        if( ! (currUnixTime >= sTime && currUnixTime <= eTime ))
            return false;


        return true;

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

    private void showSnackBar(final LatLng latLng){

        CoordinatorLayout coordinatorLayout = mRootView.findViewById(R.id.coordinate_layout);
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Tag this place ?", Snackbar.LENGTH_SHORT)

                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        getDeviceLocation();

                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy,hh:mma");
                        String fdate = df.format(Calendar.getInstance().getTime());

                        String[] s = fdate.split(",");
                        final String date = s[0];
                        final String time = s[1];

                        Log.e(TAG,date+" "+time);

                        Intent i = new Intent(getActivity(),AddTag.class);
                        i.putExtra(KEY_LATLNG,latLng);
                        i.putExtra("date",date);
                        i.putExtra("time",time);
                        startActivity(i);
                    }
                });
        snackbar.show();

    }

}
