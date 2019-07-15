package com.example.mani.beatpolice.TodoAndIssue.TodoRelated;

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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mani.beatpolice.GPSTracker;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.R;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.TIME_FORMAT;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.TODAY;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_POLICE_ID;

public class TodoGetDetailsPage extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private  final int CAMERA_REQUEST = 1888;
    private  final int MY_CAMERA_PERMISSION_CODE = 100;

    private long mUnixFrom = 0;
    private long mUnixTo = 0;
    private long mUnixReportedAt = 0;

    private LatLng mMyLocation;

    private String mCurrentPhotoPath;


    LoginSessionManager mSession;
    SimpleTodoTable mSimpleTodo;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_get_details);

        mSession = new LoginSessionManager(TodoGetDetailsPage.this);
        mSimpleTodo = (SimpleTodoTable) getIntent().getSerializableExtra("todo_details");

        getLocationPermission();

        setView();
        clickListener();


    }


    private void setView(){

        TextView tv_todoId = findViewById(R.id.todo_id);
        tv_todoId.setText("Todo Id "+mSimpleTodo.getId());

        Spinner spinnerType = findViewById(R.id.type);

        List<String> typeList = new ArrayList<>();
        typeList.add("Everything Good");
        typeList.add("Having Isuses");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(TodoGetDetailsPage.this,
                R.layout.spinner_layout_custom2,typeList);

        spinnerType.setAdapter(adapter);

    }

    private void clickListener(){

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final Spinner spinner_type = findViewById(R.id.type);
        TextView tv_from     = findViewById(R.id.from);
        TextView tv_to       = findViewById(R.id.to);
        final TextView tv_des      = findViewById(R.id.des);

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

        findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });


        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String des = tv_des.getText().toString().trim();

                if(mUnixFrom==0 || mUnixTo==0){
                    Toast.makeText(TodoGetDetailsPage.this,"From and To are required",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(des.equals("")) {
                    Toast.makeText(TodoGetDetailsPage.this,"Description is required",Toast.LENGTH_SHORT).show();
                    return;
                }

                String type = (String) spinner_type.getSelectedItem();

                String areaId     = mSession.getAllotmentDetails().get(KEY_A_ID);
                String  policeId  = mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID);

                Long currentUnix = System.currentTimeMillis()/1000;

                TodoTable aTodo = new TodoTable(mSimpleTodo.getId(), policeId, areaId, mSimpleTodo.getTitle(), type, String.valueOf(mUnixFrom),
                        String.valueOf(mUnixTo), String.valueOf(currentUnix), mCurrentPhotoPath,des,
                        String.valueOf(mMyLocation.latitude), String.valueOf(mMyLocation.longitude));

                new SaveTodoToRoom(BeatPoliceDb.getInstance(TodoGetDetailsPage.this)).execute(aTodo);

            }
        });

    }


    public void setDateTimePicker(final TextView textView) {

        final Calendar currentDate = Calendar.getInstance();
        final Calendar calender = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(TodoGetDetailsPage.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calender.set(year, monthOfYear, dayOfMonth);

                TimePickerDialog timePickerDialog = new TimePickerDialog(TodoGetDetailsPage.this, new TimePickerDialog.OnTimeSetListener() {
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

    class SaveTodoToRoom extends AsyncTask<TodoTable,Void,Void> {

        private final TodoTableDao todoTableDao;
        private final SimpleTodoDao simpleTodoDao;

        SaveTodoToRoom(BeatPoliceDb instance) {
            todoTableDao = instance.getTodoTableDao();
            simpleTodoDao = instance.getSimpleTodoTableDao();
        }

        @Override
        protected Void doInBackground(TodoTable... todoTables) {
            todoTableDao.insert(todoTables[0]);
            simpleTodoDao.setIsChecked(mSimpleTodo.getId(),true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e(TAG,"successfully done");
            onBackPressed();
            super.onPostExecute(aVoid);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocationPermission() {

        Log.e(TAG, "getLocationPermission");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(TodoGetDetailsPage.this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(TodoGetDetailsPage.this,
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                getDeviceLocation();

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

        LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);

        GPSTracker tracker = new GPSTracker(TodoGetDetailsPage.this);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {

            mMyLocation = new LatLng(tracker.getLatitude(), tracker.getLongitude());

            if (mMyLocation == null) {
                Log.e("zxc", "myLocation is null");
                Toast.makeText(TodoGetDetailsPage.this, "Please turn on gps first", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.e("zxc", "myLocation found : " + mMyLocation);
        }

    }




    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openCamera() {

        Log.e(TAG,"onCameraClick");

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_PERMISSION_CODE);
        }
        else if (ContextCompat.checkSelfPermission(TodoGetDetailsPage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(TodoGetDetailsPage.this,
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

                if(photoFile!=null) {
                    Uri fileUri = FileProvider.getUriForFile(TodoGetDetailsPage.this, "com.example.mani.beatpolice.provider", photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        }
    }

    //On camera result
    protected void onActivityResult(int requestCode, int resultCode, Intent data)   {

        if (requestCode != RESULT_CANCELED)
        {

            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

                ImageView imageView = findViewById(R.id.imageview);
                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                compressImage(bitmap);
                Glide.with(TodoGetDetailsPage.this)
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
