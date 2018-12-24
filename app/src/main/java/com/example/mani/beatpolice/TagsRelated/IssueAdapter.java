package com.example.mani.beatpolice.TagsRelated;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mani.beatpolice.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.DATE_FORMAT;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder> {

    private final String TAG = "IssueAdapter";
    private Context mCtx;
    private List<Issues> mIssueList;

    public IssueAdapter(Context mCtx, List<Issues> mIssueList) {
        this.mCtx = mCtx;
        this.mIssueList = mIssueList;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mCtx).inflate(R.layout.recycler_view_layout_report_history,viewGroup,false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int i) {

        Issues issues = mIssueList.get(i);
        long unixTime = Long.parseLong(issues.getUnixTime());

        Date dateInMilli = new java.util.Date(unixTime*1000L);

        SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        String formattedDate = sdf.format(dateInMilli);

        String[] s;
        String date = "NA";
        String time = "NA";

        try {
            s = formattedDate.split(",");

             date = s[0];
             time = s[1];

        }catch (Exception e){
            Log.e(TAG,"Exception cought1 : "+ e);
        }


        holder.tv_date.setText(date);
        holder.tv_time.setText(time);
        holder.tv_des.setText(issues.getDes());

    }

    @Override
    public int getItemCount() {
        return mIssueList.size();
    }

    public class IssueViewHolder extends RecyclerView.ViewHolder {

        TextView tv_date;
        TextView tv_time;
        TextView tv_des;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_date  = itemView.findViewById(R.id.date);
            tv_time  = itemView.findViewById(R.id.time);
            tv_des   = itemView.findViewById(R.id.des);

        }
    }
}
