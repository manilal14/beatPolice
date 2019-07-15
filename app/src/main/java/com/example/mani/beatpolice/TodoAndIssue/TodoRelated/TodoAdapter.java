package com.example.mani.beatpolice.TodoAndIssue.TodoRelated;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mani.beatpolice.R;

import java.util.List;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.isCurrentTimeBetweenAllotedTime;
import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.sendNavigateIntent;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private Context mCtx;
    private List<SimpleTodoTable> mSimpleTodoList;

    private int lastPosition = -1;

    public TodoAdapter(Context mCtx, List<SimpleTodoTable> mSimpleTodoList) {
        this.mCtx = mCtx;
        this.mSimpleTodoList = mSimpleTodoList;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new TodoViewHolder(LayoutInflater.from(mCtx).inflate(R.layout.recycler_view_todo,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int i) {

        final SimpleTodoTable simpleTodo = mSimpleTodoList.get(i);

        final String lat = simpleTodo.getLat();
        final String lon = simpleTodo.getLon();

        if(lat.equals("") || lon.equals("")){
            holder.tv_navigate.setVisibility(View.GONE);
        }

        else{
            holder.tv_navigate.setVisibility(View.VISIBLE);
        }


        if(simpleTodo.isChecked() == true) {
            holder.ll_page.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.green_light));
            holder.iv_arrow.setVisibility(View.GONE);

        }
        else {
            holder.ll_page.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.red_light));
            holder.iv_arrow.setVisibility(View.VISIBLE);
        }

        holder.tv_title.setText(simpleTodo.getTitle());
        holder.tv_des.setText(simpleTodo.getDes());


        holder.iv_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isCurrentTimeBetweenAllotedTime(mCtx)){
                    Intent i = new Intent(mCtx,TodoGetDetailsPage.class);
                    i.putExtra("todo_details", simpleTodo);
                    mCtx.startActivity(i);
                }

                else {
                    Toast.makeText(mCtx,"Permission denied",Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.tv_navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendNavigateIntent(mCtx,Double.parseDouble(lat),Double.parseDouble(lon));

            }
        });
    }

    @Override
    public int getItemCount() {
        return mSimpleTodoList.size();
    }

    public class TodoViewHolder extends RecyclerView.ViewHolder {

        LinearLayout ll_page;
        TextView tv_title,tv_des;
        ImageView iv_arrow;
        CardView cardView;

        TextView tv_navigate;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);

            ll_page  = itemView.findViewById(R.id.layout);
            tv_title = itemView.findViewById(R.id.title);
            tv_des   = itemView.findViewById(R.id.des);
            iv_arrow = itemView.findViewById(R.id.arrow);

            tv_navigate = itemView.findViewById(R.id.navigate);


            cardView = itemView.findViewById(R.id.cardview);


        }
    }

}
