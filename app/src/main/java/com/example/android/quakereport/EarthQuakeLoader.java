package com.example.android.quakereport;

import java.util.ArrayList;

import android.content.Context;
import android.content.AsyncTaskLoader;

public class EarthQuakeLoader extends AsyncTaskLoader<ArrayList<Earthquake>> {

    private static final String LOG_TAG = EarthQuakeLoader.class.getName();
    private String mUrl;


    public EarthQuakeLoader(Context context, String url) {
        super(context);
        mUrl = url;

    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<Earthquake> loadInBackground() {
        if(mUrl==null) {
            return null;
        }
        ArrayList<Earthquake> arrayList = QueryUtils.fetchEarthquakeData(mUrl);
        return arrayList;
    }
}
