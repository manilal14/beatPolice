package com.example.mani.beatpolice.CommonPackage;

import android.app.Application;

import com.example.mani.beatpolice.BuildConfig;

import net.gotev.uploadservice.UploadService;

public class Initializer extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
    }
}
