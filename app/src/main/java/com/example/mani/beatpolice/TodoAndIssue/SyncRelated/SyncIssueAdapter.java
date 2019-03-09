package com.example.mani.beatpolice.TodoAndIssue.SyncRelated;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.mani.beatpolice.CommonPackage.MyInterface;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.LoginRelated.LoginSessionManager;
import com.example.mani.beatpolice.R;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;
import com.example.mani.beatpolice.TodoAndIssue.IssueRelated.IssueDao;
import com.example.mani.beatpolice.TodoAndIssue.IssueRelated.IssueTable;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.getFormattedDate;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.getFormattedTime;
import static com.example.mani.beatpolice.LoginRelated.LoginSessionManager.KEY_ALLOT_ID;

public class SyncIssueAdapter extends RecyclerView.Adapter<SyncIssueAdapter.IssueViewHolder> {

    private final String TAG = "SyncIssueAdapter";
    private Context mCtx;
    private List<IssueTable> mIssueTableList;

    private ProgressDialog mProgressDialog;
    private MyInterface listener;

    public SyncIssueAdapter(Context mCtx, List<IssueTable> mIssueTableList,MyInterface listener) {
        this.mCtx = mCtx;
        this.mIssueTableList = mIssueTableList;
        mProgressDialog = new ProgressDialog(mCtx);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);

        this.listener = listener;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new IssueViewHolder(LayoutInflater.from(mCtx).inflate(R.layout.recycler_view_sync_issue,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int i) {

        final IssueTable issue = mIssueTableList.get(i);

        holder.tv_issueId.setText("Issue Id "+issue.getIssueId());
        holder.tv_title.setText(issue.getIssueType());

        holder.tv_reportingTime.setText(getFormattedTime(TAG,issue.getReportedAtTime()));

        holder.tv_from.setText(getFormattedDate(TAG,issue.getFrom())+" "+getFormattedTime(TAG,issue.getFrom()));
        holder.tv_to.setText(getFormattedDate(TAG,issue.getTo())+" "+getFormattedTime(TAG,issue.getTo()));

        if(issue.getImagePath() != null){
            Log.e(TAG,"a="+issue.getImagePath());
            Glide.with(mCtx)
                    .load(issue.getImagePath())
                    .into(holder.iv_image);
        }
        else{
            Log.e(TAG,"Image is null for issue id="+issue.getIssueId());
            holder.iv_image.setImageResource(R.mipmap.image_not_available);

        }

        holder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImageDialog(issue.getImagePath());
            }
        });

        holder.tv_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(issue.getImagePath() == null)
                    syncIssueWithoutImage(issue);
                else
                    syncIssueWithImage(issue);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mIssueTableList.size();
    }

    public class IssueViewHolder extends RecyclerView.ViewHolder {

        TextView tv_issueId;
        TextView tv_reportingTime;
        TextView tv_title;
        TextView tv_from;
        TextView tv_to;
        ImageView iv_image;
        TextView tv_sync;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_issueId       = itemView.findViewById(R.id.issue_id);
            tv_reportingTime = itemView.findViewById(R.id.reporting_time);
            tv_title         = itemView.findViewById(R.id.title);
            tv_from          = itemView.findViewById(R.id.from);
            tv_to            = itemView.findViewById(R.id.to);
            iv_image         = itemView.findViewById(R.id.imageview);
            tv_sync          = itemView.findViewById(R.id.sync);

        }
    }

    private void setImageDialog(String imagePath){

        Log.e(TAG,"opening image in a dialog");

        AlertDialog.Builder dialog = new AlertDialog.Builder(mCtx);
        View view = LayoutInflater.from(mCtx).inflate(R.layout.dialog_image_view,null);
        dialog.setView(view);

        ImageView imageView   = view.findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(imagePath));
        dialog.create();

        dialog.show().getWindow().getAttributes().windowAnimations = R.style.up_down;
    }

    private void syncIssueWithoutImage(final IssueTable issue) {

        mProgressDialog.show();
        Log.e(TAG,"called : syncTodo");

        final String SYNC_URL = BASE_URL + "syncIssueWithoutImage1.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SYNC_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,response);

                mProgressDialog.dismiss();

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int allSuccess = jsonArray.getJSONObject(0).getInt("q_executed");
                    if(allSuccess == 1){
                        Log.e("asd","deleting_issue with id "+issue.getIssueId());

                        new DeleteIssue().execute(issue);
                    }
                } catch (JSONException e) {
                    mProgressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                Log.e(TAG,error.toString());
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<>();

                String allotId = new LoginSessionManager(mCtx).getAllotmentDetails().get(KEY_ALLOT_ID);

                params.put("allot_id",allotId);
                params.put("a_id",issue.getAId());
                params.put("issue_id",issue.getIssueType());
                params.put("des",issue.getDes());
                params.put("from",issue.getFrom());
                params.put("to",issue.getTo());
                params.put("reported_at_time",issue.getReportedAtTime());
                params.put("reported_at_location",issue.getReportedAtLocation());

                params.put("lat",issue.getLocationLatitude());
                params.put("lon",issue.getLocationLongitude());

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS * 1000, NO_OF_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(mCtx).addToRequestQueue(stringRequest);

    }

    class DeleteIssue extends AsyncTask<IssueTable,Void,Void> {

        IssueDao issueDao = BeatPoliceDb.getInstance(mCtx).getIssueDao();

        @Override
        protected Void doInBackground(IssueTable... issueTables) {
            issueDao.deleteById(issueTables[0].getIssueId());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listener.updatePage();
        }
    }

    private void syncIssueWithImage(final IssueTable issue) {

        mProgressDialog.show();

        Log.e(TAG,"called : syncIssueWithImage");

        String allotId = new LoginSessionManager(mCtx).getAllotmentDetails().get(KEY_ALLOT_ID);
        final String SYNC_URL_WITH_IMAGE = BASE_URL + "syncIssueWithImage1.php";
        try {
            new MultipartUploadRequest(mCtx,SYNC_URL_WITH_IMAGE)

                    .addFileToUpload(issue.getImagePath(),"image")

                    .addParameter("allot_id",allotId)
                    .addParameter("a_id",issue.getAId())
                    .addParameter("issue_id",issue.getIssueType())
                    .addParameter("from",issue.getFrom())
                    .addParameter("to",issue.getTo())
                    .addParameter("reported_at_time",issue.getReportedAtTime())
                    .addParameter("reported_at_location",issue.getReportedAtLocation())
                    .addParameter("des",issue.getDes())
                    .addParameter("lat",issue.getLocationLatitude())
                    .addParameter("lon",issue.getLocationLongitude())

                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {}

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            mProgressDialog.dismiss();
                            Log.e("TAG",serverResponse.getBodyAsString());
                            if(exception!=null)
                                Log.e("TAG",exception.toString());
                            Toast.makeText(mCtx,"Error Syncing",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            Log.e("TAG",serverResponse.getBodyAsString());
                            mProgressDialog.dismiss();
                            new DeleteIssue().execute(issue);
                            Toast.makeText(mCtx,"Done",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            mProgressDialog.dismiss();
                        }
                    }).startUpload();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e.toString());
        }


    }
}
