package com.motif.motif.data;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import android.util.Log;

import com.motif.motif.Constants;

import java.io.IOException;

/**
 * Created by motif on 3/5/2018.
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
        Log.i(TAG, "Attempting to open database....");
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
                    Log.i(TAG, "Data Check Complete. ( " + (System.currentTimeMillis() - startTime)/1000 + "s )");

                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                    pref.edit().putBoolean(Constants.KEY_DATA_CHECK_COMPLETE, true).apply();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
