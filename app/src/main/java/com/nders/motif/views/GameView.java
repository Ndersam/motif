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
import com.nders.motif.data.LevelDatabaseHelper;
import com.nders.motif.entities.Circle;
import com.nders.motif.entities.DotColor;
import com.nders.motif.entities.DotNode;
import com.nders.motif.entities.Line;
import com.nders.motif.entities.Rectangle;
import com.nders.motif.data.Loader;
import com.nders.motif.game.Level;
import com.nders.motif.game.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;



public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback, Loader.LoaderListener{

    // for logging debug information
    private static final String TAG = GameView.class.getSimpleName();


    /**
     **   LAYOUT
     */

    // Horizontal spacing between dots
    private static final int HORIZONTAL_SPACING = 100;

    // Vertical spacing between dots
    private static final int VERTICAL_SPACING = 100;

    // The number of dots in a row.
    private static final int MAX_ROW_COUNT = 6;

    // The number of dots in a column.
    private static final int MAX_COLUMN_COUNT = 6;

    // Height and width of the area allocated for playing the dots.
    // This area is centered within the view.
    private static final int DIMENSION = 1000;

    // Radius of main dots - dots centred in the view.
    protected static final int RADIUS = 50;

    // Radius of smaller dots drawn in the header
    protected static final int RADIUS_SMALL = 40;

    // Spacing between the smaller dots drawn in the header.
    protected static final float HEADER_DOTS_SPACING = 90;

    // The height setting of the header and the footer.
    protected static final int HEADER_FOOTER_HEIGHT = 250;

    // Padding used in "padding" the contents of the header and the footer.
    protected static final float VERTICAL_PADDING = 16;

    protected static final int SCORE_TEXT_DIM = 300;


    protected static int LINE_THRESHOLD;


    /**
     **   DRAWING
     */

    // Partitions
    protected Rect headerRect;
    protected Rect footerRect;
    protected Rect scoreRect;
    protected Rect movesRect;

    // Colors
    protected int BACKGROUND_COLOR  = Color.WHITE;
    protected int HEADER_FOOTER_COLOR = Color.parseColor("#FFF6D5");
    protected int SCORE_COLOR =  HEADER_FOOTER_COLOR;
    protected int TEXT_COLOR = Color.parseColor("#202020");
    protected int TEXT_MOVES_COLOR = Color.parseColor("#717171");//Color.LTGRAY; //Color.parseColor("#FFC107");
    // Paints
    protected Paint mTextPaint;
    protected Paint mBlackPathPaint;
    protected Paint mWhitePaint;
    protected Paint mWhitePathPaint;
    protected Paint mBlackPaint;
    protected Paint mColorPaint;
    protected Paint mColorPathPaint;
    protected Paint mBackgroundPathPaint;
    protected Paint mBackgroundPaint;
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
    protected static final float TEXT_PADDING = 100;

    protected static final float TOUCH_STROKE_WIDTH = 40;
    protected static final float TOUCH_TOLERANCE = 2;
    protected static final float RECT_TOLERANCE = 4;





    /**
     **  CONTAINERS
     */

    // Keeps track of the stationary dots drawn on screen.
    protected Stack<List<Rectangle>> mRectangles;

    // Temporary variables for initializing the game
    protected List<Circle> mCircles = new ArrayList<>();
    protected List<Circle> mBufferCircles = new ArrayList<>();

    // Keeps track of all the lines drawn
    protected Stack<Line> mLines;

    // Stores the first selected dot
    protected Rectangle mStartRect = null;
    private DotColor mSelectedDotColor = null;

    // Stores all the dots that have been connected
    // The contents of this container get swapped with other dots ...
    // ... that appear above them and have not been selected (connected).
    // This occurs if and only if the size of the container is greater than one.
    protected Stack<Rectangle> mSelectedDots;


    /**
     **  DATA
     */

    // Stores the relationship between dots.
    // A pair of dots (represented as an ArrayList) is the key; while the "existence" of an ...
    // ... edge (boolean) is the value.
    protected HashMap<ArrayList, Boolean> mEdges = new HashMap<>();

    // The number of dots displayed on the screen.
    // This corresponds to the size of a game.
    protected static final int DOT_COUNT = MAX_COLUMN_COUNT* MAX_ROW_COUNT;

    // List of objects containing the graph nodes loaded from the database.
    // These are nodes displayed on the screen as colored dots.
    // There are "DOT_COUNT" number of graph nodes.
    protected List<DotNode> mGraphNodes;

    // List of remaining nodes loaded from the database.
    // The content of this container are gradually removed and used to replace ...
    // .. selected dots in "mGraphNodes".
    protected List<DotNode> mBufferNodes = new ArrayList<>();

    // List of deleted nodes.
    // Nodes are deleted when their corresponding dots have been selected and ...
    // ... removed.
    protected List<DotNode> mDeletedNodes = new ArrayList<>();

    // Object delegated with the responsibility of querying and loading data ...
    // ... from the database.
    protected Loader mDataLoader;

    /**
     **  ANIMATION
     */

    static final int ANIM_START_POS = HEADER_FOOTER_HEIGHT * 2;
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

    // Is true when the game starts and all the initial set of dots have been drawn.
    protected boolean mInitialized = false;

    // Is true when the associated activity is not paused and is in focus.
    protected boolean mRunning = false;

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

    // Keeps track of frequencies of the "dotColors" loaded.
    // This is used for ensuring the an optimum number of particular colors are present.
    // This varies with the game level.
    protected EnumMap<DotColor, Integer> mDotColorCounter = new EnumMap<>(DotColor.class);

    protected boolean mIsDrawing = false;

    // The current game state
    protected State mGameState = null;

    protected boolean mIsRectFormed = false;


    /**
     **   MISC
     */

    // Reference to the SurfaceView's surface holder.
    protected SurfaceHolder mSurfaceHolder;

    // Callback interface for handling "game complete" and "game over" events.
    protected GameOverListener mGameOverListener = null;

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
     * @param context
     */
    private GameView(Context context) {
        this(context, null);
    }

    /**
     * Constructor is private to prevent direct instantiation.
     * Make a call to the static method "getInstance" instead.
     * @param context
     * @param attrs
     */
    private GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Constants
        TIME_DELAY = (int) (1000/((Activity)context).getWindowManager()
                .getDefaultDisplay().getRefreshRate());



        // Paints

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBackgroundPaint.setColor(BACKGROUND_COLOR);
        mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mBackgroundPathPaint = new Paint();
        mBackgroundPathPaint.setAntiAlias(true);
        mBackgroundPathPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPathPaint.setStrokeWidth(TOUCH_STROKE_WIDTH + 4);
        mBackgroundPathPaint.setDither(true);
        mBackgroundPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mBackgroundPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mBackgroundPathPaint.setColor(BACKGROUND_COLOR);
        mBackgroundPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));


        mBlackPathPaint = new Paint();
        mBlackPathPaint.setAntiAlias(true);
        mBlackPathPaint.setDither(true);
        mBlackPathPaint.setStyle(Paint.Style.STROKE);
        mBlackPathPaint.setStrokeWidth(TOUCH_STROKE_WIDTH);
        mBlackPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mBlackPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mBlackPathPaint.setColor(getContext().getColor(R.color.black));
        mBlackPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mBlackPaint = new Paint();
        mBlackPaint.setAntiAlias(true);
        mBlackPaint.setDither(true);
        mBlackPaint.setStyle(Paint.Style.FILL);
        mBlackPaint.setColor(getContext().getColor(R.color.black));
        mBlackPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mWhitePaint = new Paint();
        mWhitePaint.setAntiAlias(true);
        mWhitePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mWhitePaint.setColor(getContext().getColor(R.color.white));
        mWhitePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mWhitePathPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mWhitePathPaint.setStyle(Paint.Style.STROKE);
        mWhitePathPaint.setStrokeWidth(8);
        mWhitePathPaint.setStrokeJoin(Paint.Join.ROUND);
        mWhitePathPaint.setStrokeCap(Paint.Cap.ROUND);
        mWhitePathPaint.setColor(Color.WHITE);
        mWhitePathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mColorPaint.setStyle(Paint.Style.FILL);
        mColorPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mColorPathPaint = new Paint();
        mColorPathPaint.setStyle(Paint.Style.FILL);
        mColorPathPaint.setDither(true);
        mColorPathPaint.setAntiAlias(true);
        mColorPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mColorPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mColorPathPaint.setStrokeWidth(TOUCH_STROKE_WIDTH);
        mColorPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setStrokeWidth(TOUCH_STROKE_WIDTH - 2);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mHeaderFooterPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mHeaderFooterPaint.setStyle(Paint.Style.FILL);
        mHeaderFooterPaint.setColor(HEADER_FOOTER_COLOR);

        // Keeping track of dots and User drawings
        mRectangles = new Stack<>();
        mCircles = new ArrayList<>();
        mLines = new Stack<>();
        mSelectedDots = new Stack<>();


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
        }else {
            resume();
        }
        mState = STATE.RESET;
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // hack to cancel multitouch events
        if(event.getPointerCount() > 1) return false;
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
                for(List<Rectangle> list: mRectangles){
                    for(Rectangle rect: list){
                        if(rect.contains(mStartX, mStartY)){
                            mStartX = rect.getX();
                            mStartY = rect.getY();
                            mStartRect = rect;
                            mColorPaint.setColor(mStartRect.dotColor().colorInfo());
                            mColorPathPaint.setColor(mStartRect.dotColor().colorInfo());
                            mStartRect.select();
                            mSelectedDotColor = mStartRect.dotColor();
                            mSelectedDots.push(mStartRect);
                            break;
                        }
                    }
                }
                if(mStartRect != null){
                    mState = STATE.ACTION_DOWN;
                }else{
                    mState = STATE.DO_NOTHING;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mStartRect == null){
                    break;
                }
                if(!mIsDrawing){
                    mState = STATE.MOVING;
                    break;
                }

                for(List<Rectangle> list: mRectangles){
                    for(Rectangle rect: list){
                        //==============================================================
                        // IF "rect" CONTAINS THE COORDINATE, {mx, my},...
                        // A LINE COULD BE DRAWN
                        //==============================================================
                        if(rect.contains(mx, my)){
                            float mx_raw = mx; // save raw current x value
                            float my_raw = my; // save raw current y value
                            mx = rect.getX();
                            my = rect.getY();

                            // rect.id() != mStartRect.id() &&
                            if(!rect.isSelected()){
                                if(mDataLoader.checkEdge(mStartRect.id(), rect.id(), mEdges)) {
                                    //=========================================================
                                    // IF LINE TO BE DRAWN IS NON-DIAGONAL
                                    //=========================================================
                                    if (mx == mStartX || my == mStartY) {
                                        //Log.i(TAG, "TOUCHED: " + rect.mId());
                                        // new line to be added
                                        Line line = new Line(mStartX, mStartY, mx, my);
                                        line.startId = mSelectedDots.peek().id();
                                        line.endId = rect.id();

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
                                            if (!rect.isSelected()) {
                                                mLines.push(line);
                                                mStartX = mx;
                                                mStartY = my;

                                                // push rectangle onto stack
                                                rect.select();
                                                mSelectedDots.push(rect);

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
                                if(mSelectedDots.peek().id() == rect.id()){
                                    break;
                                }

                                // if line is non-diagonal
                                if (mx == mStartX || my == mStartY) {

                                    // new line to be added
                                    Line line = new Line(mStartX, mStartY, mx, my);
                                    line.startId = mSelectedDots.peek().id();
                                    line.endId = rect.id();

                                    // To close a rectangular path 4 lines are needed.
                                    // If mLines has at least 3 lines, close path
                                    if(mLines.size() >= 3 && (line.length() <= (VERTICAL_SPACING + 2 * RADIUS))){
                                        Log.i(TAG, "Line Valid");

                                        List<Line> lines = new ArrayList<>();

                                        int i = mLines.size() - 1;
                                        Line temp = new Line(mLines.get(i));

                                        Log.i(TAG, String.format("(%03d, %03d)",
                                                mLines.get(i).startId, mLines.get(i).endId) + " " + mLines.get(i).type());

                                        for(i = mLines.size() - 2; i >= 0; i--){

                                            Log.i(TAG, String.format("(%03d, %03d)",
                                                    mLines.get(i).startId, mLines.get(i).endId) + " " + mLines.get(i).type());

                                            if(temp.type() == mLines.get(i).type()){
                                                temp.merge(mLines.get(i));
                                            }else{
                                                lines.add(temp);
                                                temp = new Line(mLines.get(i));
                                            }

                                            // closed path found
                                            if(lines.size() >= 3)
                                                break;
                                        }

                                        if(i < 0){
                                            lines.add(temp);
                                        }

                                        Log.i(TAG, "*********Lines***********");
                                        for(Line l: lines){
                                            Log.i(TAG, String.format("(%03d, %03d)",
                                                    l.startId, l.endId) + " " + l.type());
                                        }
                                        if(lines.size() >= 3 && mIsDrawing){
                                            mIsRectFormed = true;
                                            mLines.push(line);
                                            mIsDrawing = false;
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
                if(mStartRect != null){
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
        mGameState.update(mSelectedDotColor, mSelectedDots, mIsRectFormed);

        Canvas canvas = mSurfaceHolder.lockCanvas(null);

        // Stores in a map the number of selected dots per column
        SparseIntArray dotsPerColumn = new SparseIntArray(mSelectedDots.size());



        //////////////////////////////////////
        //
        // STEP 1: CLEAR "SELECTED" DOTS
        //
        ///////////////////////////////////////

        // Redraw Rectangles
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);
        for(List<Rectangle> rectRow: mRectangles){
            for(Rectangle rect: rectRow){
                // only draw unselected dots
                if(!rect.isSelected()){
                    mColorPaint.setColor(rect.dotColor().colorInfo());
                    canvas.drawCircle(rect.getX(), rect.getY(), RADIUS, mColorPaint);
                }
                else{
                    mDeletedNodes.add(rect.toDotNode());
                    dotsPerColumn.put(rect.column(), 1 + dotsPerColumn.get(rect.column(), 0));
                }
            }
        }

        // Redraw Header and Footer
        drawHeaderAndFooter(canvas);

        // Post new drawing
        mSurfaceHolder.unlockCanvasAndPost(canvas);


        System.out.print("\n------------PHASE 1----------------\n");
        printRects();
        Log.i(TAG,"SELECTED DOTS: { " + mStartRect.dotColor().toString() + ", " + mSelectedDots.size() + " }");
        try {
            Thread.sleep(TIME_DELAY);
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
                for(int row = 1; row < MAX_ROW_COUNT; row++){
                    // if the above dot is unselected and the one below in selected
                    if( !mRectangles.get(row).get(column).isSelected() && mRectangles.get(row - 1).get(column).isSelected()){
                        Rectangle.swap(mRectangles.get(row).get(column),  mRectangles.get(row - 1).get(column));
                        done = false;
                    }
                }
            }

            // Update UI
            if(!done){
                canvas = mSurfaceHolder.lockCanvas(null);
                canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

                for(List<Rectangle> rectRow: mRectangles){
                    for(Rectangle rect: rectRow){
                        // only draw unselected dots
                        if(!rect.isSelected()){
                            mColorPaint.setColor(rect.dotColor().colorInfo());
                            canvas.drawCircle(rect.getX(), rect.getY(), RADIUS, mColorPaint);
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

        System.out.print("\n------------PHASE 2 ----------------\n");
        printRects();
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
                    int row = MAX_ROW_COUNT - 1;
                    // find the next selected dot
                    while(row >= 0 && !mRectangles.get(row).get(column).isSelected()){
                        row--;
                    }

                    if(row >= 0){

                        // bubble upwards, "selected" dot found at row, "row"
                        for(int j = row; j < MAX_COLUMN_COUNT - 1; j++){
                            Rectangle.swap(mRectangles.get(j).get(column),  mRectangles.get(j + 1).get(column));
                        }
                        dotsPerColumn.put(column, count--);

                        // add new dot
                        Rectangle rect = mRectangles.get(MAX_ROW_COUNT - 1).get(column);
                        //mDeletedNodes.add(rect.toDotNode());
                        DotNode node = getNewNode();
                        rect.deselect();
                        rect.setId(node.id);
                        rect.setDotColor(DotColor.valueOf(node.degree));
                        done = false;
                    }
                }
            }


            // Update UI
            if(!done){
                canvas = mSurfaceHolder.lockCanvas(null);
                canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

                for(List<Rectangle> rectRow: mRectangles){
                    for(Rectangle rect: rectRow){
                        // only draw unselected dots
                        if(!rect.isSelected()){
                            mColorPaint.setColor(rect.dotColor().colorInfo());
                            canvas.drawCircle(rect.getX(), rect.getY(), RADIUS, mColorPaint);
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


        System.out.print("\n------------PHASE 3 ----------------\n");
        printRects();
//        try {
//            Thread.sleep(TIME_DELAY*200);
//        }catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        mState = STATE.RESET;
    }


    private synchronized void onActionMove(){
        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        //==========================================================
        // DRAW & DECORATE DOTS
        //==========================================================
        if(mStartRect != null && mGraphNodes != null){
            for(List<Rectangle> list: mRectangles){
                for(Rectangle rect: list){
                    if(rect.id() != mStartRect.id()){
                        if(mDataLoader.checkEdge(mStartRect.id(), rect.id(), mEdges)){
                            canvas.drawCircle( rect.getX(),  rect.getY(), RADIUS, mColorPaint);
                        }else{
                            canvas.drawCircle( rect.getX(),  rect.getY(), RADIUS, mBlackPaint);
                        }
                    } else {
                        canvas.drawCircle( rect.getX(),  rect.getY(), RADIUS, mColorPaint);
                        //canvas.drawCircle( rect.centreX(),  rect.centreY(), RADIUS - 20, mBlackPaint);
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
                canvas.drawLine(line.startX, line.startY, line.endX, line.endY, mColorPathPaint);
            }
        }

        if(mIsDrawing){
            if (mStartRect != null && (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) ){
                canvas.drawLine(mStartX, mStartY, mx, my, mColorPathPaint);
                last_mx = mx;
                last_my = my;
            }
        }


        drawHeaderAndFooter(canvas);

        // display
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }


    private synchronized void onActionReset(){

        Log.i(TAG, "Buffered: " + mBufferNodes.size() + "; Recycled: " + mDeletedNodes.size());

        //========================
        // REDRAW UI
        //========================
        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        drawHeaderAndFooter(canvas);

        for(List<Rectangle> rectRow: mRectangles){
            for(Rectangle rect: rectRow){
                mColorPaint.setColor(rect.dotColor().colorInfo());
                canvas.drawCircle(rect.getX(), rect.getY(), RADIUS, mColorPaint);
            }
        }
        mSurfaceHolder.unlockCanvasAndPost(canvas);

        //===========================
        // RESET
        //===========================

        // DESELECT DOTS
        for(int i = MAX_ROW_COUNT - 1; i >= 0; i--){
            List<Rectangle> list = mRectangles.get(i);
            for(Rectangle rect: list){
                rect.deselect();
            }
        }

        // DEBUGGING
        Log.i(TAG, "__________________RESET_______________");
        Log.i(TAG,"SELECTED DOTS: " + mSelectedDots.size());
        printRects();

        mLines.clear();
        mIsRectFormed = false;
        mSelectedDots.clear();
        mStartRect =  null;
        mState = STATE.DO_NOTHING;

        if(mGameOverListener != null){
//            if(mGameLevel.succeeded() || mGameLevel.failed()){
//                mGameOverListener.gameOver(mGameLevel);
//            }
            if(mGameState.succeeded() || mGameState.failed()){
                mGameOverListener.gameOver(mGameState);
            }
        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Draw Graph
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void setupGame(){
        headerRect = new Rect(0, 0, getMeasuredWidth(), HEADER_FOOTER_HEIGHT);
        footerRect = new Rect(0, getMeasuredHeight() - HEADER_FOOTER_HEIGHT, getMeasuredWidth(), getMeasuredHeight());
        scoreRect = new Rect(0, 0, SCORE_TEXT_DIM, SCORE_TEXT_DIM);
        movesRect = new Rect(getMeasuredWidth() - SCORE_TEXT_DIM, 0, getMeasuredWidth(), SCORE_TEXT_DIM);

        Canvas canvas;

        boolean doneDrawing = false;

        int currentRow = 5; // start with the last row
        float endY = loadRow(currentRow);
        float startY = ANIM_START_POS;     // current height of the rows of dots
        float stepSize = (endY - startY)/2;  // change in Y

        while(!doneDrawing){
            //==========================================
            // TEMP DRAWING
            //========================================
            canvas = mSurfaceHolder.lockCanvas(null);
            canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

            drawHeaderAndFooter(canvas);

            for(Circle circle: mCircles){
                mColorPaint.setColor(circle.color());
                canvas.drawCircle(circle.centreX(), circle.centreY(), RADIUS, mColorPaint);
            }
            for(Circle circle: mBufferCircles){
                mColorPaint.setColor(circle.color());
                canvas.drawCircle(circle.centreX(), startY, RADIUS, mColorPaint);
            }
            mSurfaceHolder.unlockCanvasAndPost(canvas);
            try {
                Thread.sleep(TIME_DELAY * 2);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            /////////////////////////////////////////


            startY += stepSize;
            if(startY >= endY){
                startY = endY;

                canvas = mSurfaceHolder.lockCanvas(null);
                canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);
                drawHeaderAndFooter(canvas);
                mCircles.addAll(mBufferCircles);
                for(Circle circle: mCircles){
                    mColorPaint.setColor(circle.color());
                    canvas.drawCircle(circle.centreX(), circle.centreY(), RADIUS, mColorPaint);
                }
                mSurfaceHolder.unlockCanvasAndPost(canvas);


                try {
                    Thread.sleep(TIME_DELAY * 8);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                if(--currentRow > -1){
                    startY = ANIM_START_POS;
                    endY = loadRow(currentRow);
                    stepSize = (endY - startY)/2;
                }else{
                    doneDrawing = true;
                    mInitialized = true;
                }
            }
        }
    }

    private int loadRow(int row){
        List<Rectangle> newRow = new ArrayList<>();
        int x = (getMeasuredWidth() - DIMENSION)/2 ;
        int y = (getMeasuredHeight() - DIMENSION)/2 + row*(2* RADIUS + VERTICAL_SPACING);
        mBufferCircles = new ArrayList<>();
        for (int i = row*MAX_COLUMN_COUNT, count = i+MAX_COLUMN_COUNT, col =0; i < count ; i++, col++) {

            DotNode node = mGraphNodes.get(i);

            mBufferCircles.add(new Circle(x,y, RADIUS,node.id, DotColor.colorInfo(node.degree)));

            // Define new Rectangle
            newRow.add( new Rectangle(
                            x - RADIUS - RECT_TOLERANCE,
                            y - RADIUS - RECT_TOLERANCE ,
                            x + RADIUS + RECT_TOLERANCE,
                            y + RADIUS + RECT_TOLERANCE,
                               node,  row, col
                    ));
            newRow.get(newRow.size() - 1).setAnimStartY(ANIM_START_POS);

            // increase column
            x += 2* RADIUS + HORIZONTAL_SPACING;
        }
        mRectangles.push(newRow);
        return y;
    }

    private DotNode getNewNode(){
        DotNode node = null;

        while(true){
            if(mBufferNodes.size() > 0){
                node = mBufferNodes.get(mBufferNodes.size() - 1);
                mBufferNodes.remove(mBufferNodes.size() - 1);
            }else{
                // If all nodes in the buffer container have been exhausted, ...
                // ... recycle the old deleted nodes.
                Log.i(TAG, "Recycled");
                Log.i(TAG, "Before: " + mBufferNodes.size() + ", " + mDeletedNodes.size());
               // Collections.shuffle(mDeletedNodes);
                mBufferNodes.addAll(mDeletedNodes);
                mDeletedNodes.clear();
                Log.i(TAG, "After " + mBufferNodes.size() + ", " + mDeletedNodes.size());
            }

            if(node != null && mGameState.isNodeValid(node.degree)){
                break;
            }

            mDeletedNodes.add(node);
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
                mColorPaint.setColor(dotColor.colorInfo());
                canvas.drawCircle(startX, startY, RADIUS_SMALL, mColorPaint);

                if(!mGameState.isCollected(dotColor)){
                    String string = score.get(dotColor)[State.IDX_DOTS_COLLECTED] + " / " + score.get(dotColor)[State.IDX_DOTS_GOAL] ;
                    canvas.drawText(string, startX, startY + 100, mTextPaint);
                }else{
                    // draw tick mark
                    float posY = startY + RADIUS_SMALL*0.4f;
                    float x = RADIUS_SMALL*(float)Math.cos(Math.PI/3);
                    float y = RADIUS_SMALL*(float)Math.sin(Math.PI/3);
                    canvas.drawLine(startX, posY, startX + x - 5, posY  - y, mWhitePathPaint);
                    x = .5f*RADIUS_SMALL*(float)Math.cos(Math.PI/6);
                    y = .5f*RADIUS_SMALL*(float)Math.sin(Math.PI/6);
                    canvas.drawLine(startX, posY, startX - x, posY  - y, mWhitePathPaint);
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
        mColorPaint.setColor(Color.parseColor("#FF4081"));
        canvas.drawCircle(posX, posY, radius, mColorPaint);
        canvas.drawLine(posX - .6f*radius, posY, posX + .6f*radius, posY, mWhitePathPaint);
        canvas.drawLine(posX , posY- .6f*radius, posX , posY+ .6f*radius, mWhitePathPaint);

        // Draw Ability #2
        // Add additional number of moves
        posX += spacing + 2*radius;
        mColorPaint.setColor(Color.parseColor("#ffb6e9b5"));
        canvas.drawCircle(posX, posY, radius, mColorPaint);
        canvas.drawLine(posX, posY, posX, posY - radius*.7f, mWhitePathPaint);
        float offsetX = .75f*radius*(float)Math.cos(Math.PI/4);
        float offsetY = .75f*radius*(float)Math.sin(Math.PI/4);
        canvas.drawLine(posX, posY, posX + offsetX, posY + offsetY, mWhitePathPaint);

        // Draw Ability #3
        posX += spacing + 2*radius;
        mColorPaint.setColor(Color.LTGRAY);
        canvas.drawCircle(posX, posY, radius, mColorPaint);


        if(mStartRect != null){
            mColorPaint.setColor(mStartRect.dotColor().colorInfo());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Debugging
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void printRects(){
        System.out.print("\n---------------------------------------\n");
        for(int i = MAX_ROW_COUNT - 1; i >= 0; i--){
            List<Rectangle> list = mRectangles.get(i);
            for(Rectangle rect: list){
                if(rect.isSelected()) {
                    System.out.print(colorToString(rect.dotColor().colorInfo()) + "* ");
                }else{
                    System.out.print(colorToString(rect.dotColor().colorInfo()) + "  ");
                }
            }
            System.out.print("\n");
        }
        System.out.print("\n--------------------------------------\n");
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
    //     Other Methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public interface GameOverListener{
        void gameOver(State state);
    }

    public void setGameOverListener(GameOverListener listener){
        mGameOverListener = listener;
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
            System.out.println("I'm just waiting.");
        }
        Log.i(TAG, "#2.  SURFACE READY");
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

        }
        Log.i(TAG, "#3.  DONE LOADING DATA");

        if(!mInitialized){
            setupGame();
        }

        while(!mInitialized){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("I'm just waiting in mInitializer.");
        }
        mReady = true;

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
//        DotColor key = DotColor.valueOf(degree);
////        boolean badDot = true;
////        boolean redDot = false;
////        int currentCount = 0;
////
////        if(key == DotColor.RED){
////            redDot = true;
////        }
////
////        if(mGameLevel.getObjective().containsKey(key)){
////            badDot = false;
////        }
////
////        if(mDotColorCounter.containsKey(key)){
////            currentCount = mDotColorCounter.get(key);
////        }
////
////        if(!badDot || (redDot && currentCount < 6) || currentCount < 3 ){
////            currentCount++;
////            mDotColorCounter.put(key, currentCount);
////            return true;
////        }
////        return false;
        return mGameState.isNodeValid(degree);
    }

    @Override
    public void onLoad(ArrayList<DotNode> nodes) {
        mGraphNodes = nodes;
        mDoneLoadingData = true;
    }

    @Override
    public void onLoadBuffer(ArrayList<DotNode> nodes) {
        mBufferNodes = nodes;
        Log.i(TAG, "BUFFER SIZE " + mBufferNodes.size());
    }

}