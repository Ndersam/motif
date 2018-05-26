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
import com.nders.motif.data.Loader;
import com.nders.motif.entities.Dot;
import com.nders.motif.entities.DotNode;
import com.nders.motif.entities.Line;
import com.nders.motif.game.Tutorial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class TutorialView extends SurfaceView implements SurfaceHolder.Callback, Runnable, Loader.LoaderListener{

    private static final String TAG = TutorialView.class.getSimpleName();
    private static long TIME_DELAY;

    enum STATE {ACTION_DOWN, ACTION_UP, MOVING, RESET, DO_NOTHING}

    SurfaceHolder mSurfaceHolder;

    /*
     *   DATA
     */
    Loader mDataLoader;
    HashMap<ArrayList, Boolean> mEdges = new HashMap<>();

    /*
    *   DRAWING PARAMETERS
     */


    Paint mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    Paint mBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    TextPaint mTextPaint = new TextPaint(Paint.FAKE_BOLD_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);

    static final int DOT_RADIUS = 50;
    static final int VERTICAL_SPACING = 80;
    static final int INTER_CENTRE_SPACING = VERTICAL_SPACING + 2*VERTICAL_SPACING;

    static final float TEXT_SIZE_MEDIUM = 60;
    static final float TEXT_SIZE_LARGE = 150;
    static final float TEXT_SIZE_SMALL = 40;
    static final float TEXT_PADDING = 100;

    static final float STROKE_WIDTH = 40;
    static final float TOUCH_TOLERANCE = 2;
    static final float DOT_TOLERANCE = 4;

    // Color
    static final int BACKGROUND_COLOR = Color.WHITE;
    static final int TEXT_COLOR = Color.parseColor("#202020");

    /*
    *   CONTAINERS
     */
    List<List<Dot>> mDots = new ArrayList<List<Dot>>();
    List<DotNode> mDotNodes = new ArrayList<>();
    int mDotNodesSize = 0;
    Stack<Line> mLines = new Stack<>();
    Stack<Dot> mSelectedDots = new Stack<>();

    float mx, my, mStartX, mStartY;
    Dot mStartDot = null;

    /*
    *   GAME STATE
     */
    private boolean mReady = false;
    private boolean mIsRectFormed = false;
    private boolean mIsDrawing = true;
    private STATE mState = STATE.RESET;
    private boolean mRunning = true;
    private boolean mDone = false;

    Queue<Tutorial> mTutorials = Tutorial.tutorialList();
    Tutorial t;


    public TutorialView(Context context){
        super(context);

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

        // Data Loader
        mDataLoader = new com.nders.motif.data.Loader(context,36);
        mDataLoader.setGraphNumber(1);
        mDataLoader.disableLoadAll();
        mDataLoader.setLoadListener(this);

        // Surface holder
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        mSurfaceHolder.addCallback(this);
        setDrawingCacheEnabled(true);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // hack to cancel multitouch events
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
                                if (mDataLoader.checkEdge(mStartDot.id(), dot.id(), mEdges)) {

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
                                            mIsRectFormed = true;
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

    private void onActionUp(){
        if(mSelectedDots.size() > 1){

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

            t.target -= mSelectedDots.size();
            if(t.target <= 0){
                if(!mTutorials.isEmpty()){
                    t = mTutorials.poll();
                    setupTutorial(t);
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
                        if(mDataLoader.checkEdge(mStartDot.id(), dot.id(), mEdges)){
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

        // display
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void onActionReset(){
        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        for(List<Dot> dots: mDots){
            for(Dot dot: dots){
                dot.deSelect();
                Log.i(TAG, dot.id() + "");
                mDotPaint.setColor(dot.color());
                canvas.drawCircle(dot.centreX(), dot.centreY(), DOT_RADIUS, mDotPaint);
            }
        }

        displayInstruction(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);

        mStartDot = null;
        mLines.clear();
        mSelectedDots.clear();
        mState = STATE.DO_NOTHING;
    }

    private void displayInstruction(Canvas canvas){
        if(!t.instructions.isEmpty()){
            String text = t.instructions.poll();
            canvas.drawText(text, getWidth()/2, getHeight()/3, mTextPaint);
        }
    }

    private void displayFeedback(Canvas canvas){
        List<String> feedback = Arrays.asList("Brilliant!", "Nice!", "Excellent!");
        Collections.shuffle(feedback);

        canvas.drawText(feedback.get(0), getWidth()/2, getHeight()/3, mTextPaint);
    }


    private void setupTutorial(Tutorial t){
//        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        mDots.clear();

        int col = 2;
        int row = 1;

        if(t.dotCount == 4){
            row = 2;
        }else if(t.dotCount == 16){
            row = col = 4;
        }

        int STARTY = (getHeight() - col*(2*DOT_RADIUS) - (col - 1)*(VERTICAL_SPACING))/2;
        int STARTX =  (getWidth() - col*(2*DOT_RADIUS) - (col - 1)*(VERTICAL_SPACING))/2;

        for(int i = 0; i < row; i++){
            List<Dot> dotRow = new ArrayList<>();

            for(int j = 0; j < col; j++){
                Dot dot = new Dot(STARTX, STARTY, DOT_RADIUS, mDotNodes.get(mDotNodesSize - 1));
                dot.setTolerance(DOT_TOLERANCE);
                dotRow.add(dot);

                mDotNodesSize--;
                STARTX += INTER_CENTRE_SPACING;
            }

            STARTY += INTER_CENTRE_SPACING;
            STARTX = (getWidth() - col*(2*DOT_RADIUS) - (col - 1)*(VERTICAL_SPACING))/2;
            mDots.add(dotRow);
        }


        mState = STATE.RESET;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Arrays.sort(Tutorial.data);
        mDataLoader.loadHandPicked(Tutorial.data);
        Log.i(TAG, "Surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
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
    }

    @Override
    public boolean isNodeValid(int degree) {
        return true;
    }

    @Override
    public void onLoad(ArrayList<DotNode> nodes) {
        mDotNodes = nodes;
        Collections.sort(mDotNodes, DotNode.idComparator());
        mDotNodesSize = mDotNodes.size();

        t = mTutorials.poll();
        setupTutorial(t);
        new Thread(this).start();
        mReady = true;
    }

    @Override
    public void onLoadBuffer(ArrayList<DotNode> nodes) {

    }
}
