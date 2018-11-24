package com.example.mani.beatpolice.TagsRelated;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mani.beatpolice.R;

import static com.example.mani.beatpolice.CommonPackage.CommanVariablesAndFunctuions.TAG_PIC_URL;

public class SSTagInfo extends AppCompatActivity {

    private final String TAG = SSTagInfo.this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sstag_info);

        final Tag tagInfo = (Tag) getIntent().getExtras().getSerializable("tagInfo");

        TextView tv_name    = findViewById(R.id.name);
        TextView tv_phone   = findViewById(R.id.phone);
        TextView tv_gender  = findViewById(R.id.gender);
        TextView tv_n_name  = findViewById(R.id.n_name);
        TextView tv_n_phone = findViewById(R.id.n_phone);
        TextView tv_verified = findViewById(R.id.verified);
        TextView tv_report = findViewById(R.id.report);

        final ImageView    imageView = findViewById(R.id.image);
        final ProgressBar progressBar = findViewById(R.id.image_progress_bar);

        String imageName = tagInfo.getImageName();

        if(!imageName.equals("")){

            imageName = TAG_PIC_URL + imageName;
            Log.e(TAG,"image url : "+imageName);

            Glide.with(SSTagInfo.this)
                    .load(imageName)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            imageView.setBackgroundResource(R.mipmap.image_not_available);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SSTagInfo.this,getString(R.string.image_not_available),Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);

        }
        else {
            progressBar.setVisibility(View.GONE);
        }


        tv_name.setText(tagInfo.getName());
        tv_phone.setText(tagInfo.getPhone());
        tv_gender.setText(tagInfo.getGender());
        tv_n_name.setText(tagInfo.getN_name());
        tv_n_phone.setText(tagInfo.getN_phone());


        tv_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = tagInfo.getPhone();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

        tv_n_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = tagInfo.getN_phone();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });


        tv_verified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SSTagInfo.this,"Verified",Toast.LENGTH_SHORT).show();
            }
        });

        tv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SSTagInfo.this,"Report",Toast.LENGTH_SHORT).show();
            }
        });










    }
}
