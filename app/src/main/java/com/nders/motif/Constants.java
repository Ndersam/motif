package com.nders.motif;

/**
 * Created by nders on 2/4/2018.
 */

public  class Constants {

    /*
    *   Data
     */
    public static final String DATABASE =  "motifdata";

    /*
    *   Shared Preferences Keys
     */
    public static final String KEY_SOUND_ENABLED = "sound";
    public static final String KEY_MUSIC_ENABLED = "music";
    public static final String KEY_SCREEN_WIDTH = "screen_width";
    public static final String KEY_SCREEN_HEIGHT = "screen_height";
    public static final String KEY_MAP_RECT_BOTTOM = "scroll_offset";
    public static final String KEY_HIGHEST_LEVEL = "highest_level";
    public static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    public static final String KEY_MUSIC_LEVEL = "music_level";
    public static final String KEY_SOUND_LEVEL = "sound_level";
    public static final String KEY_GAME_DIFFICULTY = "game_difficulty";
    public static final String KEY_GAME_COMPLETE = "game_complete";
    public static final String EASY_MODE = "easy_mode";
    public static final String NORMAL_MODE = "normal_mode";
    public static final String HARD_MODE = "hard_mode";
    public static final String EXTREME_MODE = "extreme_mode";
    public static final String KEY_EDGE_THRESHOLD = "edge_threshold";
    public static final String KEY_FIRST_LAUNCH = "first_launch";
    public static final String KEY_DATA_CHECK_COMPLETE = "data_check_complete";


    /*
    *  Home Menu && AboutAndOptionsActivity
     */
    public static final String KEY_ABOUT_OR_OPTIONS = "about_or_options";
    public static final int VALUE_OPTIONS = 0;
    public static final int VALUE_ABOUT = 1;


    /*
    *   GameLevel
     */

    public static final String KEY_GAME_ID = "game_ID";
    public static final String KEY_GAME_SCORE = "game_score";
    public static final int MAX_LEVEL = 23;

    /*
    *   Game difficulty
     */
    public static final float VALUE_EASY = 2f;
    public static final float VALUE_NORMAL = 4f;
    public static final float VALUE_HARD = 5f;
    public static final float VALUE_EXTREME = 6f;

    public static float DIFFICULTY_MULTIPLIER = 1f;

}
