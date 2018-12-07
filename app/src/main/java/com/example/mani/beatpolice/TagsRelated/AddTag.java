package com.example.mani.beatpolice.TagsRelated;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.R;
import com.google.android.gms.maps.model.LatLng;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.KEY_LATLNG;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_POLICE_ID;

public class AddTag extends AppCompatActivity {

    private  String TAG = this.getClass().getSimpleName();
    private  final int CAMERA_REQUEST = 1888;
    private  final int MY_CAMERA_PERMISSION_CODE = 100;

    private LatLng mTaggedLocation;
    private String mDate;
    private String mTime;

    private String mImagePath;
    private File file;
    private Uri fileUri;

    private ProgressDialog mProgressDialog;
    private LoginSessionManager mSession;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tag);

        mTaggedLocation = (LatLng) getIntent().getExtras().get(KEY_LATLNG);
        mDate = getIntent().getStringExtra("date");
        mTime = getIntent().getStringExtra("time");

        Log.e(TAG,mDate+" "+mTime);

        mProgressDialog = new ProgressDialog(AddTag.this);
        mProgressDialog.setMessage("Please Wait...");

        mSession = new LoginSessionManager(AddTag.this);


        clickListener();
    }

    private void clickListener() {

        final Spinner spinnerTagType = findViewById(R.id.tag_type);

        final List<String> tagItems = new ArrayList<>();
        tagItems.add(getString(R.string.normal_tag));
        tagItems.add(getString(R.string.senior_citizen));

        final LinearLayout tagLayout1 = findViewById(R.id.normal);
        final LinearLayout tagLayout2 = findViewById(R.id.seniour_citizen);

        spinnerTagType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == 0){
                    tagLayout1.setVisibility(View.VISIBLE);
                    tagLayout2.setVisibility(View.GONE);
                }

                else if(position == 1) {
                    tagLayout1.setVisibility(View.GONE);
                    tagLayout2.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTag.this,R.layout.spinner_layout_custom,tagItems);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinnerTagType.setAdapter(adapter);

        LinearLayout takePhotoLayout = findViewById(R.id.take_photo);
        TextView tv_submit           = findViewById(R.id.submit);


        takePhotoLayout.setOnClickListener(new View.OnClickListener() {

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

                        file = new File(AddTag.this.getExternalCacheDir(),
                                String.valueOf(System.currentTimeMillis()) + ".jpg");
                        fileUri = Uri.fromFile(file);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
            }
        });

        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int tagType = spinnerTagType.getSelectedItemPosition();

                EditText et_name    = findViewById(R.id.name);
                EditText et_des     = findViewById(R.id.description);
                EditText et_phone   = findViewById(R.id.phone);
                RadioButton rMale   = findViewById(R.id.male);
                RadioButton rFemale = findViewById(R.id.female);
                EditText et_n_name  = findViewById(R.id.n_name);
                EditText et_n_phone = findViewById(R.id.n_phone);

                String name     = "";
                String des      = "";
                String phone    = "";
                String gender   = "";
                String n_name   = "";
                String n_phone  = "";


                if(tagType == 0){

                    name = et_name.getText().toString();
                    des  = et_des.getText().toString();

                    if(name.equals("") ||des.equals("")){
                        Toast.makeText(AddTag.this,"Both field are required",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if(tagType == 1){

                    name  = et_name.getText().toString();
                    phone = et_phone.getText().toString();

                    if(name.equals("") || phone.equals("")){
                        Toast.makeText(AddTag.this,"Name and mobile are required",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(rFemale.isChecked())
                        gender = "female";
                    else
                        gender = "male";

                    n_name  = et_n_name.getText().toString();
                    n_phone = et_n_phone.getText().toString();
                }

                uploadPicture(tagType,name,des,phone,gender,n_name,n_phone);
            }
        });
    }




    //On camera result
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            ImageView imageView = findViewById(R.id.image);
            Uri selectedImage = fileUri;
            String path = fileUri.getPath();
            getContentResolver().notifyChange(selectedImage, null);
            ContentResolver cr = getContentResolver();
            Bitmap bitmap;
            File actualPath;

            try {
                bitmap = MediaStore.Images.Media
                        .getBitmap(cr, selectedImage);

                actualPath = new File(path);
                imageView.setImageBitmap(bitmap);

                mImagePath = actualPath.getAbsolutePath();

                Log.e(TAG, actualPath.toString());

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

        }
    }

    public void uploadPicture(int tagType, String name, String des, String phone, String gender, String n_name, String n_phone) {

        Log.e(TAG,"called : sendDetailsToDatabase");

        String policeId = mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID);
        String aId      = mSession.getAllotmentDetails().get(KEY_A_ID);

        String uploadUrl = BASE_URL + "add_tags.php/";

        String lat = String.valueOf(mTaggedLocation.latitude);
        String lon = String.valueOf(mTaggedLocation.longitude);

        try {

            Toast.makeText(AddTag.this, "Started...", Toast.LENGTH_SHORT).show();
            Log.e(TAG, mImagePath);
            new MultipartUploadRequest(AddTag.this, uploadUrl)

                    .addFileToUpload(mImagePath, "image")


                    .addParameter("p_id",String.valueOf(policeId))
                    .addParameter("a_id",String.valueOf(aId))
                    .addParameter("t_coord",lat+","+lon)
                    .addParameter("time",mTime)
                    .addParameter("date",mDate)

                    .addParameter("t_type", String.valueOf(tagType))
                    .addParameter("name",name)
                    .addParameter("des",des)
                    .addParameter("phone",phone)
                    .addParameter("gender",gender)
                    .addParameter("n_name",n_name)
                    .addParameter("n_phone",n_phone)


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
                            Toast.makeText(AddTag.this,"Error uploading",Toast.LENGTH_SHORT).show();


                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            mProgressDialog.dismiss();
                            Log.e("TAG4", serverResponse.getBodyAsString());
                            Toast.makeText(AddTag.this,"Uploading done",Toast.LENGTH_SHORT).show();
                            finish();

                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            mProgressDialog.dismiss();
                            Toast.makeText(AddTag.this,"Upload cancle",Toast.LENGTH_SHORT).show();

                        }
                    })
                    .startUpload();

        } catch (Exception e) {
            mProgressDialog.dismiss();
            Log.e(TAG, e.toString());
            Toast.makeText(AddTag.this,"Error uploadinglknl",Toast.LENGTH_SHORT).show();
            finish();


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.e(TAG, "onRequestPermissionsResult: called.");

        switch (requestCode) {

            case MY_CAMERA_PERMISSION_CODE:{

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                    Intent cameraIntent = new
                            Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    onActivityResult(requestCode,requestCode,cameraIntent);

                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
            }

            break;
        }
    }
}
