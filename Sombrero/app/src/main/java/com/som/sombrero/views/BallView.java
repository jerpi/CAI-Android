package com.som.sombrero.views;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.som.sombrero.exceptions.HandlerLaunchedException;
import com.som.sombrero.listeners.LeaveScreenListener;
import com.som.sombrero.listeners.WallBounceListener;

/**
 * Created by Jérémy on 17/10/2017.
 */

public class BallView extends AppCompatImageView implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private GestureDetectorCompat mDetector;
    private Handler mHandler;
    private Runnable mRunnable;

    private LeaveScreenListener leaveScreenListener = null;
    private WallBounceListener wallBounceListener = null;

    private final static float STEP = 5;

    private float speedX = 0;
    private float speedY = 0;

    public BallView(Context context) {
        this(context, null, 0);
    }

    public BallView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BallView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDetector = new GestureDetectorCompat(context, this);
        setOnTouchListener(this);
    }

    public void startHandler() throws HandlerLaunchedException {
        if (mHandler != null) {
            throw new HandlerLaunchedException("Handler is already launched");
        }

        mHandler = new Handler();

        mRunnable = new Runnable() {
            @Override
            public void run() {

                View parent = (View)getParent();
                final int width = parent.getWidth();
                final int height = parent.getHeight();

                float dx = speedX;
                float dy = speedY;

                float futurX = getX() + dx;
                float futurY = getY() + dy;

                if (futurX < 0) {
                    dx = Math.abs(dx);
                    speedX = Math.abs(speedX);
                    if (wallBounceListener != null) {
                        wallBounceListener.onWallBounced();
                    }
                }
                if (futurX > width) {
                    dx = - Math.abs(dx);
                    speedX = - Math.abs(speedX);
                    if (wallBounceListener != null) {
                        wallBounceListener.onWallBounced();
                    }
                }
                if (futurY < 0) {
                    dy = Math.abs(dy);
                    speedY = Math.abs(speedY);
                    if (leaveScreenListener != null) {
                        leaveScreenListener.onScreenLeft();
                    }
                }
                if (futurY > height) {
                    dy = - Math.abs(dy);
                    speedY = - Math.abs(dy);
                    if (wallBounceListener != null) {
                        wallBounceListener.onWallBounced();
                    }
                }

                setX(getX() + dx);
                setY(getY() + dy);

                mHandler.postDelayed(this, 1000/60);
            }
        };
        mHandler.post(mRunnable);
    }

    public void stopHandler() {
        if (mHandler == null) { return; }
        mHandler.removeCallbacks(mRunnable);
        mRunnable = null;
        mHandler = null;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("GestureDetector",
                "event1:" + e1.toString() +
                        "\nevent2:" + e2.toString() +
                        "\nvelocityX:" + velocityX +
                        "\nvelocityY:" + velocityY
        );
        float ratio = 1f/1000f;
        speedX = ratio*velocityX;
        speedY = ratio*velocityY;
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mDetector.onTouchEvent(event);
        return true;
    }

    public LeaveScreenListener getLeaveScreenListener() {
        return leaveScreenListener;
    }

    public void setLeaveScreenListener(LeaveScreenListener leaveScreenListener) {
        this.leaveScreenListener = leaveScreenListener;
    }

    public WallBounceListener getWallBounceListener() {
        return wallBounceListener;
    }

    public void setWallBounceListener(WallBounceListener wallBounceListener) {
        this.wallBounceListener = wallBounceListener;
    }
}