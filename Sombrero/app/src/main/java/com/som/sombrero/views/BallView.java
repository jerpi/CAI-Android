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

public class BallView extends AppCompatImageView implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private GestureDetectorCompat mDetector;
    private Handler mHandler;
    private Runnable mRunnable;

    private OffScreenListener onBallLeftScreenListener = null;
    private BounceListener onWallBounceListener = null;
    private GoalListener onGoalScoredListener = null;

    private float velocityX = 0;
    private float velocityY = 0;

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
                View parent = (View) getParent();
                final int width = parent.getWidth();
                final int height = parent.getHeight();

                float dx = velocityX;
                float dy = velocityY;

                float futureX = getX() + dx;
                float futureY = getY() + dy;

                if (futureX < 0) {
                    dx = Math.abs(dx);
                    velocityX = Math.abs(velocityX);
                    if (onWallBounceListener != null) {
                        onWallBounceListener.onBounce();
                    }
                }
                if (futureX > width - getWidth()) {
                    dx = - Math.abs(dx);
                    velocityX = - Math.abs(velocityX);
                    if (onWallBounceListener != null) {
                        onWallBounceListener.onBounce();
                    }
                }
                if (futureY < -getHeight()) {
                    dy = Math.abs(dy);
                    velocityY = Math.abs(velocityY);
                    if (onBallLeftScreenListener != null) {
                        onBallLeftScreenListener.onOffScreen();
                    }
                }
                if (futureY > height - getHeight()) {
                    dy = - Math.abs(dy);
                    velocityY = - Math.abs(dy);
                    if (onGoalScoredListener != null) {
                        onGoalScoredListener.onGoal();
                    }
                    if (onWallBounceListener != null) {
                        onWallBounceListener.onBounce();
                    }
                }

                setX(getX() + dx);
                setY(getY() + dy);

                if (mHandler != null) {
                    mHandler.postDelayed(this, 1000/60);
                }
            }
        };
        mHandler.post(mRunnable);
    }

    /**
     * Call this method when the ball goes offscreen
     */
    public void pause() {
        setVisibility(View.GONE);
        stopHandler();
    }

    public void init() throws HandlerLaunchedException {
        init(0.5f, 0.5f);
    }

    public void init(float positionX, float positionY) throws HandlerLaunchedException {
        setVisibility(View.VISIBLE);
        View parent = (View) getParent();
        setX(positionX * parent.getWidth());
        setY(positionY * parent.getHeight());
        startHandler();
    }

    /**
     * Call this method when the ball goes onscreen
     */
    public void unPause(float positionX, float velocityX, float velocityY) throws HandlerLaunchedException {
        setVisibility(View.VISIBLE);

        setVelocityX(-velocityX); // The direction is reversed from the other phone's viewpoint
        setVelocityY(Math.abs(velocityY)); // We want the ball going down

        View parent = (View) getParent();
        final float parentWidth = parent.getWidth();
        float absoluteXPosition = parentWidth * (1 - positionX);
        if (absoluteXPosition < 0) {
            absoluteXPosition = 0;
            setVelocityX(Math.abs(velocityX));
        } else if (absoluteXPosition > parentWidth - getWidth()) {
            absoluteXPosition = parentWidth - getWidth();
            setVelocityX(-Math.abs(velocityX));
        }

        setX(absoluteXPosition);
        setY(-getHeight());

        startHandler();
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
        this.velocityX = ratio*velocityX;
        this.velocityY = ratio*velocityY;
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mDetector.onTouchEvent(event);
        return true;
    }

    public void setOnBallLeftScreenListener(OffScreenListener onBallLeftScreenListener) {
        this.onBallLeftScreenListener = onBallLeftScreenListener;
    }

    public void setOnWallBounceListener(BounceListener onWallBounceListener) {
        this.onWallBounceListener = onWallBounceListener;
    }

    public void setOnGoalScoredListener(GoalListener onGoalScoredListener) {
        this.onGoalScoredListener = onGoalScoredListener;
    }

    public float getVelocityX() {
        return velocityX;
    }
    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }
    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public float getRelativeX() {
        View parent = (View) getParent();
        float w = parent.getWidth() - getWidth();
        return getX() / w;
    }

    public float getRelativeY() {
        View parent = (View) getParent();
        float h = parent.getHeight() - getHeight();
        return getY() / h;
    }

    public interface OffScreenListener {
        void onOffScreen();
    }

    public interface GoalListener {
        void onGoal();
    }

    public interface BounceListener {
        void onBounce();
    }
}
