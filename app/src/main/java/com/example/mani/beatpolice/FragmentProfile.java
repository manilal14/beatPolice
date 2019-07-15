package com.example.mani.beatpolice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.RoomDatabase.AreaTagTableDao;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.accounts.AccountManager.KEY_PASSWORD;
import static android.app.Activity.RESULT_OK;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.PROFILE_PIC_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_AREA;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_A_TIME;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_AllOT_HIST_ID;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_DATE_END;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_DATE_START;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_NAME;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_PHONE;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_PIC;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_POLICE_ID;


public class FragmentProfile extends Fragment {

    private  String TAG = "FragmentProfile";
    private HomePage mActivity;
    private final String UPLOAD_IMAGE_URL = BASE_URL + "uploadProfilePic.php";

    private LoginSessionManager mSession;
    View mRootView;

    private Bitmap  mBitmap = null;
    private static final int GALLARY_REQUEST = 1;

    private Uri filePath;


    public FragmentProfile() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (HomePage) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestStoragePermission();

        Log.e(TAG, "called : onCreate");
        mSession = new LoginSessionManager(mActivity);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(TAG, "called : onCreateView");
        mRootView = inflater.inflate(R.layout.fragment_profile, container, false);



        CircleImageView profile = mRootView.findViewById(R.id.profile_pic);

        TextView tv_editPhoto  = mRootView.findViewById(R.id.edit_photo);
        EditText et_name       = mRootView.findViewById(R.id.name);
        final EditText et_pass = mRootView.findViewById(R.id.password);
        TextView et_area       = mRootView.findViewById(R.id.area);
        TextView tv_date       = mRootView.findViewById(R.id.a_date);
        TextView et_time       = mRootView.findViewById(R.id.a_time);
        EditText et_phone      = mRootView.findViewById(R.id.phone);

        HashMap<String, String> info = mSession.getPoliceDetailsFromPref();
        et_name.setText(info.get(KEY_NAME));
        et_phone.setText(info.get(KEY_PHONE));

        String aArea = mSession.getAllotmentDetails().get(KEY_AREA);


        if(aArea.equals(""))
            et_area.setText("Not Allotted");
        else
            et_area.setText(aArea);

        String dateFrom =  mSession.getAllotmentDetails().get(KEY_DATE_START);
        String dateTo   =  mSession.getAllotmentDetails().get(KEY_DATE_END);


        String dateRange = "From: "+dateFrom +"  To: "+dateTo;
        tv_date.setText(dateRange);

        String aTime = "Not Allotted";
        String a_time =  mSession.getAllotmentDetails().get(KEY_A_TIME);

        if(a_time.contains(",")){
            try{

                String[] s = a_time.split(",");
                long sUnix = Long.valueOf(s[0]);
                long eUnix = Long.valueOf(s[1]);

                SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
                Date   sTime = new java.util.Date(sUnix*1000L);
                String sfd   = sdf.format(sTime);

                sdf = new java.text.SimpleDateFormat("hh:mm a");
                Date eTime = new java.util.Date(eUnix*1000L);

                String efd = sdf.format(eTime);
                aTime = sfd + " - "+ efd;
            }catch (Exception e){
                Log.e(TAG,"Exception cought 1 " +e);
            }
        }

        et_time.setText(aTime);

        String picName = info.get(KEY_PIC);
        if(picName.equals(""))
            profile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.police));
        else {
            Glide.with(getActivity())
                    .load(PROFILE_PIC_URL +picName)
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(profile);

        }

        tv_editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG,"edit profile is clicked");

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    Intent gallaryIntent = new Intent(Intent.ACTION_PICK);
                    gallaryIntent.setType("image/*");
                    startActivityForResult(gallaryIntent, GALLARY_REQUEST);
                }

                else {
                    Toast.makeText(getActivity(),"Permission not granted",Toast.LENGTH_SHORT).show();
                    requestStoragePermission();
                }
            }
        });
        et_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword(et_pass);
            }
        });

        clickListener();

        return mRootView;
    }


    private void clickListener() {

        mRootView.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLogoutTime(mSession.getAllotmentDetails().get(KEY_AllOT_HIST_ID));
                new ClearAreaTagTable(BeatPoliceDb.getInstance(mActivity)).execute();
            }
        });

    }

    private void updateLogoutTime(final String allotHistId) {

        String MY_URL = BASE_URL + "logout.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, MY_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,response);
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
                params.put("allot_hist_id",allotHistId);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS*1000,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(mActivity).addToRequestQueue(stringRequest);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult");

        if(requestCode == GALLARY_REQUEST && resultCode == RESULT_OK && data != null) {
            filePath = data.getData();
            CropImage.activity(filePath)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(getContext(), this);
        }

        Log.e(TAG, "Request code "+requestCode);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(),resultUri);
                    CircleImageView imageView = mRootView.findViewById(R.id.profile_pic);
                    imageView.setImageBitmap(mBitmap);
                    uploadMultipart();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(mActivity,error.toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void uploadMultipart() {

        final String p_id = mSession.getPoliceDetailsFromPref().get(KEY_POLICE_ID);
        String path = getRealPathFromURI(filePath);

        try {
            String uploadId = UUID.randomUUID().toString();
            new MultipartUploadRequest(getActivity(), uploadId,UPLOAD_IMAGE_URL)
                    .addFileToUpload(path, "image")
                    .addParameter("p_id",p_id)
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) { }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            Toast.makeText(getActivity(),"Failed to upload",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            mSession.updateProfilePicName(p_id+".jpg");
                            Toast.makeText(mActivity,"Upload complete",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            Toast.makeText(getActivity(),"cancled",Toast.LENGTH_SHORT).show();

                        }
                    })
                    .startUpload();
                    Toast.makeText(mActivity,"Uploading in background....",Toast.LENGTH_SHORT).show();

        } catch (Exception exc) {
            Toast.makeText(mActivity, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) { }
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLARY_REQUEST);
    }

    public void resetPassword(final EditText et_pass) {

        final AlertDialog alertDialog;

        View v = LayoutInflater.from(mActivity).inflate(R.layout.dialog_reset_password,null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog = new AlertDialog.Builder(mActivity,android.
                    R.style.Theme_DeviceDefault_Light_Dialog_MinWidth).create();
        } else {
            alertDialog = new AlertDialog.Builder(mActivity).create();
        }

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setView(v);

        final EditText et_current_pass,et_new_pass, et_confirm_pass;
        final TextView password_error;
        TextView done,cancel;


        et_current_pass = v.findViewById(R.id.current_password);
        et_new_pass     = v.findViewById(R.id.new_password);
        et_confirm_pass = v.findViewById(R.id.confirm_password);


        password_error = v.findViewById(R.id.tv_password_error);

        done   = v.findViewById(R.id.done);
        cancel = v.findViewById(R.id.cancel);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String currentPass,newPass,confirmPass;

                currentPass = et_current_pass.getText().toString().trim();
                newPass     = et_new_pass.getText().toString().trim();
                confirmPass = et_confirm_pass.getText().toString().trim();


                if(currentPass.equals("") || newPass.equals("") ||
                        confirmPass.equals("")){
                    password_error.setVisibility(View.VISIBLE);
                    password_error.setText("All field are required");
                    return;
                }

                //if old password is wrong
                if(!currentPass.equals(mSession.getPoliceDetailsFromPref()
                        .get(KEY_PASSWORD))){
                    password_error.setVisibility(View.VISIBLE);
                    password_error.setText("Current password is wrong");
                    et_new_pass.setText("");
                    et_confirm_pass.setText("");

                    return;
                }

                //if new password is shorter than six character
                if(newPass.length()<6){
                    password_error.setVisibility(View.VISIBLE);
                    password_error.setText("New password must be of at least six characters");
                    et_confirm_pass.setText("");
                    return;
                }

                if(!newPass.equals(confirmPass)){
                    password_error.setVisibility(View.VISIBLE);
                    password_error.setText("New password and confirm password do not march");
                    et_confirm_pass.setText("");
                    return;
                }

                password_error.setVisibility(View.GONE);

                //Toast.makeText(EditProfile.this,"new Pass "+newPass,Toast.LENGTH_SHORT).show();

                et_pass.setText(newPass);
                alertDialog.dismiss();

            }

        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();


    }

    class ClearAreaTagTable extends AsyncTask<Void,Void,Void> {

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
            mSession.logout();
            mActivity.stopService(new Intent(mActivity, MyService.class));
            mActivity.finish();
        }
    }


}
