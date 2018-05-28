package com.nders.motif.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nders.motif.SoundHelper;
import com.nders.motif.Utils;
import com.nders.motif.entities.Dot;
import com.nders.motif.entities.DotNode;
import com.nders.motif.entities.Line;
import com.nders.motif.game.Tutorial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class TutorialView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private static final String TAG = TutorialView.class.getSimpleName();

    private static final int MAX_ROW_COUNT = 6;
    private static long TIME_DELAY;

    enum STATE {ACTION_DOWN, ACTION_UP, MOVING, RESET, DO_NOTHING}

    SurfaceHolder mSurfaceHolder;
    Thread mGameThread;
    OnCompleteListener mOnCompleteListener = null;

    /*
    *   DRAWING PARAMETERS
     */

    Paint mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    Paint mBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    TextPaint mTextPaint = new TextPaint(Paint.FAKE_BOLD_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);

    static final int DOT_RADIUS = 50;
    static final int VERTICAL_SPACING = 80;
    static final int INTER_CENTRE_SPACING = VERTICAL_SPACING + 2*DOT_RADIUS;

    static final float TEXT_SIZE_MEDIUM = 60;
    static final float STROKE_WIDTH = 40;
    static final float TOUCH_TOLERANCE = 2;
    static final float DOT_TOLERANCE = 4;

    static final int BACKGROUND_COLOR = Color.WHITE;
    static final int TEXT_COLOR = Color.parseColor("#202020");


    /*
    *   GAME STATE
     */

    // Containers
    List<List<Dot>> mDots = new ArrayList<>();
    List<DotNode> mDotNodes = new ArrayList<>();
    Stack<Line> mLines = new Stack<>();
    Stack<Dot> mSelectedDots = new Stack<>();
    SparseIntArray mRelatedDots = new SparseIntArray();

    private volatile boolean mReady = false;
    private volatile boolean mIsDrawing = true;
    private volatile STATE mState = STATE.RESET;
    private volatile boolean mRunning = false;
    private boolean mSurfaceWasDestroyed = false;
    private volatile boolean mSurfaceCreated = false;
    float mx, my, mStartX, mStartY;

    Dot mStartDot = null;

    Queue<Tutorial> mTutorials ;
    Tutorial t;

    private static TutorialView sInstance = null;

    private TutorialView(Context context){
        super(context);

        mTutorials = Tutorial.tutorialList(context);

        TIME_DELAY = (int) (1000/((Activity)context).getWindowManager()
                .getDefaultDisplay().getRefreshRate());

        // Paints
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mBlackPaint.setStyle(Paint.Style.FILL);
        mBlackPaint.setColor(Color.BLACK);
        mBlackPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(STROKE_WIDTH);
        mPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);

        Typeface tf= Typeface.createFromAsset(context.getAssets(),
                "fonts/aldrich.ttf");
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setTextSize(TEXT_SIZE_MEDIUM);
        mTextPaint.setStrokeWidth(3f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        mTextPaint.setTypeface(tf);

        // Surface holder
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        mSurfaceHolder.addCallback(this);
        setDrawingCacheEnabled(true);
    }

    public static TutorialView getInstance(Context context){
        if(sInstance == null){
            sInstance = new TutorialView(context);
        }else{
            // necessary to redraw the surface
            sInstance.mState = STATE.RESET;
        }
        return sInstance;
    }

    /**
     * Used to quit and destroy the Singleton static instance of the class.
     */
    public static void pauseInstance(){
        if(sInstance != null){
            sInstance.pause();
        }
    }
    public static void quitInstance(){
        if(sInstance != null){
            sInstance.pause();
            sInstance = null;
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // ignore multitouch
        if(event.getPointerCount() > 1) {
            mState = STATE.RESET;
            return false;
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
                mStartX = mx;
                mStartY = my;
                for(List<Dot> dots: mDots){
                    for(Dot dot: dots){
                        if(dot.contains(mStartX, mStartY)){
                            mStartX = dot.centreX();
                            mStartY = dot.centreY();
                            mStartDot = dot;
                            mPathPaint.setColor(dot.color());
                            mStartDot.select();
                            mSelectedDots.push(mStartDot);
                            SoundHelper.getInstance((Activity)getContext()).playKickSound();
                            break;
                        }
                    }
                }

                if(mStartDot == null){
                    mState = STATE.DO_NOTHING;
                }else{
                    mState = STATE.ACTION_DOWN;
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


                for(List<Dot> dots: mDots) {
                    for (Dot dot : dots) {
                        //==============================================================
                        // IF "rect" CONTAINS THE COORDINATE, {mx, my},...
                        // A LINE COULD BE DRAWN
                        //==============================================================
                        if (dot.contains(mx, my)) {
                            mx = dot.centreX();
                            my = dot.centreY();

                            if (!dot.isSelected()) {
                                if (t.checkEdge(mStartDot.id(), dot.id())) {

                                    // VALIDATE LINE

                                    // Check if line is non-diagonal
                                    if (mx == mStartX || my == mStartY) {
                                        // new line to be added
                                        Line line = new Line(mStartX, mStartY, mx, my);
                                        line.startId = mSelectedDots.peek().id();
                                        line.endId = dot.id();

                                        // Check if line length is valid

                                        if (line.length() <= INTER_CENTRE_SPACING) {
                                            mLines.push(line);
                                            mStartX = mx;
                                            mStartY = my;

                                            // push dot onto stack
                                            dot.select();
                                            mSelectedDots.push(dot);
                                            SoundHelper.getInstance((Activity) getContext()).playKickSound();
                                        }
                                    }
                                }
                            } else {
                                //=================================================
                                // CHECK IF LINE TO BE DRAWN CLOSES A PATH
                                //=================================================

                                if (mSelectedDots.peek().id() == dot.id()) {
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
                                    if (mLines.size() >= 3 && line.length() <= INTER_CENTRE_SPACING && line.length() >= VERTICAL_SPACING) {

                                        List<Line> lines = new ArrayList<>();

                                        // Get the last added line
                                        int i = mLines.size() - 1;
                                        Line temp = new Line(mLines.get(i));

                                        // Concatenate lines that are of the same type.
                                        // Two line are said to be of the same type if they share end points and are both horizontal
                                        // or both vertical.
                                        // Find 3 concatenated lines (if exists)
                                        // If 3 concatenated lines exists and they form an opened rectangle, and the line to be added ...
                                        // ... shares a point with the line to be added, a path has been closed.
                                        for (i = mLines.size() - 2; i >= 0; i--) {

                                            if (temp.type() == mLines.get(i).type()) {
                                                temp.merge(mLines.get(i));
                                            } else {
                                                lines.add(temp);
                                                temp = new Line(mLines.get(i));
                                            }

                                            // break when current id is found
                                            if (temp.startId == dot.id()) {
                                                lines.add(temp);
                                                break;
                                            }
                                        }

                                        if (lines.size() >= 3 && lines.get(lines.size() - 1).startId == dot.id()) {
                                            mLines.push(line);
                                            mIsDrawing = false;
                                            Utils.vibrate(getContext().getApplicationContext());
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }

                mState = STATE.MOVING;
                break;
            case MotionEvent.ACTION_UP:
                if(mStartDot == null){
                    mState = STATE.RESET;
                }else{
                    mState = STATE.ACTION_UP;
                }
                break;
        }
    }

    /////////////////////////////////////////////////////////////////////
    //
    //  OnTouchEvent Handlers
    //
    ////////////////////////////////////////////////////////////////////

    private void onActionDown(){
        if(t.milestone() == Tutorial.MILESTONE.COLOR_CHECK ){
            t.milestoneReached(mStartDot.dotColor(), mSelectedDots.size(), mStartDot.id());
        }
    }

    private void onActionUp(){
        if( (t.milestone() == Tutorial.MILESTONE.DOT_COUNT || t.milestone() == Tutorial.MILESTONE.COLOR_AND_COUNT_CHECK)
                &&  t.milestoneReached(mStartDot.dotColor(), mSelectedDots.size(), mStartDot.id())){

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
            for(List<Dot> dots: mDots){
                for(Dot dot: dots){
                    // only draw unselected dots
                    if(!dot.isSelected()){
                        mDotPaint.setColor(dot.color());
                        canvas.drawCircle(dot.centreX(), dot.centreY(), dot.radius(), mDotPaint);
                    }
                    else{
                        dotsPerColumn.put(dots.indexOf(dot), 1 + dotsPerColumn.get(dots.indexOf(dot), 0));
                    }
                }
            }

            // Post new drawing
            mSurfaceHolder.unlockCanvasAndPost(canvas);


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
                    for(int row = mDots.size() - 1; row > 0; row--){
                        // if the above dot is unselected and the one below in selected
                        if( mDots.get(row).get(column).isSelected() && !mDots.get(row - 1).get(column).isSelected()){
                            Dot.swap(mDots.get(row).get(column),  mDots.get(row - 1).get(column));
                            mDots.get(row).get(column).deSelect();
                            mDots.get(row - 1).get(column).select();
                            done = false;
                        }
                    }
                }

                // Update UI
                if(!done){
                    canvas = mSurfaceHolder.lockCanvas(null);
                    canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

                    for(List<Dot> dots: mDots){
                        for(int i = dots.size() -1; i >= 0 ; i--){
                            Dot dot = dots.get(i);
                            // only draw unselected dots
                            if(!dot.isSelected()){
                                mDotPaint.setColor(dot.color());
                                canvas.drawCircle(dot.centreX(), dot.centreY(), dot.radius(), mDotPaint);
                            }
                        }
                    }

                    mSurfaceHolder.unlockCanvasAndPost(canvas);

                    try {
                        Thread.sleep(TIME_DELAY);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            /////////////////////////////////////////////
            //
            // STEP 3: HIDE "UNSELECTED" DOTS FALLING
            //
            ////////////////////////////////////////////

            for(List<Dot> dots: mDots){
                dots.removeIf(x -> x.isSelected());
            }
            mDots.removeIf(x -> x.isEmpty());

            draw();

            try {
                Thread.sleep(TIME_DELAY*5);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(t.complete()){
                mTutorials.poll();
                if(!mTutorials.isEmpty()){
                    t = mTutorials.peek();
                    loadTutorial(t);
                }else{
                    if(mOnCompleteListener !=  null){
                        try{
                            Thread.sleep(500);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                        Log.i(TAG, "COMPLETED---------------");
                        mOnCompleteListener.quit();
                    }
                }
            }
        }

        mState = STATE.RESET;
    }

    private void onActionMove(){
        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        //==========================================================
        // DRAW & DECORATE DOTS
        //==========================================================

        if(mStartDot != null && mDotNodes != null){
            mDotPaint.setColor(mStartDot.color());
            for(List<Dot> dots: mDots){
                for(Dot dot: dots){
                    if(dot.id() != mStartDot.id()){
                        if(t.checkEdge(mStartDot.id(), dot.id())){
                            canvas.drawCircle(dot.centreX(), dot.centreY(), DOT_RADIUS, mDotPaint);
                        }else{
                            canvas.drawCircle(dot.centreX(), dot.centreY(), DOT_RADIUS, mBlackPaint);
                        }
                    } else {
                        canvas.drawCircle(dot.centreX(), dot.centreY(), DOT_RADIUS, mDotPaint);
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
            }
        }

        displayInstruction(canvas);

        // display
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void onActionReset(){
        draw();

        mStartDot = null;
        mLines.clear();
        mSelectedDots.clear();
        mIsDrawing = true;
        mState = STATE.DO_NOTHING;
    }

    private void draw(){
        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        for(List<Dot> dots: mDots){
            for(Dot dot: dots){
                if(dot.isSelected())
                    Log.i(TAG, dot.id() + "");
                dot.deSelect();
                mDotPaint.setColor(dot.color());
                canvas.drawCircle(dot.centreX(), dot.centreY(), DOT_RADIUS, mDotPaint);
            }
        }

        displayInstruction(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void displayInstruction(Canvas canvas){
        int x = getWidth()/2, y = getHeight()/3;

        String text = t.instruction();
        if(text != null){
            for (String line: text.split("\n")) {
                canvas.drawText(line, x, y, mTextPaint);
                y += mTextPaint.descent() - mTextPaint.ascent();
            }
        }
    }

    private void displayFeedback(Canvas canvas){
        List<String> feedback = Arrays.asList("Brilliant!", "Nice!", "Excellent!");
        Collections.shuffle(feedback);

        canvas.drawText(feedback.get(0), getWidth()/2, getHeight()/3, mTextPaint);
    }

    private void loadTutorial(Tutorial t){

        Queue<DotNode> dotNodes = new LinkedList<>();
        dotNodes.addAll(t.data());
        mDots.clear();

        int col;
        int row;

        // find the col count and row count that would make
        // the dots appear most "square"
        if(Math.sqrt(dotNodes.size()) == Math.floor(Math.sqrt(dotNodes.size()))){
            col = row = (int)Math.sqrt(dotNodes.size());
        }else{
            // TODO
            row = 1;
            col = dotNodes.size();
        }


        int STARTY = (getMeasuredHeight() - row*(2*DOT_RADIUS) - (row - 1)*(VERTICAL_SPACING))/2 + VERTICAL_SPACING + DOT_RADIUS;
        int STARTX =  (getMeasuredWidth() - col*(2*DOT_RADIUS) - (col - 1)*(VERTICAL_SPACING))/2 + DOT_RADIUS;

        for(int i = 0; i < row; i++){
            List<Dot> dotRow = new ArrayList<>();

            for(int j = 0; j < col; j++){
                Dot dot = new Dot(STARTX, STARTY, DOT_RADIUS, dotNodes.poll());
                dot.setTolerance(DOT_TOLERANCE);
                dotRow.add(dot);
                STARTX += 2*DOT_RADIUS + VERTICAL_SPACING;
            }

            STARTY += 2*DOT_RADIUS + VERTICAL_SPACING;
            STARTX = (getMeasuredWidth() - (col*(2*DOT_RADIUS) + (col - 1)*(VERTICAL_SPACING)))/2 + DOT_RADIUS;
            mDots.add(dotRow);
        }

        mState = STATE.RESET;
    }

    public void start(){
        if(!mRunning){
            mRunning = true;
            mGameThread = new Thread(this);
            mGameThread.start();
            if(mSurfaceCreated && !mSurfaceWasDestroyed){
                Log.i(TAG, "Waked up");
                mReady = true;
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if(!hasWindowFocus){
            pause();
        }else {
            start();
        }
    }

    public void pause(){
        Log.i(TAG, "GAME PAUSED");
        try{
            mReady = false;
            mRunning = false;
            mGameThread.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "Surface created");
        mSurfaceCreated = true;
        mSurfaceWasDestroyed = false;
        t = mTutorials.peek();
        loadTutorial(t);
        mReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceWasDestroyed = true;
        Log.i(TAG, "Surface Destroyed");
        if(mRunning){
            pause();
        }
    }

    @Override
    public void run() {
        Log.i(TAG, "THREAD STARTED");

        while(!mReady){
            Log.i(TAG, "Not ready.......");
            try{
                Thread.sleep(3);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        Log.i(TAG, "GAME RUNNING");
        while (mRunning){
            try{
                Thread.sleep(3);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            switch (mState){
                case ACTION_DOWN:
                    onActionDown();
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
    }

    public interface OnCompleteListener{
        void quit();
    }

    public void setOnCompleteListener(OnCompleteListener listener){
        mOnCompleteListener = listener;
    }
}
