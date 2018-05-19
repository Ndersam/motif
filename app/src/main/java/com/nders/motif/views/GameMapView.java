package com.nders.motif.views;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.nders.motif.Constants;
import com.nders.motif.R;
import com.nders.motif.entities.Circle;
import com.nders.motif.levels.Level;


import java.util.ArrayList;
import java.util.List;


public class GameMapView extends View {
    // Drawing variables
    protected Canvas mCanvas;
    protected Bitmap mBitmap;
    protected Bitmap mFullMap;


    final Paint mWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    final Paint mDashPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    final Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    final Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    final Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    final TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);

    /*
    *   CONSTANTS
    */
    // DRAW params
    private final int STROKE_WIDTH = 30;
    private final int RADIUS = 120;
    private final int RING_WIDTH = 20;
    private final int mMapHeight = 7400;
    private int mMapWidth = 1440;

    // COLORS
    private final int BACKGROUND_COLOR = Color.parseColor("#324056") ;//Color.parseColor("#DF8E57");
    private final int PATH_COLOR = Color.WHITE;//Color.parseColor("#B16248");
    private final int SHADOW_COLOR =  Color.parseColor("#202020") ;//Color.parseColor("#CC703E");
    private final int LOCKED_COLOR = Color.parseColor("#EDD7B0");
    private final int TEXT_COLOR = Color.parseColor("#8A3F3C"); //AC5F47
    private final int UNLOCKED_COLOR = Color.parseColor("#ffb732") ;//Color.parseColor("#D77D4B");

    private static final String TAG = GameMapView.class.getSimpleName();

    /*
    *  EVENT
    */
    private List<Circle> mLocations = new ArrayList<>();
    private int mScrollOffset = 0;

    Rect src;
    Rect dst;

    /*
    *  LISTENER
    */
    private GameListener mGameListener = null;
    private GestureDetector mGestureDetector;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Constructors
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    public GameMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new GestureDetector(context, new GestureListener());

        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(STROKE_WIDTH);
        mPathPaint.setColor(PATH_COLOR);
        mPathPaint.setDither(true);

        mDashPathPaint.setStyle(Paint.Style.STROKE);
        mDashPathPaint.setStrokeWidth(STROKE_WIDTH *.5f);
        mDashPathPaint.setColor(PATH_COLOR);
        mDashPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mDashPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mDashPathPaint.setAlpha(120);
        mDashPathPaint.setPathEffect(new DashPathEffect(new float[]{60, 30}, 0));

        mCirclePaint.setStrokeWidth(1f);
        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCirclePaint.setTextAlign(Paint.Align.CENTER);
        mCirclePaint.setColor(LOCKED_COLOR);
        mCirclePaint.setDither(true);
        mCirclePaint.setAlpha(255);

        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setStrokeWidth(1f);
        mWhitePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mWhitePaint.setAlpha(255);

        mShadowPaint.setColor(SHADOW_COLOR);
        mShadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mShadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        mShadowPaint.setAlpha(95);

        Typeface tf= Typeface.createFromAsset(context.getAssets(),
                "fonts/aldrich.ttf");
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(90);
        mTextPaint.setStrokeWidth(3f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(tf);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().remove(Constants.KEY_MAP_RECT_BOTTOM).apply();
        mScrollOffset = pref.getInt(Constants.KEY_MAP_RECT_BOTTOM, mMapHeight);
    }

    public GameMapView(Context context) {
        super(context, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Override methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        int desiredWidth = pref.getInt(Constants.KEY_SCREEN_WIDTH, 1440);
        int desiredHeight = pref.getInt(Constants.KEY_SCREEN_HEIGHT, 2560);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMapWidth = w;
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mFullMap = Bitmap.createBitmap(mMapWidth, mMapHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mFullMap);
        dst = new Rect(0,0, w, h);

        if(mScrollOffset < h){
            mScrollOffset = mMapHeight;
        }

        src = new Rect(0, mScrollOffset - getMeasuredHeight(), mMapWidth, mScrollOffset);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        //------------------
        canvas.drawBitmap(mFullMap, src, dst, mCirclePaint);

//         draw your element
//        if (isDrawing) {
//
//        }
//        ------------------
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return (event.getPointerCount() <= 1) &&mGestureDetector.onTouchEvent(event);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Other methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void init(){

        // Constants
        final int RECT_WIDTH = 480; // 540;
        final int RECT_HEIGHT = 480; //500;

        final int MARGIN = (getMeasuredWidth() - RECT_WIDTH *2)/2;
        final int LEFT = MARGIN;
        final int RIGHT = getMeasuredWidth() - MARGIN;
        final int BOTTOM =  mFullMap.getHeight() - 300;
        final int TOP = BOTTOM - RECT_HEIGHT;


        float startX = getMeasuredWidth()*3/4f;
        float startY = mFullMap.getHeight();


        /////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////
        Path path = new Path();

        RectF leftRect = new RectF(LEFT, TOP,  (LEFT+RIGHT)/2, BOTTOM);
        RectF rightRect = new RectF((LEFT+RIGHT)/2, TOP - RECT_HEIGHT, RIGHT, TOP);


        // Start line
        path.moveTo(leftRect.left + RECT_WIDTH /2 + RECT_WIDTH, startY);
        path.lineTo(leftRect.left + RECT_WIDTH /2 + RECT_WIDTH, leftRect.bottom - STROKE_WIDTH/2);


        int level = 1;

        // level 1
        mLocations.add(new Circle(leftRect.left + RECT_WIDTH /2 + RECT_WIDTH, leftRect.bottom, RADIUS, level));


        // levels 2 - 22
        for(int i = 0; i < 7; i++){
            path.moveTo(leftRect.left + RECT_WIDTH /2 + RECT_WIDTH, leftRect.bottom);
            path.lineTo(  leftRect.left+ RECT_WIDTH /2, leftRect.bottom);
            path.arcTo(leftRect, 90, 180, true);

            path.moveTo(leftRect.left + RECT_WIDTH /2, leftRect.top);
            path.lineTo(leftRect.left +  RECT_WIDTH /2 + RECT_WIDTH, leftRect.top);

            path.arcTo(rightRect, 270, 180, true);

            level++;
            mLocations.add(new Circle(leftRect.left + 50, leftRect.bottom - RECT_HEIGHT /2, RADIUS, level));

            level++;
            mLocations.add(new Circle(leftRect.right + RECT_WIDTH /4, rightRect.bottom, RADIUS, level));

            level++;
            mLocations.add(new Circle(rightRect.right - RECT_WIDTH /8, rightRect.top + 50, RADIUS, level));

            leftRect.offset(0, - RECT_HEIGHT *2);
            rightRect.offset(0, -RECT_HEIGHT *2);
        }

        // level 23

        /////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////


        /////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////
        final int RECT_WIDTH_DASH = Math.round(RECT_WIDTH * 1.5f);
        final int RECT_HEIGHT_DASH = RECT_HEIGHT * 2;

        final int MARGIN_DASH = (getMeasuredWidth() - RECT_WIDTH_DASH *2)/2;
        final int LEFT_DASH = MARGIN_DASH;
        final int RIGHT_DASH = getMeasuredWidth() - MARGIN_DASH;
        final int BOTTOM_DASH =  mFullMap.getHeight() + 300;
        final int TOP_DASH = BOTTOM_DASH - RECT_HEIGHT_DASH;



        RectF leftDashRect= new RectF(LEFT_DASH,
                TOP_DASH, (LEFT_DASH + RIGHT_DASH)/2, BOTTOM_DASH);

        RectF rightDashRect = new RectF((LEFT_DASH+RIGHT_DASH)/2,
                TOP_DASH - RECT_HEIGHT_DASH, RIGHT_DASH, TOP_DASH);

        Path dashPath = new Path();

        for(int i = 0; i < 7; i++){
            dashPath.moveTo(leftDashRect.left + RECT_WIDTH_DASH /2 + RECT_WIDTH_DASH, leftDashRect.bottom);
            dashPath.lineTo(  leftDashRect.left+ RECT_WIDTH_DASH /2, leftDashRect.bottom);
            dashPath.arcTo(leftDashRect, 90, 180, true);

            dashPath.moveTo(leftDashRect.left + RECT_WIDTH_DASH /2, leftDashRect.top);
            dashPath.lineTo(leftDashRect.left +  RECT_WIDTH_DASH /2 + RECT_WIDTH_DASH, leftDashRect.top);
            dashPath.arcTo(rightDashRect, 270, 180, true);

            leftDashRect.offset(0, - RECT_HEIGHT_DASH *2);
            rightDashRect.offset(0, -RECT_HEIGHT_DASH *2);
        }
        /////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////


        //=====================================================
        // DRAWING
        //====================================================

        // draw background
        mCanvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        // draw paths

        mCanvas.drawPath(path, mPathPaint);
        mCanvas.drawPath(dashPath, mDashPathPaint);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());

        int highestLevel = pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1);

        // draw circles
        level = 0;
        for(Circle location : mLocations){

            mCanvas.drawCircle(location.centreX() - 25, location.centreY() + 25, RADIUS, mShadowPaint);
            mCanvas.drawCircle(location.centreX(), location.centreY(), RADIUS, mWhitePaint);
            if(location.id() <=  highestLevel){
                mCirclePaint.setColor(UNLOCKED_COLOR);
                mCanvas.drawCircle(location.centreX(), location.centreY(), RADIUS - RING_WIDTH, mCirclePaint);
                mCirclePaint.setColor(LOCKED_COLOR);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.star, options);
                float x = location.left();
                float y = location.top() - bmp.getHeight()*.75f;
                mCanvas.drawBitmap(bmp, location.left(), y, null);

            }else{
                mCanvas.drawCircle(location.centreX(), location.centreY(), RADIUS - RING_WIDTH, mCirclePaint);
            }

            mCanvas.drawText(String.valueOf(++level), location.centreX(), location.centreY() + 40, mTextPaint);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Callback Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /*
    *   Gesture Listener
    */
    class GestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if( (src.top + distanceY < 0 ) || (src.bottom + distanceY >= mFullMap.getHeight())){
                return false;
            }

            src.offset(0, (int)distanceY);
            mScrollOffset = src.bottom;
            invalidate();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for(Circle location: mLocations){
                float y = src.top + e.getY();
                if(location.contains(e.getX(), y)){
                    Log.i(TAG, "Level " + location.id());
                    if(mGameListener != null){
                        mGameListener.displayPopup(location.id(), mScrollOffset);
                    }
                    break;
                }
            }
            return super.onSingleTapUp(e);
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     INTERFACES
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    *   Game Listener
    */
    public interface GameListener{
        void displayPopup(int levelId, int scrollOffset);
    }

    public void setGameListener(GameListener listener){
        this.mGameListener = listener;
    }

}

