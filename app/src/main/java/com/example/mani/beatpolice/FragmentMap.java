package com.example.mani.beatpolice;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.RoomDatabase.AreaTagTable;
import com.example.mani.beatpolice.RoomDatabase.AreaTagTableDao;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;
import com.example.mani.beatpolice.TagsRelated.AddTag;
import com.example.mani.beatpolice.TagsRelated.NormalTagInfo;
import com.example.mani.beatpolice.TagsRelated.SSTagInfo;
import com.example.mani.beatpolice.TagsRelated.Tag;
import com.example.mani.beatpolice.TodoAndIssue.TodoRelated.SimpleTodoDao;
import com.example.mani.beatpolice.TodoAndIssue.TodoRelated.SimpleTodoTable;
import com.example.mani.beatpolice.TodoAndIssue.TodoRelated.TodoAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.KEY_LATLNG;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.SIMPLE_DATE_FORMAT;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_ALLOT_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_TIME;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_AllOT_HIST_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_COORD;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_POLICE_ID;


public class FragmentMap extends Fragment implements OnMapReadyCallback {

    private String TAG = "FragmentMap";
    private GoogleMap mMap;
    private HomePage mActivity;
    private View mRootView;

    private final String TAG_URL = BASE_URL + "fetch_tag.php";
    private final String ALLOTEMENT_URL = BASE_URL + "fetch_allotment_details.php";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private static final float GPS_ZOOM = 18f;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LatLng mMyLocation;
    private LoginSessionManager mSession;

    private List<Tag> mTagList;

    private boolean shouldExecuteOnResume;
    private PolygonOptions mPolygonOption;

    private List<AreaTagTable> mAreaTagList;

    //Todo
    private List<SimpleTodoTable> mSimpleTodoList;

    private ProgressBar mProgressBar;

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
        //mActivity.getSupportActionBar().hide();
        mSession = new LoginSessionManager(getActivity());

        mAreaTagList = new ArrayList<>();
        mTagList = new ArrayList<>();
        mSimpleTodoList = new ArrayList<>();

        if(mSession.isAlloted())
            isRelieved();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "Called : onCreateView");
        mRootView = inflater.inflate(R.layout.fragment_map, container, false);

        mProgressBar = mRootView.findViewById(R.id.progress_bar);

        getLocationPermission();
        return mRootView;
    }

    private void initMap() {

        Log.e(TAG, "Called : initMap");

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            return;
        } else {
            mapFragment.getMapAsync(this);
        }
        ImageView gps = mRootView.findViewById(R.id.gps);
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

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

        clickListener();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        fetchAllotmentDetails();

    }

    private void fetchAllotmentDetails()
    {

        //If already alloted, no need to fetch details again;
        if(mSession.isAlloted()) {
            Log.e("hello","alloted history id = "+mSession.getAllotmentDetails().get(KEY_AllOT_HIST_ID));
            mActivity.startService(new Intent(mActivity, MyService.class));
            fetchAllTagsFromDatabase();
            nextOnMapReady();
            return;
        }

        else {
            fetchAllTagsFromDatabase();
        }

        Log.e("hello", "called : fetchAllotmentDetails");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ALLOTEMENT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e("hello", "allotment details : " + response);


                try {

                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject res = jsonArray.getJSONObject(0);

                    int rc = res.getInt("response_code");
                    String mess = res.getString("message");

                    if (rc <= 0) {
                        Log.e(TAG, "fetchAllotmentDetails : " + mess);
                        //mSession.clearAllotedArea();
                        //mActivity.stopService(new Intent(mActivity, MyService.class));
                        //new ClearAreaTagTable(BeatPoliceDb.getInstance(mActivity)).execute();

                    } else {

                        //Pick the last alloted area (more than one alloted area may be there)
                        JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);

                        String id        = jsonObject.getString("id");
                        String a_id      = jsonObject.getString("a_id");

                        String a_time    = jsonObject.getString("a_time");

                        String a_name    = jsonObject.getString("a_name");
                        String a_des     = jsonObject.getString("des");
                        String a_coord   = jsonObject.getString("coord");

                        mSession.saveAllotmentDetails(id, a_id, a_time, a_name, a_des, a_coord);
                        fetchTodo();
                        createAllotmentHistoryRowInDb();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("ert", "fetchAllotement : exception cought " + e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ert", "onErrorResponse :" + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String p_id = mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID);
                params.put("p_id", p_id);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS * 1000, NO_OF_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void createAllotmentHistoryRowInDb()
    {
        Log.e(TAG,"called : createAllotmentHistoryRowinDb");

        final String MY_URL = BASE_URL +"createAllotHisRow.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, MY_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"createAllotmentHistoryRowinDb : response "+response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int rc = jsonArray.getJSONObject(0).getInt("rc");

                    if(rc<=0){
                        Log.e(TAG,"allotment history row not created");
                        return;
                    }

                    int allotHistId = jsonArray.getJSONObject(0).getInt("lastId");
                    mSession.saveAllotmentHistoryId(String.valueOf(allotHistId));

                    Log.e(TAG, "Location service started");
                    mActivity.startService(new Intent(mActivity, MyService.class));

                    nextOnMapReady();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("allot_id",mSession.getAllotmentDetails().get(KEY_ALLOT_ID));
                params.put("p_id",mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS * 1000, NO_OF_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void isRelieved() {

        Log.e(TAG,"called : isReleived");
        String MY_URL = BASE_URL + "isRelieved.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, MY_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"isReleived response, "+response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int rc = jsonArray.getJSONObject(0).getInt("rc");
                    if(rc<=0){
                        Log.e(TAG,"Error in isRelieved");
                        return;
                    }

                    int isRelieved = Integer.parseInt(jsonArray.getJSONObject(1).getString("isRelieved"));
                    if(isRelieved == 1){
                        relievedThePolice();
                    }


                } catch (JSONException e) {
                    Log.e(TAG,"Exception in isReleived, "+e.toString());
                    e.printStackTrace();
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
                params.put("allot_Id",mSession.getAllotmentDetails().get(KEY_ALLOT_ID));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(mActivity).addToRequestQueue(stringRequest);

    }

    private void nextOnMapReady() {

        //fetchAllTagsFromDatabase();

//        if (!mSession.isAlloted()) {
//            Log.e(TAG, "Session not alloted");
//            return;
//        }

        Log.e(TAG, "called : FetchTagsFromRoom1");
        new FetchTagsFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();

        Log.e(TAG, "Beat area is allocated");


        setPolygon();
        markerClickListener();

        if (!isCurrentTimeBetweenAllotedTime()) {

            Log.e(TAG, "time is out of range 1");

            if(isAllotedTimeOver()){
                // Clearing allotment details and setting flag in allotment to 1
                Log.e("a","allotement time is over");
                setAllotementFlag();
            }
            return;
        }

        mRootView.findViewById(R.id.marker_imageview).setVisibility(View.VISIBLE);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                LatLng latLng = mMap.getCameraPosition().target;

                boolean isInside = PolyUtil.containsLocation(latLng, mPolygonOption.getPoints(), false);
                if (!isInside) {
                    Log.e(TAG, "Marker is outside the area alloted");
                    return;
                }

                Log.e(TAG, "inside area alloted");

                showSnackBar(latLng);
            }
        });
    }

    private void setAllotementFlag() {

        Log.e(TAG,"setAllotementFlag");

        String MY_URL = BASE_URL + "setAllotementflag.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, MY_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"setAllotementFlag, "+response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int rc = jsonArray.getJSONObject(0).getInt("rc");

                    if(rc == 1) {
                        Log.e(TAG,"cleared allocation details");
                        mSession.clearAllotedArea();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
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
                params.put("allot_id",mSession.getAllotmentDetails().get(KEY_ALLOT_ID));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(mActivity).addToRequestQueue(stringRequest);
    }


    private void markerClickListener() {

        if (mMap == null) {
            Log.e("check", "nMap is null");
            return;
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                /*
                    Get Latlng of marker clicked and convert it to string.
                    Match the string with latlng fetched.
                    Get the index of List.
                 */

                Log.e(TAG, "marker clicked " + marker.getPosition());

                LatLng latLng = marker.getPosition();
                String clickedLoc = latLng.latitude + "," + latLng.longitude;

                int tagIndexClicked = -1;

                for (int i = 0; i < mAreaTagList.size(); i++) {

                    String coord = mAreaTagList.get(i).getCoord();
                    if (coord.equals(clickedLoc)) {
                        tagIndexClicked = i;
                        break;
                    }
                }
                if (tagIndexClicked == -1) {
                    Log.e(TAG, "Marker to be added is clicked");
                    showSnackBar(marker.getPosition());
                    return false;
                }
                Log.e(TAG, "" + mAreaTagList.get(tagIndexClicked).getDes());

                Intent i;

                if (mAreaTagList.get(tagIndexClicked).getTagType() == 1)
                    i = new Intent(getActivity(), SSTagInfo.class);
                else
                    i = new Intent(getActivity(), NormalTagInfo.class);

                i.putExtra("tagInfo", mAreaTagList.get(tagIndexClicked));
                startActivity(i);

                return true;
            }
        });

    }

    private void setPolygon() {
        Log.e(TAG, "called : setPolygon");
        mPolygonOption = new PolygonOptions();
        String coord = mSession.getAllotmentDetails().get(KEY_COORD);

        if (!coord.equals("")) {
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
    }

    private void saveTagsToRoom() {

        int allottedAreaId = Integer.parseInt(mSession.getAllotmentDetails().get(KEY_A_ID));

        List<AreaTagTable> areaTagTableList = new ArrayList<>();

        for (int i = 0; i < mTagList.size(); i++) {

            Tag tag = mTagList.get(i);

            if (tag.getAId() == allottedAreaId) {
                int id = tag.getId();
                int aId = tag.getAId();

                String coord = tag.getCoord();
                int tagType = tag.getTagType();

                String name = tag.getName();
                String des = tag.getDes();
                String phone = tag.getPhone();
                String gender = tag.getGender();
                String n_name = tag.getN_name();
                String n_phone = tag.getN_phone();
                String imageName = tag.getImageName();

                areaTagTableList.add(new AreaTagTable(id, aId, coord, tagType, name, des, phone, gender, n_name, n_phone, imageName));
            }
        }

        //To remove the tags inside the area from general list
        for (int i = mTagList.size() - 1; i >= 0; i--) {
            Tag tag = mTagList.get(i);
            if (tag.getAId() == allottedAreaId)
                mTagList.remove(i);
        }

        // Add tags of alloted area to room database
        Log.e(TAG, "called : SaveTagToRoom");
        new SaveTagToRoom(BeatPoliceDb.getInstance(mActivity)).execute(areaTagTableList);


    }


    private void fetchAllTagsFromDatabase() {

        Log.e(TAG, "called : fetchAllTagsVFromDatabase");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, TAG_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (mTagList.size() != 0)
                    mTagList.clear();

                Log.e(TAG, "fetchTags : on Response " + response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject dbResponse = jsonArray.getJSONObject(0);

                    int rc = dbResponse.getInt("response_code");
                    String mess = dbResponse.getString("message");

                    if (rc <= 0) {
                        Log.e(TAG, "Response Code : " + rc + " message :" + mess);
                        return;
                    }

                    for (int i = 1; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        int id = Integer.parseInt(jsonObject.getString("id"));
                        int aId = Integer.parseInt(jsonObject.getString("a_id"));

                        String coord = jsonObject.getString("t_coord");
                        int tagType = Integer.parseInt(jsonObject.getString("t_type"));

                        String name = jsonObject.getString("name");
                        String des = jsonObject.getString("des");
                        String phone = jsonObject.getString("phone");
                        String gender = jsonObject.getString("gender");
                        String n_name = jsonObject.getString("n_name");
                        String n_phone = jsonObject.getString("n_phone");

                        String imageName = jsonObject.getString("image_name");
                        mTagList.add(new Tag(id, aId, coord, tagType, name, des, phone, gender, n_name, n_phone, imageName));
                    }

                    if (mSession.isAlloted()) {
                        if (!mSession.isSavedToRoom()) {
                            saveTagsToRoom();
                            mSession.setSavedToRoom();
                        }
                    }

                    //All tags are treated outside if area is not alloted
                    addOutsideTagsToMap(mTagList);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Exception cought " + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS * 1000, NO_OF_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    /*
        All tags are treated outside if area is not alloted
     */
    private void addOutsideTagsToMap(List<Tag> tagList) {

        Log.e(TAG, "called : addOutsideTagsToMap");

        int areaId = -5484246; //A random number

        if (mSession.isAlloted()) {
            areaId = Integer.parseInt(mSession.getAllotmentDetails().get(KEY_A_ID));
        }

        for (int i = 0; i < tagList.size(); i++) {

            Tag tagDetails = tagList.get(i);

            //So that only outside marker is marked is area is alloted
            if (tagDetails.getAId() == areaId)
                continue;

            String coord = tagDetails.getCoord();
            String[] s;
            try {
                s = coord.split(",");
                LatLng latLng = new LatLng(Double.valueOf(s[0]), Double.valueOf(s[1]));

                Drawable drawable = getResources().getDrawable(R.drawable.marker_outside);
                if (tagDetails.getTagType() == 1)
                    drawable = getResources().getDrawable(R.drawable.mss_outside);

                Bitmap bitmap = drawableToBitmap(drawable);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(tagDetails.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                mMap.addMarker(markerOptions);


            } catch (Exception e) {
                Log.e(TAG, "Exception cought 3");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG,"called onResume");


        if (shouldExecuteOnResume) {
            Log.e(TAG, "called : onResume, FetchTagsFromRoom2");
            new FetchTagsFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();
        }
        shouldExecuteOnResume = true;


        if(!mSession.isAlloted()){
            Log.e("zxc", "session is not alloted hence cant fetch todo");
            return;
        }

        fetchTodo();
    }

    private void fetchTodo() {

        //Fetch only one time in single day
        Date date = new Date();
        SimpleDateFormat sdf = new java.text.SimpleDateFormat(SIMPLE_DATE_FORMAT);
        String today = sdf.format(date);

        if(today.equals(mSession.getTodoUpdateDate())){
            Log.e("zxc","fetching directly from room and updating the adapter");
            new FetchSimpleTodoFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();
        }
        else{
            //Simpletodo fetching from database and saving to room then again fetching from room and updating the adapter
            fetchTodoListAndShow();
        }
    }


    private void relievedThePolice() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(mActivity);

        builder1.setMessage("You are being relieved throgh the Admin Dashboard.");
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        callphpToRelivedThePolice();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();



    }

    private void callphpToRelivedThePolice() {

        mProgressBar.setVisibility(View.VISIBLE)
        ;
        String MY_URL = BASE_URL + "relievedThePolice.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, MY_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"callphpToRelivedThePolice response, "+response);

                mProgressBar.setVisibility(View.GONE);

                try {

                    JSONArray jsonArray = new JSONArray(response);
                    int rc = jsonArray.getJSONObject(0).getInt("rc");

                    if(rc == 1){

                        mSession.logout();
                        mActivity.stopService(new Intent(mActivity, MyService.class));
                        mActivity.finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressBar.setVisibility(View.GONE);
                Log.e(TAG,error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("allot_id",mSession.getAllotmentDetails().get(KEY_ALLOT_ID));
                params.put("allot_hist_id",mSession.getAllotmentDetails().get(KEY_AllOT_HIST_ID));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(mActivity).addToRequestQueue(stringRequest);

    }

    private void showTagInsideArea() {

        Log.e(TAG, "called : showTagInsideArea");

        for (int i = 0; i < mAreaTagList.size(); i++) {

            AreaTagTable tagDetails = mAreaTagList.get(i);
            String coord = tagDetails.getCoord();
            String[] s;
            try {
                s = coord.split(",");

                LatLng latLng = new LatLng(Double.valueOf(s[0]), Double.valueOf(s[1]));

                Drawable drawable = null;

                if (tagDetails.getTagType() == 1) {

                    int tagStatus = tagDetails.getStatus();

                    switch (tagStatus) {
                        case 0:
                            drawable = getResources().getDrawable(R.drawable.mss_inside_unvisited);
                            break;
                        case 1:
                            drawable = getResources().getDrawable(R.drawable.mss_ok);
                            break;
                        case 2:
                            drawable = getResources().getDrawable(R.drawable.mss_issue);
                            break;
                    }
                } else {
                    int tagStatus = tagDetails.getStatus();
                    switch (tagStatus) {
                        case 0:
                            drawable = getResources().getDrawable(R.drawable.marker_inside_unvisited);
                            break;
                        case 1:
                            drawable = getResources().getDrawable(R.drawable.marker_ok);
                            break;
                        case 2:
                            drawable = getResources().getDrawable(R.drawable.marker_issue);
                            break;
                    }
                }

                Bitmap bitmap = drawableToBitmap(drawable);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(tagDetails.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                mMap.addMarker(markerOptions);


            } catch (Exception e) {
                Log.e(TAG, "Exception cought 3= "+e.toString());
            }
        }

    }


    /*-------------------------------------Supporting Functions-----------------------------------------------*/

    private void getLocationPermission() {

        Log.e(TAG, "getLocationPermission");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(mActivity, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

        LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);

        GPSTracker tracker = new GPSTracker(mActivity);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {

            mMyLocation = new LatLng(tracker.getLatitude(), tracker.getLongitude());
            Log.e(TAG, "myLocation found : " + mMyLocation);

            if (mMyLocation == null) {
                Toast.makeText(mActivity, "Please turn on gps first", Toast.LENGTH_SHORT).show();
                return;
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMyLocation, GPS_ZOOM));


        }

    }

    private boolean isCurrentTimeBetweenAllotedTime() {

        String aTime = mSession.getAllotmentDetails().get(KEY_A_TIME);

        String[] s2;
        long sTime = 0;
        long eTime = 0;

        try {
            s2 = aTime.split(",");

            sTime = Long.valueOf(s2[0]);
            eTime = Long.valueOf(s2[1]);

        } catch (Exception e) {
            Log.e(TAG, "Exception cought 2");
        }

        long currUnixTime = System.currentTimeMillis() / 1000L;


        if (!(currUnixTime >= sTime && currUnixTime <= eTime))
            return false;


        return true;

    }

    private boolean isAllotedTimeOver() {

        String aTime = mSession.getAllotmentDetails().get(KEY_A_TIME);

        String[] s2;
        long sTime = 0;
        long eTime = 0;

        try {
            s2 = aTime.split(",");

            sTime = Long.valueOf(s2[0]);
            eTime = Long.valueOf(s2[1]);

        } catch (Exception e) {
            Log.e(TAG, "Exception cought 2");
        }

        long currUnixTime = System.currentTimeMillis() / 1000L;


        if (currUnixTime>eTime)
            return true;

        return false;
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
                            Log.e(TAG, "onRequestPermissionsResult: permission denied");
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


    private void showSnackBar(final LatLng latLng) {

        if (!isCurrentTimeBetweenAllotedTime()) {
            Log.e(TAG, "You don't have permisssion to tag now2");
            return;
        }

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


                        Intent i = new Intent(getActivity(), AddTag.class);
                        i.putExtra(KEY_LATLNG, latLng);
                        i.putExtra("date", date);
                        i.putExtra("time", time);
                        startActivity(i);
                    }
                });
        snackbar.show();

    }

    class SaveTagToRoom extends AsyncTask<List<AreaTagTable>, Void, Void> {

        private final AreaTagTableDao areaTagTableDao;

        public SaveTagToRoom(BeatPoliceDb instance) {
            areaTagTableDao = instance.getAreaTagTableDao();
        }

        @Override
        protected Void doInBackground(List<AreaTagTable>... lists) {

            areaTagTableDao.deleteAll();
            areaTagTableDao.insert(lists[0]);

            //mAreaTagList = areaTagTableDao.getAllAreaTags();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //showTagInsideArea();
            Log.e(TAG, "called : FetchTagsFromRoom3");
            new FetchTagsFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();
        }
    }


    class FetchTagsFromRoom extends AsyncTask<Void, Void, Void> {

        private final AreaTagTableDao areaTagTableDao;

        public FetchTagsFromRoom(BeatPoliceDb instance) {
            areaTagTableDao = instance.getAreaTagTableDao();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mAreaTagList != null)
                mAreaTagList.clear();
            mAreaTagList = areaTagTableDao.getAllAreaTags();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showTagInsideArea();
        }
    }

    class ClearAreaTagTable extends AsyncTask<Void, Void, Void> {

        private final AreaTagTableDao areaTagTableDao;

        public ClearAreaTagTable(BeatPoliceDb instance) {
            areaTagTableDao = instance.getAreaTagTableDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            areaTagTableDao.deleteAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public void clickListener() {

        mRootView.findViewById(R.id.open_todo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);

                LinearLayout layout = mRootView.findViewById(R.id.todo_layout);

                Animation enterRight = AnimationUtils.loadAnimation(mActivity, R.anim.enter_from_right);

                layout.setVisibility(View.VISIBLE);
                layout.setAnimation(enterRight);

            }
        });

        mRootView.findViewById(R.id.close_todo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootView.findViewById(R.id.open_todo).setVisibility(View.VISIBLE);

                LinearLayout layout = mRootView.findViewById(R.id.todo_layout);

                Animation exitRight = AnimationUtils.loadAnimation(mActivity, R.anim.exit_to_right);
                layout.setAnimation(exitRight);
                layout.setVisibility(View.GONE);
            }
        });


    }


    public void fetchTodoListAndShow()
    {
        Log.e(TAG,"fetchTodoListAndShow");
        String FETCH_URL = BASE_URL + "fetchTodoList.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, FETCH_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("zxc","response for fetchTodoListAndShow "+response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int rc = Integer.parseInt(jsonArray.getJSONObject(0).getString("response_code"));

                    if(rc<=0){
                        String mess = jsonArray.getJSONObject(0).getString("message");
                        Log.e(TAG,mess);
                        return;
                    }

                    for(int i =1;i<jsonArray.length();i++){

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        int id = Integer.parseInt(jsonObject.getString("id"));
                        String title = jsonObject.getString("todo");
                        String des = jsonObject.getString("des");
                        String lat =  jsonObject.getString("lat");
                        String lon = jsonObject.getString("lon");

                        mSimpleTodoList.add(new SimpleTodoTable(id,title,des,lat,lon));

                    }

                    Log.e("zxc","savingSimpleTodoToRoom");
                    new SaveSimpleTodoToRoom(BeatPoliceDb.getInstance(mActivity)).execute(mSimpleTodoList);



                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("zxc",e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("zxc",error.toString());
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params =  new HashMap<>();
                String aId = mSession.getAllotmentDetails().get(KEY_A_ID);
                if(aId.equals("")){
                    Log.e("zxc", "area is not alloted yet");
                    aId = "1";
                }
                params.put("a_id",aId);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS * 1000, NO_OF_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(mActivity).addToRequestQueue(stringRequest);
    }


    class SaveSimpleTodoToRoom extends AsyncTask<List<SimpleTodoTable> ,Void,Void> {

        private final SimpleTodoDao simpleTodoDao;

        SaveSimpleTodoToRoom(BeatPoliceDb instance) {
            simpleTodoDao = instance.getSimpleTodoTableDao();
        }
        @Override
        protected Void doInBackground(List<SimpleTodoTable>... lists) {
            simpleTodoDao.insertList(lists[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Date date = new Date();
            SimpleDateFormat sdf = new java.text.SimpleDateFormat(SIMPLE_DATE_FORMAT);
            String today = sdf.format(date);
            mSession.setTodoUpdatedate(today);

            Log.e("zxc","fetchSimpleTodoFromRoom");
            new FetchSimpleTodoFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();
        }
    }

    class FetchSimpleTodoFromRoom extends AsyncTask<Void, Void, Void> {

        private final SimpleTodoDao todoTableDao;

        public FetchSimpleTodoFromRoom(BeatPoliceDb instance) {
            todoTableDao = instance.getSimpleTodoTableDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mSimpleTodoList != null)
                mSimpleTodoList.clear();
            mSimpleTodoList = todoTableDao.getAllSimpleTodo();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            RecyclerView recyclerViewTodo = mRootView.findViewById(R.id.recycler_view_to_do);

            TodoAdapter adapter = new TodoAdapter(mActivity, mSimpleTodoList);
            recyclerViewTodo.setLayoutManager(new LinearLayoutManager(mActivity));
            recyclerViewTodo.setHasFixedSize(true);
            recyclerViewTodo.setAdapter(adapter);
        }
    }


    private Date getDateFromString(String time){

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        try {
            return sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean compareDate(Date s, Date e, Date today){
        return s.compareTo(today) * today.compareTo(e) >= 0;
    }


}
