package com.example.mani.beatpolice.TagsRelated;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.example.mani.beatpolice.ReportHistory;
import com.example.mani.beatpolice.RoomDatabase.AreaTagTable;
import com.example.mani.beatpolice.RoomDatabase.AreaTagTableDao;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;
import com.google.android.gms.maps.model.LatLng;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.TAG_PIC_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.getDeviceLocation;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_ALLOT_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_TIME;

public class NormalTagInfo extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private final String ISSUE_URL = BASE_URL + "fetch_issues_type.php";
    private final String REPORT_ISSUE_URL = BASE_URL + "report_issue.php";

    private AreaTagTable mTagDeails;
    private List<String> mIssueList;

    private  final int MY_CAMERA_PERMISSION_CODE = 100;
    private  final int CAMERA_REQUEST = 1888;

    private  View mDialogView;

    private String mImagePath = null;
    private Uri mFileUri;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;

    //private FusedLocationProviderClient mFusedLocationProviderClient;

    private LatLng mCurrentLatlng;
    private ProgressDialog mProgressDialog;

    int flag = 0;

    private String mCurrentPhotoPath;
    private LoginSessionManager mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_tag_info);


        mTagDeails = (AreaTagTable) getIntent().getExtras().getSerializable("tagInfo");
        Log.e(TAG,mTagDeails.getId() +"");

        mIssueList = new ArrayList<>();
        mProgressDialog = new ProgressDialog(NormalTagInfo.this);
        mProgressDialog.setMessage("Please wait....");
        mProgressDialog.setCancelable(false);

        mSession = new LoginSessionManager(NormalTagInfo.this);

        fetchIssueTypes();

        showDetails();
        clickListener();

        if(!mLocationPermissionsGranted)
            getLocationPermission();
        else
            try {
                mCurrentLatlng = getDeviceLocation(NormalTagInfo.this);
            } catch (Exception e) {
                Log.e(TAG, "Exception cought " + e);
            }
    }



    private void clickListener() {

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        LinearLayout reportButtons = findViewById(R.id.report_layout);
        reportButtons.setVisibility(View.VISIBLE);

        TextView btnVerified = findViewById(R.id.verified);
        TextView btnReport = findViewById(R.id.report_issue);

        btnVerified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isTimeAlloted = checkAllotmentTime();
                if(!isTimeAlloted) {
                    Toast.makeText(NormalTagInfo.this,getString(R.string.permission_denied),Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mTagDeails.getStatus() != 0 || flag == 1){
                    Toast.makeText(NormalTagInfo.this,"Already reported",Toast.LENGTH_SHORT).show();
                    return;
                }
                sendReportWithoutImage("0","Ok",1,1);
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isTimeAlloted = checkAllotmentTime();
                if(!isTimeAlloted) {
                    Toast.makeText(NormalTagInfo.this,getString(R.string.permission_denied),Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mTagDeails.getStatus() != 0 || flag == 1){
                    Toast.makeText(NormalTagInfo.this,"Already reported",Toast.LENGTH_SHORT).show();
                    return;
                }
                mImagePath = null;
                dialogIssueReport();
            }
        });

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
                }

                else if (ContextCompat.checkSelfPermission(NormalTagInfo.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(NormalTagInfo.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }

                else {

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {

//                        String allotId = new LoginSessionManager(NormalTagInfo.this).getAllotmentDetails().get(KEY_ALLOT_ID);
//
//                        File file = new File(NormalTagInfo.this.getExternalCacheDir(),
//                                allotId+String.valueOf(System.currentTimeMillis()) + ".jpg");
//
//                        mFileUri = FileProvider.getUriForFile(NormalTagInfo.this,"com.example.mani.beatpolice.provider",file);
//                        Log.e(TAG,"mFileUri "+mFileUri.toString());
//
//                        //mFileUri = Uri.fromFile(file);
//                         cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
//                         startActivityForResult(cameraIntent, CAMERA_REQUEST);

                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG,"file for image is not created");
                        }

                        if(photoFile!=null){
                            Uri fileUri = FileProvider.getUriForFile(NormalTagInfo.this,"com.example.mani.beatpolice.provider",photoFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }
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

                //int issueType = spinnerIssue.getSelectedItemPosition() + 1;
                String des    = et_des.getText().toString().trim();

                if(des.equals("")){
                    Toast.makeText(NormalTagInfo.this,"Write description",Toast.LENGTH_SHORT).show();
                    return;
                }

                String type = (String) spinnerIssue.getSelectedItem();

                if(mImagePath != null)
                    sendIssueWithImage(type,des,0,mImagePath);
                else {
                    sendReportWithoutImage(type,des,0,2);
                }

                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }



    //On camera result
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        Log.e(TAG,"onActivityResult : called");

        if(requestCode != RESULT_CANCELED) {

            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

//                Uri selectedImage = mFileUri;
//                String path = mFileUri.getPath();
//
//                getContentResolver().notifyChange(selectedImage, null);
//                ContentResolver cr = getContentResolver();
//
//                Bitmap bitmap;
//                File actualPath;
//
//                try {
//                    bitmap = MediaStore.Images.Media
//                            .getBitmap(cr, selectedImage);
//
//                    actualPath = new File(path);
//                    mImagePath = actualPath.getAbsolutePath();
//
//                    ImageView imageView = mDialogView.findViewById(R.id.image);
//                    imageView.setImageBitmap(bitmap);
//
//                    Log.e(TAG, "Actual path : " + mImagePath);
//
//                } catch (Exception e) {
//                    Log.e(TAG,"Exception :"+ e.getMessage());
//                    e.printStackTrace();
//                }

                ImageView imageView = mDialogView.findViewById(R.id.image);
                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                compressImage(bitmap);
                Glide.with(NormalTagInfo.this)
                        .load(mCurrentPhotoPath)
                        .into(imageView);

            }
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

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                try {
                    mCurrentLatlng = getDeviceLocation(NormalTagInfo.this);
                } catch (Exception e) {
                    Log.e(TAG, "Exception cought " + e);
                }

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




    // When image need to be send
    private void sendIssueWithImage(String issueType, String des, final int checkValue, String imagePath) {

        Log.e(TAG,"called : sendIssueWithImage");

        //String policeId = mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID);

        String allotId = new LoginSessionManager(NormalTagInfo.this).getAllotmentDetails().get(KEY_ALLOT_ID);
        String tagId = String.valueOf(mTagDeails.getId());
        final String check = String.valueOf(checkValue);
        long currUnixTime = System.currentTimeMillis()/1000L;
        String time = String.valueOf(currUnixTime);

        String myLocation = "na";

        if(mCurrentLatlng !=null){
            myLocation = String.valueOf(mCurrentLatlng.latitude) + "," + String.valueOf(mCurrentLatlng.longitude);
        }

        try {
            mProgressDialog.show();
            new MultipartUploadRequest(NormalTagInfo.this,REPORT_ISSUE_URL)

                    .addFileToUpload(mCurrentPhotoPath, "image")

                    .addParameter("allot_id",allotId)
                    .addParameter("tag_id",tagId)
                    .addParameter("check",check)
                    .addParameter("issue_id",String.valueOf(issueType))
                    .addParameter("des",des)
                    .addParameter("time",time)
                    .addParameter("my_pos",myLocation)

                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) { }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            mProgressDialog.dismiss();
                            Log.e("asd1",uploadInfo.toString());
                            if(serverResponse!=null)
                                Log.e("asd",serverResponse.toString());
                            if(exception!=null)
                                Log.e("asd",exception.toString());
                            Toast.makeText(NormalTagInfo.this,"IssueTable is not reported",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            mProgressDialog.dismiss();
                            String message ="";
                            if(checkValue == 0){
                                message = "IssueTable Reported";
                            }
                            else {
                                message = "verified";
                            }

                            Toast.makeText(NormalTagInfo.this,message,Toast.LENGTH_SHORT).show();
                            new UpdateTagStatusinRoom(BeatPoliceDb.getInstance(NormalTagInfo.this)).execute(2);


                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            mProgressDialog.dismiss();
                            Toast.makeText(NormalTagInfo.this,"Upload cancle",Toast.LENGTH_SHORT).show();

                        }
                    })
                    .startUpload();
                    //To lock screen

        } catch (Exception e) {
            mProgressDialog.dismiss();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Log.e(TAG, e.toString());
            Toast.makeText(NormalTagInfo.this,"Error uploading",Toast.LENGTH_SHORT).show();
            finish();


        }

    }

    // Without image, ReportType = 1(for veified), 2(for issue report)
    private void sendReportWithoutImage(final String type, final String des, final int checkValue, final int reportType) {

        Log.e(TAG,"called : sendReportWithoutImage");
        mProgressDialog.show();

        long currUnixTime = System.currentTimeMillis()/1000L;
        final String time = String.valueOf(currUnixTime);

        String myLocation = "na";
        if(mCurrentLatlng !=null){
            myLocation = String.valueOf(mCurrentLatlng.latitude) + "," + String.valueOf(mCurrentLatlng.longitude);
        }
        final String finalMyLocation = myLocation;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REPORT_ISSUE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e(TAG,"------" + response);
                mProgressDialog.dismiss();
                Toast.makeText(NormalTagInfo.this,"Success",Toast.LENGTH_SHORT).show();

                //For verifiacation 1, issue reported = 2
                new UpdateTagStatusinRoom(BeatPoliceDb.getInstance(NormalTagInfo.this)).execute(reportType);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                Log.e(TAG,error.toString());
                Toast.makeText(NormalTagInfo.this,error.toString(),Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();

                LoginSessionManager mSeesion = new LoginSessionManager(NormalTagInfo.this);

                String allotId = mSeesion.getAllotmentDetails().get(KEY_ALLOT_ID);
                String tagId  = String.valueOf(mTagDeails.getId());

                params.put("allot_id",allotId);
                params.put("tag_id",tagId);
                params.put("check",String.valueOf(checkValue));
                params.put("issue_id",type);
                params.put("des",des);
                params.put("time",time);
                params.put("my_pos", finalMyLocation);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(NormalTagInfo.this).addToRequestQueue(stringRequest);

    }

    class UpdateTagStatusinRoom extends AsyncTask<Integer,Void,Void> {

        private final AreaTagTableDao areaTagTableDao;

        public UpdateTagStatusinRoom(BeatPoliceDb instance) {
            areaTagTableDao = instance.getAreaTagTableDao();
        }


        @Override
        protected Void doInBackground(Integer... integers) {
            areaTagTableDao.updateTagStatus(mTagDeails.getId(),integers[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //flag = 1;
            if(!NormalTagInfo.this.isDestroyed()){
                onBackPressed();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tag_hisory,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.history:
                Intent i = new Intent(NormalTagInfo.this,ReportHistory.class);
                i.putExtra("tagId",mTagDeails.getId());
                startActivity(i);
                return true;

        }
        return false;
    }

    private boolean checkAllotmentTime() {

        LoginSessionManager session = new LoginSessionManager(NormalTagInfo.this);
        String aTime = session.getAllotmentDetails().get(KEY_A_TIME);

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

    private File createImageFile() throws IOException {

        String allotId = mSession.getAllotmentDetails().get(KEY_ALLOT_ID);
        String mImageName     = allotId+String.valueOf(System.currentTimeMillis())+".jpg";
        File storageDir   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File imageFile    = new File(NormalTagInfo.this.getExternalCacheDir(), mImageName);

        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.e(TAG,"currentPhotoPath = "+mCurrentPhotoPath);
        Log.e(TAG,"currentPhotoPath = "+mImageName);
        return imageFile;
    }

    private void compressImage(Bitmap bitmap) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 18, bos);

        byte[] bitmapData = bos.toByteArray();

        try {
            //Compressed image is written in same previous image file
            FileOutputStream fos = new FileOutputStream(mCurrentPhotoPath);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Exception while converting bitmap to file, "+e.toString());
        }

    }

}
























