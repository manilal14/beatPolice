package com.example.mani.beatpolice.SyncRelated;

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
import com.example.mani.beatpolice.R;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;
import com.example.mani.beatpolice.TodoRelated.TodoTable;
import com.example.mani.beatpolice.TodoRelated.TodoTableDao;

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

public class SyncTodoAdapter extends RecyclerView.Adapter<SyncTodoAdapter.SyncTodoViewHolder> {

    private String TAG = "SyncTodoAdapter";
    private Context mCtx;
    private List<TodoTable> mTodoTableList;


    private MyInterface listener;

    private ProgressDialog mProgressDialog;

    public SyncTodoAdapter(Context mCtx, List<TodoTable> todoTableList, MyInterface listener) {
        this.mCtx = mCtx;
        this.mTodoTableList = todoTableList;
        this.listener = listener;

        mProgressDialog = new ProgressDialog(mCtx);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public SyncTodoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SyncTodoViewHolder(LayoutInflater.from(mCtx).inflate(R.layout.recycler_view_sync_todo,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull SyncTodoViewHolder holder, int i) {

        final TodoTable todo = mTodoTableList.get(i);

        holder.tv_todoId.setText("Todo Id "+todo.getTodoId());
        holder.tv_title.setText(todo.getTitle());

        holder.tv_reportingTime.setText(getFormattedTime(TAG,todo.getReportedAt()));

        holder.tv_from.setText(getFormattedDate(TAG,todo.getFrom())+" "+getFormattedTime(TAG,todo.getFrom()));
        holder.tv_to.setText(getFormattedDate(TAG,todo.getTo())+" "+getFormattedTime(TAG,todo.getTo()));

        if(!todo.getImagePath().equals("null")){
            Log.e(TAG,"a="+todo.getImagePath());
            Glide.with(mCtx)
                    .load(Uri.parse(todo.getImagePath()))
                    .into(holder.iv_image);
        }
        else{
            Log.e(TAG,"Image is null for todo id="+todo.getTodoId());
            holder.iv_image.setImageResource(R.mipmap.image_not_available);

        }

        holder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImageDialog(todo.getImagePath());
            }
        });

        holder.tv_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(todo.getImagePath().equals("null"))
                    syncTodoWithoutImage(todo);
                else
                    syncTodoWithImage(todo);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mTodoTableList.size();
    }

    public class SyncTodoViewHolder extends RecyclerView.ViewHolder {

        TextView tv_todoId;
        TextView tv_reportingTime;
        TextView tv_title;
        TextView tv_from;
        TextView tv_to;
        ImageView iv_image;
        TextView tv_sync;

        public SyncTodoViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_todoId        = itemView.findViewById(R.id.todo_id);
            tv_reportingTime = itemView.findViewById(R.id.reporting_time);
            tv_title         = itemView.findViewById(R.id.title);
            tv_from          = itemView.findViewById(R.id.from);
            tv_to            = itemView.findViewById(R.id.to);
            iv_image         = itemView.findViewById(R.id.imageview);
            tv_sync          = itemView.findViewById(R.id.sync);

        }
    }

    private void setImageDialog(String imagePath){

        AlertDialog.Builder dialog = new AlertDialog.Builder(mCtx);
        View view = LayoutInflater.from(mCtx).inflate(R.layout.dialog_image_view,null);
        dialog.setView(view);

        ImageView imageView   = view.findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(imagePath));
        dialog.create();
    }


    private void syncTodoWithoutImage(final TodoTable todo) {

        mProgressDialog.show();

        Log.e(TAG,"called : syncTodo");
        final String SYNC_URL = BASE_URL + "syncTodoToDb.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SYNC_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int allSuccess = jsonArray.getJSONObject(0).getInt("q_executed");
                    if(allSuccess == 1){
                        Log.e("zxc","deleting");
                        deleteTodoFromRoom(todo);
                    }
                } catch (JSONException e) {
                    mProgressDialog.dismiss();
                    Log.e("zxc",e.toString());
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

                params.put("todo_id", String.valueOf(todo.getTodoId()));
                params.put("p_id",todo.getPId());
                params.put("a_id",todo.getAId());
                params.put("type",todo.getTagType());
                params.put("from",todo.getFrom());
                params.put("to",todo.getTo());
                params.put("reported_at",todo.getReportedAt());
                params.put("des",todo.getDes());
                params.put("lat",todo.getReportedAtLat());
                params.put("lon",todo.getReportedAtLon());

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS * 1000, NO_OF_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(mCtx).addToRequestQueue(stringRequest);

    }

    private void deleteTodoFromRoom(TodoTable todo) {
        new DeleteTodo().execute(todo);
    }

    class DeleteTodo extends AsyncTask<TodoTable,Void,Void>{

        TodoTableDao todoDao = BeatPoliceDb.getInstance(mCtx).getTodoTableDao();

        @Override
        protected Void doInBackground(TodoTable... todoTables) {
            todoDao.deleteById(todoTables[0].getTodoId());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
            listener.updatePage();
        }
    }


    private void syncTodoWithImage(final TodoTable todo) {

        mProgressDialog.show();

        Log.e(TAG,"called : syncTodoWithImage");

        final String SYNC_URL_WITH_IMAGE = BASE_URL + "syncTodoWithImage.php";

        try {
            new MultipartUploadRequest(mCtx,SYNC_URL_WITH_IMAGE)

                    .addFileToUpload(todo.getImagePath(),"image")

                    .addParameter("todo_id", String.valueOf(todo.getTodoId()))
                    .addParameter("p_id",todo.getPId())
                    .addParameter("a_id",todo.getAId())
                    .addParameter("type",todo.getTagType())
                    .addParameter("from",todo.getFrom())
                    .addParameter("to",todo.getTo())
                    .addParameter("reported_at",todo.getReportedAt())
                    .addParameter("des",todo.getDes())
                    .addParameter("lat",todo.getReportedAtLat())
                    .addParameter("lon",todo.getReportedAtLon())

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
                            deleteTodoFromRoom(todo);
                            Toast.makeText(mCtx,"Done",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {}
                    }).startUpload();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e.toString());
        }


    }


}