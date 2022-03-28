package com.github.hueyra.biometricauth.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.github.hueyra.biometricauth.R;

/**
 * Created by zhujun.
 * Date : 2021/10/20
 * Desc : __
 */
public class CustomStatusView extends View implements Animator.AnimatorListener {

    private int progressColor;    //进度颜色
    private int loadSuccessColor;    //成功的颜色
    private int loadFailureColor;   //失败的颜色
    private float progressWidth;    //进度宽度
    private float progressRadius;   //圆环半径

    private Paint mPaint;
    private StatusEnum mStatus;     //状态

    private int startAngle = -90;
    private int minAngle = -90;
    private int sweepAngle = 120;
    private int curAngle = 0;

    //追踪Path的坐标
    private PathMeasure mPathMeasure;
    //画圆的Path
    private Path mPathCircle;
    //截取PathMeasure中的path
    private Path mPathCircleDst;
    private Path successPath;
    private Path failurePathLeft;
    private Path failurePathRight;

    private ValueAnimator circleAnimator;
    private float circleValue;
    private float successValue;
    private float failValueRight;
    private float failValueLeft;

    private OnSucOrFaiAnimEndListener mOnSucOrFaiAnimEndListener;

    public CustomStatusView(Context context) {
        this(context, null);
    }

    public CustomStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomStatusView, defStyleAttr, 0);
        progressColor = array.getColor(R.styleable.CustomStatusView_progress_color, 0xFF3DB77E);
        loadSuccessColor = array.getColor(R.styleable.CustomStatusView_load_success_color, 0xFF3DB77E);
        loadFailureColor = array.getColor(R.styleable.CustomStatusView_load_failure_color, 0xFFFA7483);
        progressWidth = array.getDimension(R.styleable.CustomStatusView_progress_width, 6);
        progressRadius = array.getDimension(R.styleable.CustomStatusView_progress_radius, 100);
        array.recycle();

        initPaint();
        initPath();
        initAnim();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(progressColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(progressWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);    //设置画笔为圆角笔触
    }

    private void initPath() {
        mPathCircle = new Path();
        mPathMeasure = new PathMeasure();
        mPathCircleDst = new Path();
        successPath = new Path();
        failurePathLeft = new Path();
        failurePathRight = new Path();
    }

    private void initAnim() {
        circleAnimator = ValueAnimator.ofFloat(0, 1);
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.translate(getPaddingLeft(), getPaddingTop());   //将当前画布的点移到getPaddingLeft,getPaddingTop,后面的操作都以该点作为参照点
        if (mStatus == StatusEnum.Loading) {    //正在加载
            canvas.translate(getPaddingLeft(), getPaddingTop());
            mPaint.setColor(progressColor);
            mPaint.setAntiAlias(true);
            if (startAngle == minAngle) {
                sweepAngle += 6;
            }
            if (sweepAngle >= 300 || startAngle > minAngle) {
                startAngle += 6;
                if (sweepAngle > 20) {
                    sweepAngle -= 6;
                }
            }
            if (startAngle > minAngle + 300) {
                startAngle %= 360;
                minAngle = startAngle;
                sweepAngle = 20;
            }
            canvas.rotate(curAngle += 4, progressRadius, progressRadius);  //旋转的弧长为4
            canvas.drawArc(new RectF(0, 0, progressRadius * 2, progressRadius * 2), startAngle, sweepAngle, false, mPaint);
            invalidate();
        } else if (mStatus == StatusEnum.Success) {     //加载成功
            canvas.translate(0, 0);
            mPaint.setColor(loadSuccessColor);
            mPaint.setAntiAlias(true);
            mPathCircle.addCircle(getWidth() / 2f, getWidth() / 2f, progressRadius, Path.Direction.CW);
            mPathMeasure.setPath(mPathCircle, false);
            mPathMeasure.getSegment(0, circleValue * mPathMeasure.getLength(), mPathCircleDst, true);   //截取path并保存到mPathCircleDst中
            canvas.drawPath(mPathCircleDst, mPaint);

            if (circleValue == 1) {      //表示圆画完了,可以钩了
                successPath.moveTo(getWidth() / 8f * 3, getWidth() / 2f);
                successPath.lineTo(getWidth() / 2f, getWidth() / 5f * 3);
                successPath.lineTo(getWidth() / 3f * 2, getWidth() / 5f * 2);
                mPathMeasure.nextContour();
                mPathMeasure.setPath(successPath, false);
                mPathMeasure.getSegment(0, successValue * mPathMeasure.getLength(), mPathCircleDst, true);
                canvas.drawPath(mPathCircleDst, mPaint);
            }
        } else if (mStatus == StatusEnum.Failure) {      //加载失败
            canvas.translate(0, 0);
            mPaint.setColor(loadFailureColor);
            mPaint.setAntiAlias(true);
            mPathCircle.addCircle(getWidth() / 2f, getWidth() / 2f, progressRadius, Path.Direction.CW);
            mPathMeasure.setPath(mPathCircle, false);
            mPathMeasure.getSegment(0, circleValue * mPathMeasure.getLength(), mPathCircleDst, true);
            canvas.drawPath(mPathCircleDst, mPaint);
            if (circleValue == 1) {  //表示圆画完了,可以画叉叉的右边部分
                failurePathRight.moveTo(getWidth() / 3f * 2, getWidth() / 3f);
                failurePathRight.lineTo(getWidth() / 3f, getWidth() / 3f * 2);
                mPathMeasure.nextContour();
                mPathMeasure.setPath(failurePathRight, false);
                mPathMeasure.getSegment(0, failValueRight * mPathMeasure.getLength(), mPathCircleDst, true);
                canvas.drawPath(mPathCircleDst, mPaint);
            }
            if (failValueRight == 1) {    //表示叉叉的右边部分画完了,可以画叉叉的左边部分
                failurePathLeft.moveTo(getWidth() / 3f, getWidth() / 3f);
                failurePathLeft.lineTo(getWidth() / 3f * 2, getWidth() / 3f * 2);
                mPathMeasure.nextContour();
                mPathMeasure.setPath(failurePathLeft, false);
                mPathMeasure.getSegment(0, failValueLeft * mPathMeasure.getLength(), mPathCircleDst, true);
                canvas.drawPath(mPathCircleDst, mPaint);
            }
        }
    }

    //重制路径
    private void resetPath() {
        successValue = 0;
        circleValue = 0;
        failValueLeft = 0;
        failValueRight = 0;
        mPathCircle.reset();
        mPathCircleDst.reset();
        failurePathLeft.reset();
        failurePathRight.reset();
        successPath.reset();
    }

    private void setStatus(StatusEnum status) {
        mStatus = status;
    }

    public void onLoading() {
        setStatus(StatusEnum.Loading);
        invalidate();
    }

    public void onSuccess() {
        resetPath();
        setStatus(StatusEnum.Success);
        startSuccessAnim();
    }

    public void onFailure() {
        resetPath();
        setStatus(StatusEnum.Failure);
        startFailAnim();
    }

    public void onLoading(int color) {
        progressColor = color;
        setStatus(StatusEnum.Loading);
        invalidate();
    }

    public void onSuccess(int color) {
        loadSuccessColor = color;
        resetPath();
        setStatus(StatusEnum.Success);
        startSuccessAnim();
    }

    public void onFailure(int color) {
        loadFailureColor = color;
        resetPath();
        setStatus(StatusEnum.Failure);
        startFailAnim();
    }

    private void startSuccessAnim() {
        ValueAnimator success = ValueAnimator.ofFloat(0f, 1.0f);
        success.addUpdateListener(animation -> {
            successValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        //组合动画,一先一后执行
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(success).after(circleAnimator);
        animatorSet.setDuration(500);
        animatorSet.addListener(this);
        animatorSet.start();
    }

    private void startFailAnim() {
        ValueAnimator failLeft = ValueAnimator.ofFloat(0f, 1.0f);
        failLeft.addUpdateListener(animation -> {
            failValueRight = (float) animation.getAnimatedValue();
            invalidate();
        });
        ValueAnimator failRight = ValueAnimator.ofFloat(0f, 1.0f);
        failRight.addUpdateListener(animation -> {
            failValueLeft = (float) animation.getAnimatedValue();
            invalidate();
        });
        //组合动画,一先一后执行
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(failLeft).after(circleAnimator).before(failRight);
        animatorSet.setDuration(500);
        animatorSet.addListener(this);
        animatorSet.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            width = size;
        } else {
            width = (int) (2 * progressRadius + progressWidth + getPaddingLeft() + getPaddingRight());
        }

        mode = MeasureSpec.getMode(heightMeasureSpec);
        size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            height = (int) (2 * progressRadius + progressWidth + getPaddingTop() + getPaddingBottom());
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (mOnSucOrFaiAnimEndListener != null) {
            mOnSucOrFaiAnimEndListener.onSucOrFaiAnimEnd();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        if (mOnSucOrFaiAnimEndListener != null) {
            mOnSucOrFaiAnimEndListener.onSucOrFaiAnimEnd();
        }
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public void setOnSucOrFaiAnimEndListener(OnSucOrFaiAnimEndListener listener) {
        mOnSucOrFaiAnimEndListener = listener;
    }

    public interface OnSucOrFaiAnimEndListener {
        void onSucOrFaiAnimEnd();
    }
}
