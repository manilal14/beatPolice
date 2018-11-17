package com.example.mani.beatpolice;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by mani on 2/12/18.
 */


public class MySingleton {

    private static MySingleton mInstance;
    private RequestQueue requestQueue;
    private Context mCtx;

    private MySingleton(Context context)
    {
        mCtx = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue()
    {
        if(requestQueue==null) {
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }
    public static synchronized MySingleton getInstance(Context context)
    {
        if(mInstance == null)
        {
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }
    public<T> void addToRequestQueue(Request<T> request)
    {
        request.setShouldCache(false);
        getRequestQueue().add(request);
    }


}
