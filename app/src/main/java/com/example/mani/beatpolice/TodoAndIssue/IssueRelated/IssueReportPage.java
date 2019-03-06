package com.example.mani.beatpolice.TodoAndIssue.IssueRelated;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.mani.beatpolice.AddressDialog;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.R;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.LOCATION_NOT_FOUND;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.TIME_FORMAT;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.TODAY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.getDeviceLocation;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_POLICE_ID;

public class IssueReportPage extends AppCompatActivity implements AddressDialog.AddressDialogListener {

    @Override
    public void getAddressViaListener(LatLng selectedLatlng, String completeAddress) {
       mSelectedLatlng = selectedLatlng;

       EditText et_address = findViewById(R.id.address);
       et_address.setText(completeAddress);
       et_address.setSelection(et_address.length());

    }

    private final String TAG = this.getClass().getSimpleName();

    private List<String> mIssueList;
    private long mUnixFrom=0,mUnixTo=0;


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private  final int CAMERA_REQUEST = 1888;
    private  final int MY_CAMERA_PERMISSION_CODE = 100;

    private LoginSessionManager mSession;

    private ProgressBar mProgressBar;

    private LatLng mDeviceLocation;
    private LatLng mSelectedLatlng;

    private String mCurrentPhotoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_report_page);

        mIssueList = new ArrayList<>();
        mIssueList.add("Select Issue Type");
        mProgressBar = findViewById(R.id.progress_bar);
        mSession = new LoginSessionManager(IssueReportPage.this);
        getLocationPermission();
        fetchIssueType();

        clickListener();
    }



    private void clickListener() {

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView tv_from      = findViewById(R.id.from);
        TextView tv_to        = findViewById(R.id.to);
        TextView tv_takePhoto = findViewById(R.id.take_photo);
        final EditText et_address   = findViewById(R.id.address);
        final TextView tv_des = findViewById(R.id.des);
        TextView tv_submit    = findViewById(R.id.submit);



        tv_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateTimePicker((TextView) v);
            }
        });
        tv_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateTimePicker((TextView) v);
            }
        });
        tv_takePhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        et_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermissionAndOpenAddressDialog();
            }
        });


        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String des = tv_des.getText().toString().trim();
                String address = et_address.getText().toString().trim();

                final Spinner spinnerIssueType = findViewById(R.id.issue_type);
                int issuePos = spinnerIssueType.getSelectedItemPosition();

                if(issuePos == 0){
                    Toast.makeText(IssueReportPage.this,"Select issue type",Toast.LENGTH_SHORT).show();
                    return;
                }



                if(mUnixFrom==0 || mUnixTo==0){
                    Toast.makeText(IssueReportPage.this,"From and To are required",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(des.equals("")) {
                    Toast.makeText(IssueReportPage.this,"Description is required",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(address.equals("") || address.equals(LOCATION_NOT_FOUND)){
                    Toast.makeText(IssueReportPage.this,"Location required",Toast.LENGTH_SHORT).show();
                    return;

                }


                String issueType = (String) spinnerIssueType.getSelectedItem();

                String areaId    = mSession.getAllotmentDetails().get(KEY_A_ID);
                String policeId  = mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID);

                Long currentUnix = System.currentTimeMillis()/1000;

                String reportedAtLocation = String.valueOf(mDeviceLocation.latitude)+","+String.valueOf(mDeviceLocation.longitude);


                IssueTable issue = new IssueTable(policeId,areaId,issueType,String.valueOf(mUnixFrom), String.valueOf(mUnixTo),
                        String.valueOf(currentUnix),reportedAtLocation,mCurrentPhotoPath,des,
                        String.valueOf(mSelectedLatlng.latitude), String.valueOf(mSelectedLatlng.longitude),address);

                new SaveIssueToRoom(BeatPoliceDb.getInstance(IssueReportPage.this)).execute(issue);

            }
        });


    }


    private void fetchIssueType(){
        mProgressBar.setVisibility(View.VISIBLE);
        String ISSUE_URL = BASE_URL + "fetch_issues_type.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ISSUE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"Issues : "+ response);

                mProgressBar.setVisibility(View.GONE);

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
                    mIssueList.add("Select Issue Type");

                    for(int i=1;i<jsonArray.length();i++){
                        String issueTitle = jsonArray.getJSONObject(i).getString("title");
                        mIssueList.add(issueTitle);
                    }

                    final Spinner spinnerIssueType = findViewById(R.id.issue_type);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(IssueReportPage.this,R.layout.spinner_layout_custom2,mIssueList);

                    spinnerIssueType.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressBar.setVisibility(View.GONE);
                Log.e(TAG,"onErrorResponse "+error);

            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(IssueReportPage.this).addToRequestQueue(stringRequest);
    }

    public void setDateTimePicker(final TextView textView) {

        final Calendar currentDate = Calendar.getInstance();
        final Calendar calender = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(IssueReportPage.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calender.set(year, monthOfYear, dayOfMonth);

                TimePickerDialog timePickerDialog = new TimePickerDialog(IssueReportPage.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calender.set(Calendar.MINUTE, minute);

                        long unixInMilli = calender.getTimeInMillis();

                        SimpleDateFormat datePattern = new SimpleDateFormat("EEE, d MMM");
                        String formattedDate = datePattern.format(calender.getTime());

                        SimpleDateFormat timePattern = new SimpleDateFormat(TIME_FORMAT);
                        String formattedTime ;

                        if (DateUtils.isToday(unixInMilli))
                            formattedTime = TODAY + ", " +timePattern.format(calender.getTime());
                        else
                            formattedTime = timePattern.format(calender.getTime());

                        textView.setText(formattedDate+"    "+formattedTime);
                        textView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);

                        if(textView.getId() == R.id.from){
                            Log.e(TAG, "from is set");
                            mUnixFrom = unixInMilli/1000L;
                        }
                        else {
                            Log.e(TAG, "to is set");
                            mUnixTo = unixInMilli/1000L;
                        }


                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);

                timePickerDialog.show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }




    private void getLocationPermissionAndOpenAddressDialog() {

        Log.e(TAG, "getLocationPermission");

        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(IssueReportPage.this,
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(IssueReportPage.this,
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                new AddressDialog().show(getSupportFragmentManager(), null);

            } else {
                ActivityCompat.requestPermissions(IssueReportPage.this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(IssueReportPage.this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getLocationPermission() {

        Log.e(TAG, "getLocationPermission");

        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(IssueReportPage.this,
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(IssueReportPage.this,
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                try {
                    mDeviceLocation = getDeviceLocation(IssueReportPage.this);
                } catch (Exception e) {
                    Log.e(TAG,"error in finding location "+e.toString());
                    e.printStackTrace();
                }


            } else {
                ActivityCompat.requestPermissions(IssueReportPage.this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(IssueReportPage.this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    class SaveIssueToRoom extends AsyncTask<IssueTable,Void,Void> {

        private final IssueDao issueDao;

        SaveIssueToRoom(BeatPoliceDb instance) {
            issueDao = instance.getIssueDao();
        }
        @Override
        protected Void doInBackground(IssueTable... issueTables) {
            issueDao.insert(issueTables[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e(TAG,"successfully done");
            onBackPressed();
            super.onPostExecute(aVoid);
        }
    }



    // Camera Related
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openCamera() {

        Log.e(TAG,"onCameraClick");

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_PERMISSION_CODE);
        }
        else if (ContextCompat.checkSelfPermission(IssueReportPage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(IssueReportPage.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        else {

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"file for image is not created");
                }

                if(photoFile!=null){
                    Uri fileUri = FileProvider.getUriForFile(IssueReportPage.this,"com.example.mani.beatpolice.provider",photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }

            }
        }
    }
    //On camera result
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode != RESULT_CANCELED) {

            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                ImageView imageView = findViewById(R.id.imageview);

                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                compressImage(bitmap);
                Glide.with(IssueReportPage.this)
                        .load(mCurrentPhotoPath)
                        .into(imageView);

            }
        }
    }


    private File createImageFile() throws IOException {

        String timeStamp      = String.valueOf(System.currentTimeMillis()/1000);
        String imageFileName  = timeStamp;
        File storageDir       = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File imageFile        = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.e(TAG,"currentPhotoPath = "+mCurrentPhotoPath);
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
