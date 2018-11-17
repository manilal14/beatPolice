package com.example.mani.beatpolice;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import static com.example.mani.beatpolice.CommanVariablesAndFunctuions.KEY_LATLNG;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMap extends Fragment implements OnMapReadyCallback {

    private String TAG = "FragmentMap";
    private GoogleMap mMap;
    private HomePage mActivity;


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM =15f;
    private static final float GPS_ZOOM =18f;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LatLng mMyLocation;

    LatLng p1 = new LatLng(10.05669880894801,76.32916152477264);
    LatLng p2 = new LatLng(10.054208338205562,76.33883692324162);
    LatLng p3 = new LatLng(10.046204963027062,76.33811607956886);
    LatLng p4 = new LatLng(10.04813096763208,76.32612626999617);
    LatLng p5 = new LatLng(10.0518568329032,76.32516670972109);


    public FragmentMap() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "Called : onCreate");

        mActivity = (HomePage) getActivity();



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
        else
            mapFragment.getMapAsync(this);

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

//        final Circle circle = mMap.addCircle(new CircleOptions()
//                .center(new LatLng(10.0511734,76.3327078))
//                .radius(1000)
//                .strokeColor(Color.RED)
//                .fillColor(getResources().getColor(R.color.map_alloted_color)));


        final PolygonOptions polygonOption = new PolygonOptions()
                .add(p1,p2,p3,p4,p5)
                .strokeColor(Color.RED)
                .fillColor(getResources().getColor(R.color.map_alloted_color))
                .zIndex(5.0f);

        final Polygon polygon = mMap.addPolygon(polygonOption);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                Log.e(TAG,"Map is clicked");
                boolean isInside = PolyUtil.containsLocation(latLng,polygonOption.getPoints(),false);
                if(!isInside)
                    return;

                Log.e(TAG,"inside");

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
}
