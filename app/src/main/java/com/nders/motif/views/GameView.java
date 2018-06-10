package com.nders.motif.views;

import android.app.Activity;
import android.content.Context;
import android.database.SQLException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nders.motif.R;
import com.nders.motif.SoundHelper;
import com.nders.motif.Utils;
import com.nders.motif.data.LevelDatabaseHelper;
import com.nders.motif.data.Loader;
import com.nders.motif.entities.Dot;
import com.nders.motif.entities.DotColor;
import com.nders.motif.entities.DotNode;
import com.nders.motif.entities.Line;
import com.nders.motif.game.Level;
import com.nders.motif.game.State;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;



public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback, Loader.LoaderListener{

    private static final String TAG = GameView.class.getSimpleName();


    /**
     **   LAYOUT
     */

    // Horizontal spacing between dots
    private static final int HORIZONTAL_SPACING = 80;

    // Vertical spacing between dots
    private static final int VERTICAL_SPACING = 80;

    // The number of dots in a row.
    private static final int MAX_ROW_COUNT = 6;

    // The number of dots in a column.
    private static final int MAX_COLUMN_COUNT = 6;

    // Height and width of the area allocated for playing the dots.
    // This area is centered within the view.
    private static int DIMENSION;

    protected static final float DOT_TOLERANCE = 35;

    // Radius of main dots - dots centred in the view.
    protected static final int RADIUS = 50;

    // Radius of smaller dots drawn in the header
    protected static final int RADIUS_SMALL = 40;

    // Spacing between the smaller dots drawn in the header.
    protected static final float HEADER_DOTS_SPACING = 90;

    // The height setting of the header and the footer.
    protected static final int HEADER_FOOTER_HEIGHT = 250;

    protected static final int SCORE_TEXT_DIM = 300;




    /**
     **   DRAWING
     */

    // Partitions
    protected Rect headerRect;
    protected Rect footerRect;
    protected Rect scoreRect;
    protected Rect movesRect;
    protected Rect labelRect;

    // Colors
    protected int BACKGROUND_COLOR  = Color.WHITE;
    protected int HEADER_FOOTER_COLOR = Color.parseColor("#FFF6D5");
    protected int SCORE_COLOR =  HEADER_FOOTER_COLOR;
    protected int TEXT_COLOR = Color.parseColor("#202020");
    protected int TEXT_MOVES_COLOR = Color.parseColor("#717171");

    // Paints
    protected Paint mTextPaint;
    protected Paint mTickMarkPaint;
    protected Paint mBlackDotPaint;
    protected Paint mColorDotPaint;
    protected Paint mPathPaint;
    protected Paint mHeaderFooterPaint;

    // Tracks the position of the initial dot touched when a player is about connect ...
    // ... a series of dots.
    protected float mStartX;
    protected float mStartY;

    // Tracks the touch position.
    // mx is short for move event X value
    // my is short for move event Y value.
    protected float mx;
    protected float my;
    protected float last_mx;
    protected float last_my;

    // Text Params
    protected static final float TEXT_SIZE_MEDIUM = 45;
    protected static final float TEXT_SIZE_LARGE = 150;
    protected static final float TEXT_SIZE_SMALL = 40;


    protected static final float TOUCH_STROKE_WIDTH = 40;
    protected static final float TOUCH_TOLERANCE = 2;


    /**
     **  CONTAINERS
     */

    // Keeps track of the stationary dots drawn on screen.
    protected List<List<Dot>> mDotRows = new ArrayList<>();

    // Keeps track of all the lines drawn
    protected Stack<Line> mLines = new Stack<>();

    // The first selected dot
    protected Dot mStartDot = null;

    protected Stack<Dot> mSelectedDots = new Stack<>();


    /**
     **  DATA
     */

    // Stores the relationship between dots.
    // A pair of dots (represented as an ArrayList) is the key; while the "existence" of an ...
    // ... edge (boolean) is the value.
    protected HashMap<ArrayList, Boolean> mEdges = new HashMap<>();

    protected List<DotNode> mStartingNodes;

    // List of remaining nodes loaded from the database.
    // The content of this container are gradually removed and used to replace ...
    // .. selected dots in "mStartingNodes".
    protected List<DotNode> mNodes = new ArrayList<>();

    // The size of buffer
    protected int mBufferSize = 0;

    // Object delegated with the responsibility of querying and loading data ...
    // ... from the database.
    protected Loader mDataLoader;

    /**
     **  ANIMATION
     */

    static int TIME_DELAY;



    /**
     **  GAME CONTROL
     */

    // This thread is used in tandem with the "main" or UI thread.
    // It handles the all drawing calls besides the first initialization of the game.
    protected Thread mGameThread = null;

    enum STATE {ACTION_DOWN, ACTION_UP, MOVING, RESET, DO_NOTHING}

    // OnTouch State for passing control from the UI thread to the GameThread.
    protected STATE mState = STATE.RESET;

    // Is true when data (nodes) have been loaded from the database.
    protected boolean mDoneLoadingData = false;

    // Is true when the associated activity starts or resumes.
    protected boolean  mSurfaceReady = false;

    // Is true when the associated activity is not paused and is in focus.
    protected volatile boolean mRunning = false;

    // Is true when the surface is ready, data has been loaded and the game has been ...
    // ... initialized.
    // It is set to false when game is paused or drawing is occurring.
    // When the game is not ready, user input (touch input) is ignored.
    protected boolean mReady = false;

    // Is true when the SurfaceView has been created and it's "OnSurfaceCreated" callback ...
    // ... method has been called.
    protected boolean mSurfaceCreated = false;

    // Is true when the SurfaceView has been destroyed. This boolean is set true in the ...
    // ... SurfaceView's "OnSurfaceDestroyed" callback.
    protected boolean mSurfaceWasDestroyed = true;

    // Object holding the details of the current game level.
    protected Level mGameLevel = null;

    protected boolean mIsDrawing = false;

    // The current game state
    protected State mGameState = null;

    protected boolean mAPathClosed = false;

    protected boolean mObjectiveShown = false;

    /**
     **   MISC
     */

    // Reference to the SurfaceView's surface holder.
    protected SurfaceHolder mSurfaceHolder;

    // Callback interface for handling "game complete" and "game over" events.
    protected GameListener mGameListener = null;

    // Static reference to the GameView to prevent the recreation of multiple instances.
    private static GameView sInstance;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Constructors
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Constructor is private to prevent direct instantiation.
     * Make a call to the static method "getInstance" instead.
     */
    private GameView(Context context) {
        this(context, null);
    }

    /**
     * Constructor is private to prevent direct instantiation.
     * Make a call to the static method "getInstance" instead.
     */
    private GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Constants
        TIME_DELAY = (int) (1000/((Activity)context).getWindowManager()
                .getDefaultDisplay().getRefreshRate());


        // Paints

        mColorDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mColorDotPaint.setStyle(Paint.Style.FILL);
        mColorDotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mBlackDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mBlackDotPaint.setStyle(Paint.Style.FILL);
        mBlackDotPaint.setColor(getContext().getColor(R.color.black));
        mBlackDotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mTickMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mTickMarkPaint.setStyle(Paint.Style.STROKE);
        mTickMarkPaint.setStrokeWidth(8);
        mTickMarkPaint.setStrokeJoin(Paint.Join.ROUND);
        mTickMarkPaint.setStrokeCap(Paint.Cap.ROUND);
        mTickMarkPaint.setColor(Color.WHITE);
        mTickMarkPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeWidth(TOUCH_STROKE_WIDTH);
        mPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setStrokeWidth(TOUCH_STROKE_WIDTH - 2);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mHeaderFooterPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mHeaderFooterPaint.setStyle(Paint.Style.FILL);
        mHeaderFooterPaint.setColor(HEADER_FOOTER_COLOR);
        mHeaderFooterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));


        // Data Loader
        mDataLoader = new Loader(context,36);
        mDataLoader.setLoadListener(this);

        // Surface Holder
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        mSurfaceHolder.addCallback(this);
        setDrawingCacheEnabled(true);
    }


    /**
     * Static method used to create a singleton instance of the class
     * @param context Activity context
     * @return static instance of this class
     */
    public static GameView getInstance(Context context){
        if(sInstance == null){
            sInstance = new GameView(context);
        }else{
            // necessary to redraw the surface
            sInstance.mState = STATE.RESET;
        }
        return sInstance;
    }

    /**
     * Used to quit and destroy the Singleton static instance of the class.
     */
    public static void quitInstance(){
        if(sInstance != null){
            sInstance.close();
            sInstance = null;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Drawing Methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(!hasFocus){
            pause();
        }else{
            resume();
        }
        mState = STATE.RESET;
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // hack to cancel multitouch events
        if(event.getPointerCount() > 1){
            mState = STATE.RESET;
            return super.onTouchEvent(event);
        }
        if(!mReady) return false;

        // Retrieve the point
        mx = event.getX();
        my = event.getY();

        // Handles the drawing of lines
        onTouchEventLine(event);
        return true;
    }


    private void onTouchEventLine(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // First touch. Store the initial point
                mIsDrawing = true;
                mStartX = mx;
                mStartY = my;
                for(List<Dot> list: mDotRows){
                    for(Dot dot: list){
                        if(dot.contains(mStartX, mStartY)){
                            mStartX = dot.centreX();
                            mStartY = dot.centreY();
                            mStartDot = dot;
                            mColorDotPaint.setColor(mStartDot.color());
                            mPathPaint.setColor(mStartDot.color());
                            mStartDot.select();
                            mSelectedDots.push(mStartDot);

                            SoundHelper.getInstance((Activity)getContext()).playKickSound();
                            break;
                        }
                    }
                }
                if(mStartDot != null){
                    mState = STATE.ACTION_DOWN;
                }else{
                    mState = STATE.DO_NOTHING;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mStartDot == null){
                    break;
                }
                if(!mIsDrawing){
                    mState = STATE.MOVING;
                    break;
                }

                for(List<Dot> list: mDotRows){
                    for(Dot dot: list){
                        //==============================================================
                        // IF "rect" CONTAINS THE COORDINATE, {mx, my},...
                        // A LINE COULD BE DRAWN
                        //==============================================================
                        if(dot.contains(mx, my)){
                            float mx_raw = mx; // save raw current x value
                            float my_raw = my; // save raw current y value
                            mx = dot.centreX();
                            my = dot.centreY();

                            // rect.id() != mStartDot.id() &&
                            if(!dot.isSelected()){
                                if(mDataLoader.checkEdge(mStartDot.id(), dot.id(), mEdges)) {
                                    //=========================================================
                                    // IF LINE TO BE DRAWN IS NON-DIAGONAL
                                    //=========================================================
                                    if (mx == mStartX || my == mStartY) {
                                        //Log.i(TAG, "TOUCHED: " + rect.mId());
                                        // new line to be added
                                        Line line = new Line(mStartX, mStartY, mx, my);
                                        line.startId = mSelectedDots.peek().id();
                                        line.endId = dot.id();

                                        //========================================================
                                        // BACKTRACK (IF CONDITION IS MEANT)
                                        //========================================================
//                                        if (mLines.size() > 0) {
//                                            double distFrom2ndLastDot =
//                                                    Math.sqrt(Math.pow(mLines.peek().startX - mx_raw, 2) +
//                                                    Math.pow(mLines.peek().startY - my_raw, 2));
//                                            // The sum, (VERTICAL_SPACING + 2 * RADIUS - RECT_TOLERANCE),
//                                            // is the value below which the most recent line is removed
//                                            // of the stack.
//
//                                            // This allows for undo movesLeft
//
//                                            if (distFrom2ndLastDot <= (VERTICAL_SPACING + 2 * RADIUS - RECT_TOLERANCE)) {
//                                                mStartX = mLines.peek().startX;
//                                                mStartY = mLines.peek().startY;
//                                                mLines.pop();
//
//                                                // pop rectangle off stack
//                                                rect.deselect();
//                                                if(!mSelectedDots.empty()) mSelectedDots.pop();
//
//                                                break;
//                                            }
//                                        }

                                        //==========================================================
                                        // ADD NEW LINE
                                        //==========================================================

                                        // Add new line iff the line (to be drawn) connects
                                        // only adjacent dots

                                        // The sum, (VERTICAL_SPACING + 2*RADIUS), is the maximum
                                        // distance between the centres of two adjacent dots

                                        if (line.length() <= (VERTICAL_SPACING + 2 * RADIUS)){

                                            //==========================================================
                                            // IF RECTANGLE HASN'T BEEN CONNECTED
                                            //==========================================================
                                            if (!dot.isSelected()) {
                                                mLines.push(line);
                                                mStartX = mx;
                                                mStartY = my;

                                                // push rectangle onto stack
                                                dot.select();
                                                mSelectedDots.push(dot);
                                                SoundHelper.getInstance((Activity)getContext()).playKickSound();

//                                                double distFromLastDot = Math.sqrt(Math.pow(mLines.peek().endX - mx_raw, 2) +
//                                                        Math.pow(mLines.peek().endY - my_raw, 2));
//                                                if (mOnDrawingPathListener != null && distFromLastDot > (VERTICAL_SPACING / 2)) {
//                                                    mOnDrawingPathListener.onDotConnected(mSelectedDots.size());
//                                                }
                                            }
                                        }
                                    } //### END IF (LINE TO BE DRAWN IS NON-DIAGONAL)

                                }
                            }
                            else{
                                //=================================================
                                // CLOSE PATH
                                //=================================================
                                if(mSelectedDots.peek().id() == dot.id()){
                                    break;
                                }

                                // if line is non-diagonal
                                if (mx == mStartX || my == mStartY) {

                                    // new line to be added
                                    Line line = new Line(mStartX, mStartY, mx, my);
                                    line.startId = mSelectedDots.peek().id();
                                    line.endId = dot.id();

                                    // To close a rectangular path 4 lines are needed.
                                    // If mLines has at least 3 lines, close path
                                    if(mLines.size() >= 3 && (line.length() <= (VERTICAL_SPACING + 2 * RADIUS)) && line.length() >=(VERTICAL_SPACING) ){
                                        Log.i(TAG, "Line Valid");

                                        List<Line> lines = new ArrayList<>();

                                        // Get the last added line
                                        int i = mLines.size() - 1;
                                        Line temp = new Line(mLines.get(i));

                                        Log.i(TAG, String.format("(%03d, %03d)",
                                                mLines.get(i).startId, mLines.get(i).endId) + " " + mLines.get(i).type());

                                        // Concatenate lines that are of the same type.
                                        // Two line are said to be of the same type if they share end points and are both horizontal
                                        // or both vertical.
                                        // Find 3 concatenated lines (if exists)
                                        // If 3 concatenated lines exists and they form an opened rectangle, and the line to be added ...
                                        // ... shares a point with the line to be added, a path has been closed.
                                        for(i = mLines.size() - 2; i >= 0; i--){

                                            Log.i(TAG, String.format("(%03d, %03d)",
                                                    mLines.get(i).startId, mLines.get(i).endId) + " " + mLines.get(i).type());

                                            if(temp.type() == mLines.get(i).type()){
                                                temp.merge(mLines.get(i));
                                            }else{
                                                lines.add(temp);
                                                temp = new Line(mLines.get(i));
                                            }

                                            // closed path found?
                                            if(temp.startId == dot.id()){
                                                lines.add(temp);
                                                break;
                                            }
                                        }

                                        Log.i(TAG, "*********Lines***********");
                                        for(Line l: lines){
                                            Log.i(TAG, String.format("(%03d, %03d)",
                                                    l.startId, l.endId) + " " + l.type());
                                        }
                                        if(lines.size() >= 3 && mIsDrawing && lines.get(lines.size() - 1).startId == dot.id()){
                                            mAPathClosed = true;
                                            mLines.push(line);
                                            mIsDrawing = false;
                                            Utils.vibrate(getContext().getApplicationContext());
                                        }else{
                                            Log.i(TAG, "Not a Closed Path: " + lines.size());
                                        }
                                        Log.i(TAG, "*****************************");

                                    }
                                }
                            }
                            // break if (mx, my) is in rect
                            break;
                        }
                    }
                }
                mState = STATE.MOVING;
                break;
            case MotionEvent.ACTION_UP:
                if(mStartDot != null){
                    mState = STATE.ACTION_UP;
                }else{
                    mState = STATE.RESET;
                }
                break;
        }
    }


    private synchronized void onActionUp(){

        // If no two dots have been connected, return
        if(mSelectedDots.size() <= 1 || mLines.empty()){
            mState = STATE.RESET;
            return;
        }

        // Update score info
        mGameState.update(mStartDot.dotColor(), mSelectedDots, mAPathClosed);

        Canvas canvas = mSurfaceHolder.lockCanvas(null);

        // Map column number to the the number of selected dots in the column
        SparseIntArray dotsPerColumn = new SparseIntArray(mSelectedDots.size());

        //////////////////////////////////////
        //
        // STEP 1: CLEAR "SELECTED" DOTS
        //
        ///////////////////////////////////////

        // Redraw Dots
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);
        for(List<Dot> dotRow: mDotRows){
            for(Dot dot: dotRow){
                // only draw unselected dots
                if(!dot.isSelected()){
                    mColorDotPaint.setColor(dot.color());
                    canvas.drawCircle(dot.centreX(), dot.centreY(), RADIUS, mColorDotPaint);
                }else{
                    dotsPerColumn.put(dot.column(), 1 + dotsPerColumn.get(dot.column(), 0));
                }
            }
        }
        drawHeaderAndFooter(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);


        Log.i(TAG, "\n------------PHASE 1----------------\n");
        printDots();
        Log.i(TAG,"SELECTED DOTS: { " + mStartDot.dotColor().toString() + ", " + mSelectedDots.size() + " }");
        try {
            Thread.sleep(TIME_DELAY*8);
        }catch (InterruptedException e){
            e.printStackTrace();
        }


        /////////////////////////////////////////////
        //
        // STEP 2: ANIMATE "UNSELECTED" DOTS FALLING
        //
        ////////////////////////////////////////////

        boolean done = false;
        while(!done){
            done = true;

            // Bubble "selected" dots upwards
            for(int i = 0; i < dotsPerColumn.size(); i++){
                int column = dotsPerColumn.keyAt(i);
                for(int row = MAX_ROW_COUNT - 1; row > 0; row--){
                    // if the above dot is unselected and the one below in selected
                    if(mDotRows.get(row).get(column).isSelected() && !mDotRows.get(row - 1).get(column).isSelected()){
                        Dot.swap(mDotRows.get(row).get(column),  mDotRows.get(row - 1).get(column));
                        mDotRows.get(row).get(column).deSelect();
                        mDotRows.get(row - 1).get(column).select();
                        done = false;
                    }
                }
            }

            // Update UI
            if(!done){
                canvas = mSurfaceHolder.lockCanvas(null);
                canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

                for(List<Dot> dotRow: mDotRows){
                    for(Dot dot: dotRow){
                        // only draw unselected dots
                        if(!dot.isSelected()){
                            mColorDotPaint.setColor(dot.color());
                            canvas.drawCircle(dot.centreX(), dot.centreY(), RADIUS, mColorDotPaint);
                        }
                    }
                }
                // Redraw Header and Footer
                drawHeaderAndFooter(canvas);
                mSurfaceHolder.unlockCanvasAndPost(canvas);

                try {
                    Thread.sleep(TIME_DELAY);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.i(TAG,"\n------------PHASE 2 ----------------\n");
        printDots();
        try {
            Thread.sleep(TIME_DELAY*2);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }


        /////////////////////////
        //
        // ADD NEW DOTS
        //
        /////////////////////////

        done = false;
        while(!done){
            done = true;
            // Add new dots
            // ... bubble them downwards
            for(int i = 0; i < dotsPerColumn.size(); i++){
                int column = dotsPerColumn.keyAt(i);
                int count = dotsPerColumn.get(column, 0);

                if (count > 0){
                    int row = 0;
                    // find the next selected dot
                    while(row < MAX_ROW_COUNT && !mDotRows.get(row).get(column).isSelected()){
                        row++;
                    }

                    if(row < MAX_ROW_COUNT){
                        // bubble upwards, "selected" dot found at row, "row"
                        for(int j = row; j > 0; j--){
                            Dot.swap(mDotRows.get(j).get(column),  mDotRows.get(j - 1).get(column));
                            mDotRows.get(j).get(column).deSelect();
                        }
                        dotsPerColumn.put(column, --count);

                        // Replace dotNode of "selected" dot
                        Dot dot = mDotRows.get(0).get(column);
                        dot.setDotNode(getNewNode());
                        dot.deSelect();

                        done = false;
                    }
                }
            }


            // Update UI
            if(!done){
                canvas = mSurfaceHolder.lockCanvas(null);
                canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

                for(List<Dot> dotRow: mDotRows){
                    for(Dot dot: dotRow){
                        if(!dot.isSelected()){
                            mColorDotPaint.setColor(dot.color());
                            canvas.drawCircle(dot.centreX(), dot.centreY(), dot.radius(), mColorDotPaint);
                        }
                    }
                }
                drawHeaderAndFooter(canvas);
                mSurfaceHolder.unlockCanvasAndPost(canvas);

                try {
                    Thread.sleep(TIME_DELAY);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        Log.i(TAG, "\n------------PHASE 3 ----------------\n");
        printDots();
        mState = STATE.RESET;
    }


    private synchronized void onActionMove(){
        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        //==========================================================
        // DRAW & DECORATE DOTS
        //==========================================================
        if(mStartDot != null && mStartingNodes != null){
            for(List<Dot> list: mDotRows){
                for(Dot dot: list){
                    if(dot.id() != mStartDot.id()){
                        if(mDataLoader.checkEdge(mStartDot.id(), dot.id(), mEdges)){
                            canvas.drawCircle(dot.centreX(),  dot.centreY(), dot.radius(), mColorDotPaint);
                        }else{
                            canvas.drawCircle(dot.centreX(),  dot.centreY(), dot.radius(), mBlackDotPaint);
                        }
                    } else {
                        canvas.drawCircle( dot.centreX(),  dot.centreY(), dot.radius(), mColorDotPaint);
                    }
                }
            }

        }

        //==========================================================
        // DRAW LINE or PATH
        //==========================================================
        float dx = Math.abs(mx - mStartX);
        float dy = Math.abs(my - mStartY);

        // draw line connections
        synchronized (mLines){
            for(Line line: mLines){
                canvas.drawLine(line.startX, line.startY, line.endX, line.endY, mPathPaint);
            }
        }

        if(mIsDrawing){
            if (mStartDot != null && (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) ){
                canvas.drawLine(mStartX, mStartY, mx, my, mPathPaint);
                last_mx = mx;
                last_my = my;
            }
        }

        drawHeaderAndFooter(canvas);

        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }


    private synchronized void onActionReset(){

        //===========================
        // RESET
        //===========================

        // DESELECT DOTS
        for(int i = MAX_ROW_COUNT - 1; i >= 0; i--){
            List<Dot> list = mDotRows.get(i);
            for(Dot dot: list){
                dot.deSelect();
            }
        }

        // DEBUGGING
        Log.i(TAG, "__________________RESET_______________");
        Log.i(TAG,"SELECTED DOTS: " + mSelectedDots.size());
        printDots();

        mLines.clear();
        mAPathClosed = false;
        mSelectedDots.clear();
        mStartDot =  null;
        mState = STATE.DO_NOTHING;

        //---------------------------------

        Log.i(TAG, "Buffered: " + mBufferSize + "; Recycled: " + (mNodes.size() - mBufferSize));

        //========================
        // REDRAW UI
        //========================
        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        drawHeaderAndFooter(canvas);

        for(List<Dot> dotRow: mDotRows){
            for(Dot dot: dotRow){
                mColorDotPaint.setColor(dot.color());
                canvas.drawCircle(dot.centreX(), dot.centreY(), dot.radius(), mColorDotPaint);
            }
        }
        mSurfaceHolder.unlockCanvasAndPost(canvas);

        if(mGameListener != null){
            boolean isOver = false;
            if(mGameState.succeeded()){
                isOver = true;
            } else if(mGameState.failed()){
                isOver = true;
            }

            sleep(8);

            if (isOver){
                mGameListener.gameOver(mGameState);
            }
        }

    }

    private void sleep(int millis){
        try{
            Thread.sleep(TIME_DELAY*millis);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void preinit(){
        if(mGameListener != null){
            if(!mObjectiveShown){
                init();

                Canvas canvas;
                canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.parseColor("#F5F5F5"));
                drawHeaderAndFooter(canvas);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                mGameListener.displayObjective();
                mObjectiveShown = true;
            }
        }
    }

    private void init(){
        DIMENSION = (2*RADIUS*MAX_COLUMN_COUNT) + HORIZONTAL_SPACING*(MAX_COLUMN_COUNT - 1);
        int centerX = (getMeasuredWidth() - DIMENSION)/2 + RADIUS;
        int centerY = (getMeasuredHeight() - DIMENSION)/2 + RADIUS;

        for(int i = 0; i < MAX_ROW_COUNT; i++){
            List<Dot> dotRow = new ArrayList<>();
            for(int j = 0; j < MAX_COLUMN_COUNT; j++){
                Dot dot = new Dot(centerX, centerY, RADIUS);
                dot.setColumn(j);
                dot.setRow(i);
                dot.setTolerance(DOT_TOLERANCE);
                dotRow.add(dot);
                centerX += 2*RADIUS + HORIZONTAL_SPACING;
            }
            centerX = (getMeasuredWidth() - DIMENSION)/2 + RADIUS;
            centerY += 2*RADIUS + VERTICAL_SPACING;

            mDotRows.add(dotRow);
        }

        headerRect = new Rect(0, 0, getMeasuredWidth(), HEADER_FOOTER_HEIGHT);
        footerRect = new Rect(0, getMeasuredHeight() - HEADER_FOOTER_HEIGHT, getMeasuredWidth(), getMeasuredHeight());
        scoreRect = new Rect(0, 0, SCORE_TEXT_DIM, SCORE_TEXT_DIM);
        movesRect = new Rect(getMeasuredWidth() - SCORE_TEXT_DIM, 0, getMeasuredWidth(), SCORE_TEXT_DIM);
        labelRect = new Rect(scoreRect.right + 10, headerRect.bottom + 10, movesRect.left - 10,
                headerRect.bottom + 10 + SCORE_TEXT_DIM/2);
    }


    private void loadGame(){
        Canvas canvas;
        boolean doneDrawing = false;
        int curEmptyRow = 0;
        int dataIdx = 0;
//        float endY = loadRow(currentRow);
//        float startY = ANIM_START_POS;     // current height of the rows of dots
//        float stepSize = (endY - startY)/2;  // change in Y

        while (!doneDrawing){
            for(int row  = curEmptyRow; row >  0; row--){
                for(int col = 0; col < MAX_COLUMN_COUNT; col++){
                    Dot.swap(mDotRows.get(row).get(col), mDotRows.get(row - 1).get(col));
                }
            }

            for(int col = 0; col < MAX_COLUMN_COUNT; col++){
                mDotRows.get(0).get(col).setDotNode(mStartingNodes.get(dataIdx++));
            }

            if(curEmptyRow >= (MAX_ROW_COUNT - 1)){
                doneDrawing = true;
            }

            // Update UI
            canvas = mSurfaceHolder.lockCanvas(null);
            canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);
            drawHeaderAndFooter(canvas);
            for(int row  = curEmptyRow; row >=  0; row--){
                for(int col = 0; col < MAX_COLUMN_COUNT; col++){
                    Dot dot = mDotRows.get(row).get(col);
                    mColorDotPaint.setColor(dot.color());
                    canvas.drawCircle(dot.centreX(), dot.centreY(), dot.radius(), mColorDotPaint);
                }
            }
            mSurfaceHolder.unlockCanvasAndPost(canvas);

            try {
                Thread.sleep(TIME_DELAY*4);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            curEmptyRow++;
        }

        mReady = true;
    }


    private DotNode getNewNode(){
        DotNode node = null;

        while(true){

            if(mBufferSize > 0){
                node = mNodes.get(mBufferSize - 1);
                mBufferSize--;
            }else{
                // If all nodes in the buffer container have been exhausted, ...
                // ... recycle the old deleted nodes.
                // NOTE: Nodes are deleted logically
                Log.i(TAG, "Recycled");
                mBufferSize = mNodes.size();
            }

            if(node != null && mGameState.isNodeValid(node.degree)){
                break;
            }
        }
        return node;
    }


    private void drawHeaderAndFooter(Canvas canvas){

        //////////////////////////////////////////////
        //                                          //
        // DRAW HEADER                              //
        //                                          //
        //////////////////////////////////////////////
        mHeaderFooterPaint.setColor(HEADER_FOOTER_COLOR);
        canvas.drawRect(headerRect, mHeaderFooterPaint);

//        canvas.drawRect(labelRect, mHeaderFooterPaint);
        if(mStartDot != null){
            mTextPaint.setColor(Color.parseColor("#8E5DAA"));
            mTextPaint.setTextSize(60);
            float y = labelRect.centerY() + mTextPaint.descent();
            canvas.drawText(mStartDot.toString(), labelRect.centerX(), y, mTextPaint);
        }

        int numOfDots = 1;
        EnumMap<DotColor, Integer> objective = null;
        EnumMap<DotColor, int[]> score = null;
        if(mGameLevel != null){
            objective = mGameLevel.getObjective();
            //score = mGameLevel.getScore();
            score = mGameState.progress();
            numOfDots = objective.size();
        }

        float startX = getMeasuredWidth() -
                (numOfDots* RADIUS_SMALL *2 + HEADER_DOTS_SPACING *(numOfDots -1));
        float startY = (HEADER_FOOTER_HEIGHT - (RADIUS_SMALL*2 + 100 - 20 ));

        startX /= 2;
        startX += RADIUS_SMALL;

        mTextPaint.setTextSize(TEXT_SIZE_MEDIUM);
        mTextPaint.setColor(TEXT_COLOR);

        // draw objective dots
        if(objective != null){
            for(DotColor dotColor: objective.keySet()){
                mColorDotPaint.setColor(dotColor.colorInfo());
                canvas.drawCircle(startX, startY, RADIUS_SMALL, mColorDotPaint);

                if(!mGameState.isCollected(dotColor)){
                    String string = score.get(dotColor)[State.IDX_DOTS_COLLECTED] + " / " + score.get(dotColor)[State.IDX_DOTS_GOAL] ;
                    canvas.drawText(string, startX, startY + 100, mTextPaint);
                }else{
                    // draw tick mark
                    float posY = startY + RADIUS_SMALL*0.4f;
                    float x = RADIUS_SMALL*(float)Math.cos(Math.PI/3);
                    float y = RADIUS_SMALL*(float)Math.sin(Math.PI/3);
                    canvas.drawLine(startX, posY, startX + x - 5, posY  - y, mTickMarkPaint);
                    x = .5f*RADIUS_SMALL*(float)Math.cos(Math.PI/6);
                    y = .5f*RADIUS_SMALL*(float)Math.sin(Math.PI/6);
                    canvas.drawLine(startX, posY, startX - x, posY  - y, mTickMarkPaint);
                }
                startX += RADIUS_SMALL *2 + HEADER_DOTS_SPACING;
            }
        }

        // Header Corners
        mHeaderFooterPaint.setColor(SCORE_COLOR);
        canvas.drawRect(scoreRect, mHeaderFooterPaint);
        canvas.drawRect(movesRect, mHeaderFooterPaint);


        // draw Score
        mTextPaint.setColor(TEXT_MOVES_COLOR);
        mTextPaint.setTextSize(TEXT_SIZE_LARGE);
        canvas.drawText(String.valueOf(mGameState.score()), scoreRect.centerX(), scoreRect.centerY() + TEXT_SIZE_LARGE/3, mTextPaint);
        mTextPaint.setColor(Color.LTGRAY);
        mTextPaint.setTextSize(TEXT_SIZE_SMALL);
        canvas.drawText("SCORE", scoreRect.centerX(), scoreRect.centerY()+ TEXT_SIZE_LARGE/3 + 60, mTextPaint);


        // draw Moves
        mTextPaint.setColor(TEXT_MOVES_COLOR);
        mTextPaint.setTextSize(TEXT_SIZE_LARGE);
        canvas.drawText(String.valueOf(mGameState.movesLeft()), movesRect.centerX(), movesRect.centerY() + TEXT_SIZE_LARGE/3, mTextPaint);
        mTextPaint.setColor(Color.LTGRAY);
        mTextPaint.setTextSize(TEXT_SIZE_SMALL);
        canvas.drawText("MOVES", movesRect.centerX(), movesRect.centerY()+ TEXT_SIZE_LARGE/3 + 60, mTextPaint);



        //////////////////////////////////////////////
        //                                          //
        // DRAW FOOTER                              //
        //                                          //
        //////////////////////////////////////////////
        mHeaderFooterPaint.setColor(HEADER_FOOTER_COLOR);
        canvas.drawRect(footerRect, mHeaderFooterPaint);

        final int width = 900;
        final int radius = RADIUS_SMALL;
        int spacing = (width - 3*2*radius)/2;
        int posX = (getMeasuredWidth() - width)/2 + radius;
        int posY = getMeasuredHeight() - HEADER_FOOTER_HEIGHT/2;

        // Draw Ability #1
        // Swap red dot.
        mColorDotPaint.setColor(Color.parseColor("#FF4081"));
        canvas.drawCircle(posX, posY, radius, mColorDotPaint);
        canvas.drawLine(posX - .6f*radius, posY, posX + .6f*radius, posY, mTickMarkPaint);
        canvas.drawLine(posX , posY- .6f*radius, posX , posY+ .6f*radius, mTickMarkPaint);

        // Draw Ability #2
        // Add additional number of moves
        posX += spacing + 2*radius;
        mColorDotPaint.setColor(Color.parseColor("#ffb6e9b5"));
        canvas.drawCircle(posX, posY, radius, mColorDotPaint);
        canvas.drawLine(posX, posY, posX, posY - radius*.7f, mTickMarkPaint);
        float offsetX = .75f*radius*(float)Math.cos(Math.PI/4);
        float offsetY = .75f*radius*(float)Math.sin(Math.PI/4);
        canvas.drawLine(posX, posY, posX + offsetX, posY + offsetY, mTickMarkPaint);

        // Draw Ability #3
        posX += spacing + 2*radius;
        mColorDotPaint.setColor(Color.LTGRAY);
        canvas.drawCircle(posX, posY, radius, mColorDotPaint);


        if(mStartDot != null){
            mColorDotPaint.setColor(mStartDot.dotColor().colorInfo());
        }
    }


    public void setGameLevel(int id){
        if(mGameLevel == null){

            LevelDatabaseHelper helper = null;

            try {
                helper = LevelDatabaseHelper.getInstance(getContext());
            } catch (Exception e) {
                throw new Error("Unable to create database");
            }
            finally {
                try {
                    if(helper != null){
                        mGameLevel = helper.getLevel(id);
                        mGameState = new State(mGameLevel);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    public void resume(){
        if(!mRunning){
            Log.i(TAG, "#0.  THREAD STARTED");
            mRunning = true;
            mGameThread = new Thread(this);
            mGameThread.start();

            // If Surface was created but not destroyed.
            // This happens when the device goes to sleep or the power button
            // is pressed.
            if(mSurfaceCreated && !mSurfaceWasDestroyed){
                mSurfaceReady = true;
            }

            mState = STATE.RESET;
        }
    }


    public void pause(){
        try {
            if(mRunning){
                mRunning = false;
                mSurfaceReady = false;
                mGameThread.join();
                Log.i(TAG, "PAUSED");
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }


    public void close(){
        pause();
        Log.i(TAG, "CLOSED");
        mDataLoader.close();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Debugging
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void printDots(){
        Log.i(TAG, "---------------------------------------");
        for(List<Dot> dotRow: mDotRows){
            StringBuilder s = new StringBuilder();
            for(Dot dot: dotRow){
                s.append(colorToString(dot.dotColor().colorInfo()));
                if(dot.isSelected()) {
                    s.append("* ");
                }else{
                   s.append("  ");
                }
            }
           Log.i(TAG, s.toString());
        }
        Log.i(TAG, "--------------------------------------");
    }

    private String colorToString(int color){
        if (color == getContext().getColor(R.color.dot_violet))
            return "PUR";
        else if (color == getContext().getColor(R.color.dot_indigo))
            return "IND";
        else if (color == getContext().getColor(R.color.dot_blue))
            return "BLU";
        else if (color == getContext().getColor(R.color.dot_green))
            return "GRE";
        else if (color == getContext().getColor(R.color.dot_yellow))
            return "YEL";
        else if (color == getContext().getColor(R.color.dot_orange))
            return "ORA";
        else if (color == getContext().getColor(R.color.dot_red))
            return "RED";

        return "UNK";
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public interface GameListener {
        void gameOver(State state);
        void displayObjective();
        boolean isObjectiveVisible();
    }

    public void setGameOverListener(GameListener listener){
        mGameListener = listener;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Interfaces Implemented
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "#1.  SURFACE CREATED");
        mSurfaceReady = true;
        mSurfaceCreated = true;
        mSurfaceWasDestroyed = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "SURFACE DESTROYED");
        mSurfaceWasDestroyed = true;
        if(mRunning) pause();
    }

    @Override
    public void run() {
        //===============================
        // INITIALIZE GAME VIEW
        //===============================
        while (!mSurfaceReady){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            Log.i(TAG, "Surface Not Ready.");
        }
        Log.i(TAG, "#2.  SURFACE READY");

        // display objective if necessary
        if(!mObjectiveShown){
            preinit();
            return;
        }


        if(!mDoneLoadingData){
            mDataLoader.setGraphNumber(mGameLevel.id());
            mDataLoader.loadNodes();
        }
        while(!mDoneLoadingData){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            Log.i(TAG, "Waiting for Data to Load.");
        }
        Log.i(TAG, "#3.  DONE LOADING DATA");

        if(!mReady){
            loadGame();
        }

        while(!mReady){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            Log.i(TAG, "Game not ready.");
        }

        Log.i(TAG, "GAME READY");
        //================================
        // GAME LOOP
        //================================
        while (mRunning){
            try{
                Thread.sleep(3);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            switch (mState){
                case ACTION_DOWN:
                    break;
                case ACTION_UP:
                    onActionUp();
                    break;
                case MOVING:
                    onActionMove();
                    break;
                case RESET:
                    onActionReset();
                    break;
                case DO_NOTHING:
                    break;
            }
        }
        Log.i(TAG, "STOPPED RUNNING");
    }


    @Override
    public boolean isNodeValid(int degree) {
        return mGameState.isNodeValid(degree);
    }

    @Override
    public void onLoad(ArrayList<DotNode> nodes) {
        mStartingNodes = nodes;
        mDoneLoadingData = true;
    }

    @Override
    public void onLoadBuffer(ArrayList<DotNode> nodes) {
        mNodes = nodes;
        mBufferSize = mNodes.size();
        Log.i(TAG, "BUFFER SIZE " + mNodes.size());
    }

}