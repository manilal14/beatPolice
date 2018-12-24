package com.example.mani.beatpolice;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mani.beatpolice.CommonPackage.MySingleton;
import com.example.mani.beatpolice.TagsRelated.IssueAdapter;
import com.example.mani.beatpolice.TagsRelated.Issues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.BASE_URL;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.NO_OF_RETRY;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.RETRY_SECONDS;

public class ReportHistory extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private int mTagId;
    private List<Issues> mIssuesList;

    private LinearLayout ll_error_layout;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(ReportHistory.this);
        mProgressDialog.setMessage("Please Wait");

        ll_error_layout =   findViewById(R.id.error_layout);

        mTagId = (int) getIntent().getExtras().get("tagId");
        mIssuesList = new ArrayList<>();
        fetchReportHistory();
    }

    private void fetchReportHistory() {

        Log.e(TAG, "called : fetchReportHistory");

        mProgressDialog.show();

        String SEND_URL = BASE_URL + "fetch_tag_report_history.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SEND_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,response);

                if(!mIssuesList.isEmpty())
                    mIssuesList.clear();

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    int rc = jsonObject.getInt("response_code");

                    if(rc<=0){
                        String message = jsonObject.getString("message");
                        Log.e(TAG,message);
                        mProgressDialog.dismiss();
                        ll_error_layout.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> desList = new ArrayList<>();

                    for(int i=1;i<jsonArray.length();i++){

                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                        int issueId     = Integer.parseInt(jsonObject1.getString("id"));
                        String des      = jsonObject1.getString("des");
                        String unixTime = jsonObject1.getString("time");
                        desList.add(des);
                        mIssuesList.add(new Issues(issueId,des,unixTime));

                    }

                    if(mIssuesList.size() == 0){
                        mProgressDialog.dismiss();
                        ll_error_layout.setVisibility(View.VISIBLE);
                        return;
                    }

                    RecyclerView recyclerView = findViewById(R.id.recycler_view);
                    recyclerView.setHasFixedSize(true);
                    IssueAdapter adapter = new IssueAdapter(ReportHistory.this,mIssuesList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ReportHistory.this));
                    recyclerView.setAdapter(adapter);
                    mProgressDialog.dismiss();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
                mProgressDialog.dismiss();
                ll_error_layout.setVisibility(View.VISIBLE);
                Toast.makeText(ReportHistory.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                HashMap<String, String> params = new HashMap<>();
                params.put("tag_id",String.valueOf(mTagId));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS,NO_OF_RETRY,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(ReportHistory.this).addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
