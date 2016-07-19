package com.softdesign.devintensive.data.network;

import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

/**
 * Created by smalew on 17.07.16.
 */
public class PicassoCache {
    private Context mContext;
    private Picasso mPicassoInstance;

    public PicassoCache(Context context) {
        mContext = context;
        OkHttp3Downloader okHttp3Downloader = new OkHttp3Downloader(mContext, Integer.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mContext);

        mPicassoInstance = builder.build();
        Picasso.setSingletonInstance(mPicassoInstance);
    }

    public Picasso getPicassoInstance() {
        if (mPicassoInstance == null){
            new PicassoCache(mContext);
            return mPicassoInstance;
        }
        return mPicassoInstance;
    }
}