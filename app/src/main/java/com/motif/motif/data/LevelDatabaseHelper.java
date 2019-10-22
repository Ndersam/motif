package com.motif.motif.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.motif.motif.entities.DotColor;
import com.motif.motif.game.Level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;

/**
 * Created by motif on 9/5/2018.
 */

public class LevelDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = LevelDatabaseHelper.class.getSimpleName();

    private static LevelDatabaseHelper sInstance;
    private Context mContext;

    private static final String DATABASE_NAME = "game_db";
    private static final int VERSION = 1;

    private static final String TABLE_LEVELS = "levels";

    // Table Levels Column Names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_UNLOCKED = "unlocked";
    private static final String COLUMN_HIGHSCORE = "highscore";

    private static final String COLUMN_MOVES = "movesLeft";
    private static final String COLUMN_RED = "red";
    private static final String COLUMN_ORANGE = "orange";
    private static final String COLUMN_YELLOW = "yellow";
    private static final String COLUMN_GREEN = "green";
    private static final String COLUMN_BLUE = "blue";
    private static final String COLUMN_INDIGO = "indigo";
    private static final String COLUMN_VIOLET = "violet";


    // Table Levels Column Id
    public static final int KEY_ID = 0;
    public static final int KEY_UNLOCKED = 1;
    public static final int KEY_HIGHSCORE = 2;
    public static final int KEY_MOVES = 3;
    public static final int KEY_RED = 4;
    public static final int KEY_ORANGE = 5;
    public static final int KEY_YELLOW = 6;
    public static final int KEY_GREEN = 7;
    public static final int KEY_BLUE = 8;
    public static final int KEY_INDIGO = 9;
    public static final int KEY_VIOLET = 10;



    public static synchronized LevelDatabaseHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new LevelDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /***
     *  Constructor is private to prevent direct instantiation.
     *  Make a call to the static method "getInstance()" instead.
     * @param context
     */
    private LevelDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_LEVELS +
                " ( " +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_UNLOCKED + " INTEGER, " +
                    COLUMN_HIGHSCORE + " INTEGER, " +
                    COLUMN_MOVES + " INTEGER, " +
                    COLUMN_RED + " INTEGER, " +
                    COLUMN_ORANGE + " INTEGER, " +
                    COLUMN_YELLOW + " INTEGER, " +
                    COLUMN_GREEN + " INTEGER, " +
                    COLUMN_BLUE + " INTEGER, " +
                    COLUMN_INDIGO + " INTEGER, " +
                    COLUMN_VIOLET + " INTEGER " +
                ")";
        db.execSQL(query);
        Log.i(TAG, "Database created");
        readJSONFile(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion != newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEVELS);
            onCreate(db);
        }
        Log.i(TAG, "Upgrade method called");
    }

    public boolean updateLevel(Level level){
        SQLiteDatabase db = getWritableDatabase();

        boolean success = false;

        db.beginTransaction();

        try{
            ContentValues values = new ContentValues();

            EnumMap map = level.getObjective();

            values.put(COLUMN_ID, level.id());
            values.put(COLUMN_UNLOCKED, (level.isUnlocked() ? 1: 0) );
            values.put(COLUMN_HIGHSCORE, level.highScore);
            values.put(COLUMN_MOVES, level.moves());
            values.put(COLUMN_RED, (map.containsKey(DotColor.RED)? (int)map.get(DotColor.RED): 0));
            values.put(COLUMN_ORANGE, (map.containsKey(DotColor.ORANGE)? (int)map.get(DotColor.ORANGE): 0));
            values.put(COLUMN_YELLOW, (map.containsKey(DotColor.YELLOW)? (int)map.get(DotColor.YELLOW): 0));
            values.put(COLUMN_GREEN, (map.containsKey(DotColor.GREEN)? (int)map.get(DotColor.GREEN): 0));
            values.put(COLUMN_BLUE, (map.containsKey(DotColor.BLUE)? (int)map.get(DotColor.BLUE): 0));
            values.put(COLUMN_INDIGO, (map.containsKey(DotColor.INDIGO)? (int)map.get(DotColor.INDIGO): 0));
            values.put(COLUMN_VIOLET, (map.containsKey(DotColor.VIOLET)? (int)map.get(DotColor.VIOLET): 0));

            // Try to update the level
            int rows = db.update(TABLE_LEVELS, values, COLUMN_ID + "= ?",
                    new String[]{"" + level.id()});

            // Check if update succeeded
            if(rows == 1){
                String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_LEVELS +
                        " WHERE " + COLUMN_ID + " = ?";
                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(level.id())});
                if(cursor.moveToFirst()){
                    success = true;
                    db.setTransactionSuccessful();

                    Log.i(TAG, "Level Updated");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }

        return success;
    }

    private void readJSONFile(SQLiteDatabase db){
        Log.i(TAG, db.getPath());

        String filename = "levels.json";

        try{
            InputStream in = mContext.getAssets().open(filename);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            String jsonString = new String(buffer, "UTF-8");

            JSONObject obj = new JSONObject(jsonString);
            JSONArray array = obj.getJSONArray("levels");

            for(int i = 0, len = array.length(); i < len; i++){
                JSONObject levelObj = array.getJSONObject(i);
                JSONArray colors = levelObj.getJSONArray("array");

                ContentValues values = new ContentValues();

                //values.put(COLUMN_ID, levelObj.getInt("mId"));
                Log.i(TAG, "ID " + levelObj.getInt("id"));
                values.put(COLUMN_UNLOCKED, levelObj.getBoolean("unlocked"));
                values.put(COLUMN_HIGHSCORE, levelObj.getInt("highscore"));
                values.put(COLUMN_MOVES, levelObj.getInt("moves"));

                values.put(COLUMN_RED, colors.getInt(0));
                values.put(COLUMN_ORANGE , colors.getInt(1));
                values.put(COLUMN_YELLOW , colors.getInt(2));
                values.put(COLUMN_GREEN , colors.getInt(3) );
                values.put(COLUMN_BLUE , colors.getInt(4));
                values.put(COLUMN_INDIGO, colors.getInt(5));
                values.put(COLUMN_VIOLET , colors.getInt(6));

                db.insertOrThrow(TABLE_LEVELS, null, values);
            }

        }catch (JSONException e){
            Log.e(TAG, "ERROR READING JSON\n" + e.getMessage());
        }catch (IOException e){
            Log.e(TAG, "IO ERROR\n" + e.getMessage());
        }

    }


    /**
     *
     * @param id
     * @return level
     */
    public Level getLevel(int id){
        SQLiteDatabase db = getReadableDatabase();
        if(db == null) return null;
        Cursor cursor = null;
        String query = "SELECT * FROM " + TABLE_LEVELS + " WHERE " + COLUMN_ID + " == " + id + ";";


        Level level = null;
        try{
            cursor = db.rawQuery(query, null);
            if(cursor.moveToFirst()){
                boolean unlocked = cursor.getInt(KEY_UNLOCKED) > 0;
                int moves = cursor.getInt(KEY_MOVES);
                int highscore = cursor.getInt(KEY_HIGHSCORE);

                EnumMap<DotColor, Integer> map = new EnumMap<>(DotColor.class);
                DotColor[] values = DotColor.values();
                for(int i = KEY_RED, j = 0; i <= KEY_VIOLET; i++, j++){
                    map.put(values[j], cursor.getInt(i));
                }

                Log.i(TAG, "I GOT HERE");
                level = new Level(id, unlocked, highscore, moves, map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }

        if(level !=  null)
            Log.i(TAG, level.toString());
        return level;
    }
}
