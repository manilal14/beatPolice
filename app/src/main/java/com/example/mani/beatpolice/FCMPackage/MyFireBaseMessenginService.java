package com.example.mani.beatpolice.FCMPackage;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFireBaseMessenginService extends FirebaseMessagingService {

    private static final String TAG = "FCMMessageing";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e(TAG,"NEW_TOKEN " +s);
        storeTokenInSP(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e(TAG,remoteMessage.getNotification().getBody());

        notifyUser(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
    }

    private void storeTokenInSP(String refreshedToken) {
        SharedPrefFcm.getmInstance(getApplicationContext()).storeToken(refreshedToken);
        Log.e(TAG, "token stored in sharedPref : "+SharedPrefFcm.getmInstance(getApplicationContext()).getToken());
    }

    public void notifyUser(String title ,String notification){

        MyFcmNotificationManager myFcmNotificationManager = new MyFcmNotificationManager(getApplicationContext());
        myFcmNotificationManager.createNotification(title,notification);
    }
}
