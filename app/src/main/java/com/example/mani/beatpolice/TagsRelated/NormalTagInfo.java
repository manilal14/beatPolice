package com.example.mani.beatpolice.TagsRelated;

import android.graphics.drawable.Drawable;
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

public class NormalTagInfo extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_tag_info);

        Tag tag = (Tag) getIntent().getExtras().getSerializable("tagInfo");
        Log.e(TAG,tag.getId() +"");

        TextView tv_title             = findViewById(R.id.title);
        TextView tv_des               = findViewById(R.id.des);
        final ImageView imageView     = findViewById(R.id.image);
        final ProgressBar progressBar = findViewById(R.id.image_progress_bar);

        tv_title.setText(tag.getName());
        tv_des.setText(tag.getDes());


        String imageName = tag.getImageName();

        if(!imageName.equals("")) {

            imageName = TAG_PIC_URL + imageName;
            Log.e(TAG,"image url : "+imageName);

            Glide.with(NormalTagInfo.this)
                    .load(imageName)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                            imageView.setBackgroundResource(R.mipmap.image_not_available);
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(NormalTagInfo.this,getString(R.string.image_not_available),Toast.LENGTH_SHORT).show();
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


    }
}
