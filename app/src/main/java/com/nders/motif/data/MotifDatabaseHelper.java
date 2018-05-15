package com.nders.motif.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nders.motif.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by nders on 30/3/2018.
 */

public class MotifDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = MotifDatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = Constants.DATABASE;
    private final String DATABASE_LOCATION;

    private SQLiteDatabase mDataBase;

    private final Context mContext;

    // Singleton Instance
    private static MotifDatabaseHelper sInstance;


    // Node Table Column Keys
    public static final int KEY_ID = 0;
    public static final int KEY_LABEL = 1;
    public static final int KEY_DEGREE = 2;

    // Edge Table Column Keys
    public static final int KEY_SOURCE = 1;
    public static final int KEY_TARGET = 2;
    public static final int KEY_WEIGHT = 3;


    public static synchronized MotifDatabaseHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new MotifDatabaseHelper(context.getApplicationContext());
        }

        return sInstance;
    }


    /***
     * Constructor is private to prevent direct instantiation.
     * Make a call to the static method "getInstance" instead.
     * @param context
     */
    private MotifDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
        DATABASE_LOCATION = "/data/data/" + mContext.getPackageName() + "/databases/";
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DATABASE_LOCATION + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            Log.i(TAG, checkDB.getPath());
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = mContext.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_LOCATION + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[10];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DATABASE_LOCATION + DATABASE_NAME;
        mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion){
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return mDataBase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public Cursor rawQuery(String query, String[] selectionArgs){
        return mDataBase.rawQuery(query, selectionArgs);
    }
}
