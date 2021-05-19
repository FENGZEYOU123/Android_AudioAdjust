package com.yfz.main.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

import com.yfz.main.R;

/**
 * 作者：游丰泽
 * 简介：仿ios风格的音量控制view
 * CSDN: https://blog.csdn.net/ruiruiddd
 * GITHUB: https://github.com/FENGZEYOU123
 */
public class IosColumnAudioView extends View {
    private Context mContext;
    //防止手指持续移动最大距离
    private final double PREVENT_MOVE_DISTANCE = 2.0;
    //手指移动后,UI位移幅度大小
    private double mMoveDistance =10.0;
    //当前UI高度与view高度的比例
    private double mCurrentLoudRate = 0.5;
    //系统最大声音index
    private int mMaxLoud=0;
    //记录按压时手指相对于组件view的高度
    private float mDownY;
    //系统audio管理
    private AudioManager mAM;
    //画笔
    private Paint mPaint;
    //位置
    private RectF mRectF;
    /**
     * 设置声音流类型-默认音乐-iosColumnAudioView_setAudioStreamType
     */
    private int mAudioStreamInt= AudioManager.STREAM_MUSIC;
    /**
     * 设置圆弧度数-xml-iosColumnAudioView_setRadiusXY
     */
    private float mRXY=40;
    /**
     * 设置当前音量颜色-xml-iosColumnAudioView_setColorLoud
     */
    private int mColorLoud = Color.GRAY;
    /**
     * 设置组件背景颜色-xml-iosColumnAudioView_setColorBackground
     */
    private int mColorBackground = Color.DKGRAY;
    /**
     * 设置文字大小-xml-iosColumnAudioView_setTextSize
     */
    private float mTextSize = 15;
    /**
     * 设置文字颜色-xml-iosColumnAudioView_setTextColor
     */
    private int mTextColor = Color.BLACK;

    public IosColumnAudioView(Context context) {
        super(context);
        initial(context);
    }

    public IosColumnAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTextSize = sp2px(context,mTextSize);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.IosColumnAudioView);
        mColorBackground = typedArray.getColor(R.styleable.IosColumnAudioView_iosColumnAudioView_setColorBackground,mColorBackground);
        mColorLoud = typedArray.getColor(R.styleable.IosColumnAudioView_iosColumnAudioView_setColorLoud,mColorLoud);
        mAudioStreamInt=typedArray.getInteger(R.styleable.IosColumnAudioView_iosColumnAudioView_setAudioStreamType,mAudioStreamInt);
        mRXY = typedArray.getDimension(R.styleable.IosColumnAudioView_iosColumnAudioView_setRadiusXY, mRXY);
        mTextSize = typedArray.getDimension(R.styleable.IosColumnAudioView_iosColumnAudioView_setTextSize,mTextSize);
        mTextColor = typedArray.getColor(R.styleable.IosColumnAudioView_iosColumnAudioView_setTextColor,mTextColor);
        initial(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMoveDistance = MeasureSpec.getSize(heightMeasureSpec) * 0.05;
        if(null != mContext) {
            mMoveDistance = px2dip(mContext, mMoveDistance);
        }
    }

    private void initial(Context context){
        mContext=context;
        mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxLoud = mAM.getStreamMaxVolume(mAudioStreamInt);
        mCurrentLoudRate = (double) mAM.getStreamVolume(mAudioStreamInt) / mMaxLoud;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectF = new RectF();
        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mTextSize);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownY=event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mDownY - event.getY() > PREVENT_MOVE_DISTANCE){ //向上移动
                    mCurrentLoudRate = ( getHeight() * mCurrentLoudRate + mMoveDistance) /  getHeight();
                }else if(mDownY - event.getY() < -1 * PREVENT_MOVE_DISTANCE) {
                    mCurrentLoudRate = ( getHeight() * mCurrentLoudRate - mMoveDistance) /  getHeight();
                }
                if(mCurrentLoudRate >=1){
                    mCurrentLoudRate =1;
                }
                if(mCurrentLoudRate <=0){
                    mCurrentLoudRate =0;
                }
                mDownY=event.getY();
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        refresh();
        return true;
    }

    private void refresh(){
        mAM.setStreamVolume(mAudioStreamInt, (int)(mCurrentLoudRate * mMaxLoud), 0);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int layerId=canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        onDrawBackground(canvas);
        onDrawLoud(canvas,layerId);
        onDrawText(canvas);
        canvas.restoreToCount(layerId);
    }

    /**
     * 画圆弧背景
     * @param canvas
     */
    private void onDrawBackground(Canvas canvas){
        mPaint.setColor(mColorBackground );
        mRectF.left=0;
        mRectF.top=0;
        mRectF.right=canvas.getWidth();
        mRectF.bottom=canvas.getHeight();
        canvas.drawRoundRect(mRectF,mRXY,mRXY,mPaint);
    }

    /**
     * 画音量背景-方形-随手势上下滑动而变化用来显示音量大小
     * @param canvas
     * @param layerId
     */
    private void onDrawLoud(Canvas canvas, int layerId){
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        mPaint.setColor(mColorLoud);
        mRectF.left=0;
        mRectF.top=(canvas.getHeight()-(int)(canvas.getHeight() * mCurrentLoudRate));
        mRectF.right=canvas.getWidth();
        mRectF.bottom=canvas.getHeight();
        canvas.drawRect(mRectF,mPaint);
        mPaint.setXfermode(null);
    }
    private void onDrawText(Canvas canvas){
        mPaint.setColor(mTextColor);
        canvas.drawText( ""+(int)(mCurrentLoudRate * 100),0, 20,mPaint);
        Log.d("TAG", "onDrawText: "+""+(int)(mCurrentLoudRate * 100));
    }



        /**
         * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
         */
        public int dip2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        /**
         * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
         */
        public int px2dip(Context context, float pxValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        }
        private double px2dip(Context context, double pxValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (pxValue / scale + 0.5f);
        }
        /**
         * 将px值转换为sp值，保证文字大小不变
         */
        public int px2sp(Context context, float pxValue) {
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (pxValue / fontScale + 0.5f);
        }

        /**
         * 将sp值转换为px值，保证文字大小不变
         */
        public int sp2px(Context context, float spValue) {
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (spValue * fontScale + 0.5f);

    }

}
