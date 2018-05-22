package com.nders.motif.views;

import android.content.Context;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;


import com.nders.motif.data.LevelDatabaseHelper;
import com.nders.motif.entities.DotColor;
import com.nders.motif.game.Level;

import java.util.Map;


/**
 * Created by nders on 3/5/2018.
 */

public class GameObjectiveView extends View {

    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mPaint;
    private TextPaint mTextPaint;



    private final int RADIUS = 40;
    private final int PADDING = 40;
    private final int TEXT_SIZE = 50;
    private final int TEXT_PADDING = 100;

    private int mLevelId;

    public GameObjectiveView(Context context) {
        super(context);
    }

    public GameObjectiveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = 250;
        int desiredHeight = 180;

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
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    private void init(){
        LevelDatabaseHelper helper = null;
        Level level =  null;

        try {
            helper = LevelDatabaseHelper.getInstance(getContext());
        } catch (Exception e) {
            throw new Error("Unable to create database");
        }
        finally {
            try {
                if(helper != null){
                    level = helper.getLevel(mLevelId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        int numOfDots = 1;
        Map<DotColor, Integer> objective =  null;
        if(level != null){
            objective = level.getObjective();

            int i = 0;
            for(DotColor dotColor: objective.keySet()){
                if(objective.get(dotColor) > 0){
                    i++;
                }
            }
            numOfDots = i;
        }

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.BLACK);


        float startX = getMeasuredWidth() -
                (numOfDots*RADIUS*2 + PADDING*(numOfDots -1));
        float startY = getMeasuredHeight() -
                (2*RADIUS + TEXT_SIZE );

        startX /= 2;
        startX += RADIUS;

        startY /= 2;
        startY += RADIUS;

        if(objective != null){
            for(DotColor dotColor: objective.keySet()){
                if(objective.get(dotColor) > 0){
                    mPaint.setColor(dotColor.colorInfo());
                    mCanvas.drawCircle(startX, startY, RADIUS, mPaint);
                    mCanvas.drawText(String.valueOf(objective.get(dotColor)), startX, startY + TEXT_PADDING, mTextPaint);

                    startX += RADIUS*2 + PADDING;
                }
            }
        }


    }



    public void setGameLevel(int levelId){
        mLevelId = levelId;
    }
}
