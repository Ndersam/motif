package com.nders.motif.data;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by nders on 3/5/2018.
 */

public class DataInitService extends IntentService {

    private static final String TAG = DataInitService.class.getSimpleName();

    public DataInitService(){
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MotifDatabaseHelper motifDatabaseHelper = null;
        long startTime = System.currentTimeMillis();
        try {
            motifDatabaseHelper = MotifDatabaseHelper.getInstance(this);
            motifDatabaseHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        finally {
            try {
                if(motifDatabaseHelper != null){
                    motifDatabaseHelper.openDataBase();
                    motifDatabaseHelper.close();
                    Log.i("SERVICE UPDATE", "Data init");
                    Log.i(TAG, "Time: " + (System.currentTimeMillis() - startTime)/1000);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
