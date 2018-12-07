package com.example.mani.beatpolice.TagsRelated;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.TAG_PIC_URL;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_ALLOT_ID;

public class NormalTagInfo extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private final String ISSUE_URL = BASE_URL + "fetch_issues_type.php";

    private Tag mTagDeails;
    private List<String> mIssueList;

    private  final int MY_CAMERA_PERMISSION_CODE = 100;
    private  final int CAMERA_REQUEST = 1888;

    private  View mDialogView;

    private String mImagePath;
    private Uri mFileUri;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LatLng mCurrentLatlng;

    private ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_tag_info);

        mTagDeails = (Tag) getIntent().getExtras().getSerializable("tagInfo");
        Log.e(TAG,mTagDeails.getId() +"");

        mIssueList = new ArrayList<>();
        mProgressDialog = new ProgressDialog(NormalTagInfo.this);
        mProgressDialog.setMessage("Uploading....");

        fetchIssueTypes();

        showDetails();
        clickListener();

        if(!mLocationPermissionsGranted)
            getLocationPermission();
        else
            getDeviceLocation();
    }

    private void clickListener() {

        TextView btnVerified = findViewById(R.id.verified);
        TextView btnReport = findViewById(R.id.report_issue);

        btnVerified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"Verified button is clicked");
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"Report button is clicked");

                dialogIssueReport();
            }
        });

    }

    private void dialogIssueReport() {

        final AlertDialog alertDialog;

        mDialogView = LayoutInflater.from(NormalTagInfo.this).inflate(R.layout.dialog_report_issue,null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog = new AlertDialog.Builder(NormalTagInfo.this,android.
                    R.style.Theme_DeviceDefault_Light_Dialog_MinWidth).create();
        } else {
            alertDialog = new AlertDialog.Builder(NormalTagInfo.this).create();
        }

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        final Spinner spinnerIssue  = mDialogView.findViewById(R.id.issue_title);
        LinearLayout llTakePhoto    = mDialogView.findViewById(R.id.take_photo);
        final EditText et_des       = mDialogView.findViewById(R.id.des);
        TextView btnCancel          = mDialogView.findViewById(R.id.cancel);
        TextView btnReport          = mDialogView.findViewById(R.id.report);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(NormalTagInfo.this,R.layout.spinner_layout_custom,mIssueList);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinnerIssue.setAdapter(adapter);

        alertDialog.setView(mDialogView);

        llTakePhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                Log.e(TAG,"onCameraClick");

                if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            MY_CAMERA_PERMISSION_CODE);
                } else {

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {

                        File file = new File(NormalTagInfo.this.getExternalCacheDir(),
                                String.valueOf(System.currentTimeMillis()) + ".jpg");
                        mFileUri = Uri.fromFile(file);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }


            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    int issueType = spinnerIssue.getSelectedItemPosition() + 1;
                    String des    = et_des.getText().toString();

                    sendIssue(issueType,des);

            }
        });

        alertDialog.show();


    }



    //On camera result
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.e(TAG,"onActivityResult : called");

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            Uri selectedImage = mFileUri;
            String path       = mFileUri.getPath();
            getContentResolver().notifyChange(selectedImage, null);
            ContentResolver cr = getContentResolver();

            Bitmap bitmap;
            File actualPath;

            try {
                bitmap = MediaStore.Images.Media
                        .getBitmap(cr, selectedImage);

                actualPath = new File(path);
                mImagePath = actualPath.getAbsolutePath();

                ImageView imageView = mDialogView.findViewById(R.id.image);
                imageView.setImageBitmap(bitmap);

                Log.e(TAG, "Actual path : " + actualPath.toString());

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

        }
    }

    private void showDetails() {

        TextView tv_title             = findViewById(R.id.title);
        TextView tv_des               = findViewById(R.id.des);
        final ImageView imageView     = findViewById(R.id.image);
        final ProgressBar progressBar = findViewById(R.id.image_progress_bar);

        tv_title.setText(mTagDeails.getName());
        tv_des.setText(mTagDeails.getDes());


        String imageName = mTagDeails.getImageName();

        if(!imageName.equals("")) {

            imageName = TAG_PIC_URL + imageName;
            Log.e(TAG,"image url : "+imageName);

            Glide.with(NormalTagInfo.this)
                    .load(imageName)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                            imageView.setBackgroundResource(R.mipmap.image_not_available);
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(NormalTagInfo.this,getString(R.string.image_not_available),Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
        }
        else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void fetchIssueTypes(){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ISSUE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e(TAG,"Issues : "+ response);


                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject res = jsonArray.getJSONObject(0);

                    int resCode = res.getInt("response_code");
                    if(resCode<=0){
                        String message = res.getString("message");
                        Log.e(TAG,"Error :"+message);
                        return;
                    }

                    mIssueList.clear();

                    for(int i=1;i<jsonArray.length();i++){
                        String issueTitle = jsonArray.getJSONObject(i).getString("title");
                        mIssueList.add(issueTitle);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"onErrorResponse "+error);

            }
        }){

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(NormalTagInfo.this).addToRequestQueue(stringRequest);
    }

    private void getLocationPermission() {

        Log.e(TAG, "getLocationPermission");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mLocationPermissionsGranted = true;
                getDeviceLocation();

            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void getDeviceLocation()  {

        Log.e(TAG, "getDeviceLocation");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Location location = (Location) task.getResult();
                            mCurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());

                            Log.e(TAG, "Current Location " + mCurrentLatlng);

                        } else {
                            Log.e(TAG, "Current Location can't be found");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());

        }


    }

    private void sendIssue(int issueType, String des) {

        Log.e(TAG,"called : sendIssue");

        //String policeId = mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID);

        String allotId = new LoginSessionManager(NormalTagInfo.this).getAllotmentDetails().get(KEY_ALLOT_ID);
        String tagId = "14524";
        String checkValue = "0";
        long currUnixTime = System.currentTimeMillis()/1000L;
        String time = String.valueOf(currUnixTime);
        String myLocation = String.valueOf(mCurrentLatlng.latitude) + "," + String.valueOf(mCurrentLatlng.longitude);

        String UPLOAD_URL = BASE_URL + "report_issue.php";

        try {

            new MultipartUploadRequest(NormalTagInfo.this,UPLOAD_URL)

                    .addFileToUpload(mImagePath, "image")

                    .addParameter("allot_id",allotId)
                    .addParameter("tag_id",tagId)
                    .addParameter("check",checkValue)
                    .addParameter("issue_id",String.valueOf(issueType))
                    .addParameter("des",des)
                    .addParameter("time",time)
                    .addParameter("my_pos",myLocation)

                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(10)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            mProgressDialog.show();
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            mProgressDialog.dismiss();
                            Log.e("TAG1",serverResponse.toString());
                            Log.e("TAG2",uploadInfo.toString());
                            if(exception!=null)
                                Log.e("TAG3",exception.toString());

                            Toast.makeText(NormalTagInfo.this,"Error uploading",Toast.LENGTH_SHORT).show();


                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            mProgressDialog.dismiss();
                            Toast.makeText(NormalTagInfo.this,"Issue Reported",Toast.LENGTH_SHORT).show();
                            finish();

                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {

                            mProgressDialog.dismiss();
                            Toast.makeText(NormalTagInfo.this,"Upload cancle",Toast.LENGTH_SHORT).show();

                        }
                    })
                    .startUpload();

        } catch (Exception e) {
            mProgressDialog.dismiss();
            Log.e(TAG, e.toString());
            Toast.makeText(NormalTagInfo.this,"Error uploadinglknl",Toast.LENGTH_SHORT).show();
            finish();


        }

    }
}
























