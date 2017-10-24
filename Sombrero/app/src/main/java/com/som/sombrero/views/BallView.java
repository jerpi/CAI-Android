package com.som.sombrero.views;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.som.sombrero.exceptions.HandlerLaunchedException;
import com.som.sombrero.listeners.OnBallLeftScreenListener;
import com.som.sombrero.listeners.OnGoalScoredListener;
import com.som.sombrero.listeners.OnWallBounceListener;

public class BallView extends AppCompatImageView implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private GestureDetectorCompat mDetector;
    private Handler mHandler;
    private Runnable mRunnable;

    private OnBallLeftScreenListener onBallLeftScreenListener = null;
    private OnWallBounceListener onWallBounceListener = null;
    private OnGoalScoredListener onGoalScoredListener = null;

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

                float futureX = getX() + dx;
                float futureY = getY() + dy;

                if (futureX < 0) {
                    dx = Math.abs(dx);
                    speedX = Math.abs(speedX);
                    if (onWallBounceListener != null) {
                        onWallBounceListener.onBounce();
                    }
                }
                if (futureX > width - getWidth()) {
                    dx = - Math.abs(dx);
                    speedX = - Math.abs(speedX);
                    if (onWallBounceListener != null) {
                        onWallBounceListener.onBounce();
                    }
                }
                if (futureY < 0) {
                    dy = Math.abs(dy);
                    speedY = Math.abs(speedY);
                    if (onBallLeftScreenListener != null) {
                        onBallLeftScreenListener.onScreenLeft();
                    }
                }
                if (futureY > height - getHeight()) {
                    dy = - Math.abs(dy);
                    speedY = - Math.abs(dy);
                    if (onGoalScoredListener != null) {
                        onGoalScoredListener.onGoalScored();
                    }
                    if (onWallBounceListener != null) {
                        onWallBounceListener.onBounce();
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

    public OnBallLeftScreenListener getOnBallLeftScreenListener() {
        return onBallLeftScreenListener;
    }

    public void setOnBallLeftScreenListener(OnBallLeftScreenListener onBallLeftScreenListener) {
        this.onBallLeftScreenListener = onBallLeftScreenListener;
    }

    public OnWallBounceListener getOnWallBounceListener() {
        return onWallBounceListener;
    }

    public void setOnWallBounceListener(OnWallBounceListener onWallBounceListener) {
        this.onWallBounceListener = onWallBounceListener;
    }

    public OnGoalScoredListener getOnGoalScoredListener() {
        return onGoalScoredListener;
    }

    public void setOnGoalScoredListener(OnGoalScoredListener onGoalScoredListener) {
        this.onGoalScoredListener = onGoalScoredListener;
    }

}
