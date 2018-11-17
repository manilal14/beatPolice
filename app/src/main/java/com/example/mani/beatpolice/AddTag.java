package com.example.mani.beatpolice;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.mani.beatpolice.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommanVariablesAndFunctuions.KEY_LATLNG;

public class AddTag extends AppCompatActivity {

    private  String TAG = this.getClass().getSimpleName();
    private  final int CAMERA_REQUEST = 1888;
    private  final int MY_CAMERA_PERMISSION_CODE = 100;

    private final String SEND_TAG_URL = BASE_URL + "add_tag_to_database.php";

    private LatLng mTaggedLocation;
    private Bitmap mPhotoTaken;
    private String mImagePath;
    private File file;
    private Uri fileUri;

    //Temp
    int policeId = 1235;

    ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tag);

        mTaggedLocation = (LatLng) getIntent().getExtras().get(KEY_LATLNG);

        mProgressDialog = new ProgressDialog(AddTag.this);
        mProgressDialog.setMessage("Please Wait...");


        clickListener();
    }

    private void clickListener() {

        Spinner spinnerTitle         = findViewById(R.id.spinner_title);
        LinearLayout takePhotoLayout = findViewById(R.id.take_photo);
        TextView tv_submit           = findViewById(R.id.submit);


        List<String> titleItems = new ArrayList<>();
        titleItems.add("Select Title");
        titleItems.add("Title 1");
        titleItems.add("Title 2");
        titleItems.add("Title 3");
        titleItems.add("Title 4");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTag.this,R.layout.spinner_layout_custom ,titleItems);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinnerTitle.setAdapter(adapter);

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

                    //Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                uploadPicture();
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

    public void uploadPicture() {

        Log.e(TAG,"called : sendDetailsToDatabase");
        Spinner spinnerTitle  = findViewById(R.id.spinner_title);

        final int pos = spinnerTitle.getSelectedItemPosition();
        if(pos == 0) {
            Toast.makeText(AddTag.this, "Select title", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText et_des = findViewById(R.id.description);
        final String des      = et_des.getText().toString();
        if(des.equals("")){
            Toast.makeText(AddTag.this, "write dsfsdfdsfdsf", Toast.LENGTH_SHORT).show();
            return;
        }

        final String title = (String) spinnerTitle.getSelectedItem();

        String uploadUrl = BASE_URL + "add_tag_to_database.php/";

        String lat = String.valueOf(mTaggedLocation.latitude);
        String lon = String.valueOf(mTaggedLocation.longitude);

        try {

            Toast.makeText(AddTag.this, "Started...", Toast.LENGTH_SHORT).show();
            Log.e("asd", mImagePath);
            new MultipartUploadRequest(AddTag.this, uploadUrl)

                    .addFileToUpload(mImagePath, "image")

                    .addParameter("p_id",String.valueOf(policeId))
                    .addParameter("title",title)
                    .addParameter("latlng",lat+","+lon)
                    .addParameter("des",des)


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
