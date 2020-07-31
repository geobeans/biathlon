package cn.geobeans.biathlon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.RequiresApi;

/**
 * TODO: document your custom view class.
 */
public class PanelView extends View {
    private static final int DEFAULT_STROKE_WIDTH = 8;
    private static final float DEFAULT_POINTER_ANGLE = 30.0f;

    private String mWindSpeed; // TODO: use a default from R.string...
    private int mTextColor = Color.RED; // TODO: use a default from R.color...
    private float mTextDimension = 0; // TODO: use a default from R.dimen...
    private float mAngleDimension = 0;
    private Drawable mExampleDrawable;
    private float mAngle = 0.0f; // TODO: use a default angle...

    private TextPaint mWindPaint;
    private TextPaint mAnglePaint;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float mAngle) {
        this.mAngle = mAngle-90.0f;
    }

    private float mTextWidth;
    private float mTextHeight;

    private Paint mCirclePaint; //画圆画笔
    private Paint mCirclePaint1; //画圆画笔
    private Paint mDashPaint; //画圆画笔

    private Path mPointerPath;
    private Paint mPointerPaint;
    private Paint.FontMetrics mFontMetrics;
    private float mPointerLength;

    public PanelView(Context context) {
        super(context);
        init(null, 0);
    }

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PanelView, defStyle, 0);

        mWindSpeed = a.getString(
                R.styleable.PanelView_windSpeedString);
        mTextColor = a.getColor(
                R.styleable.PanelView_textColor,
                mTextColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextDimension = a.getDimension(
                R.styleable.PanelView_textDimension,
                mTextDimension);

        mAngleDimension = a.getDimension(
                R.styleable.PanelView_angleDimension,
                mAngleDimension);

        mAngle = a.getFloat(R.styleable.PanelView_angle,mAngle);

        if (a.hasValue(R.styleable.PanelView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.PanelView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mWindPaint = new TextPaint();
        mWindPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mWindPaint.setTextAlign(Paint.Align.LEFT);

        mAnglePaint= new TextPaint();
        mAnglePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mAnglePaint.setTextAlign(Paint.Align.LEFT);
        //画圆画笔初始化
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setARGB(255,165,180,222);
        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCirclePaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        mCirclePaint1 = new Paint();
        mCirclePaint1.setAntiAlias(true);
        mCirclePaint1.setARGB(255,255,255,255);
        mCirclePaint1.setStyle(Paint.Style.FILL_AND_STROKE);
        mCirclePaint1.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        mDashPaint = new Paint();
        mDashPaint.setAntiAlias(true);
        mDashPaint.setARGB(255,165,180,222);
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setStrokeWidth(8.0f);
        //指针初始化
        mPointerPaint = new Paint();
        mPointerPaint.setARGB(255,165,180,222);
        mPointerPaint.setStyle(Paint.Style.FILL);

        mPointerPath = new Path();
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mWindPaint.setTextSize(mTextDimension);
        mWindPaint.setColor(mTextColor);

        mAnglePaint.setTextSize(mAngleDimension);
        mAnglePaint.setColor(mTextColor);
        mFontMetrics = mAnglePaint.getFontMetrics();

        mTextWidth = mWindPaint.measureText(mWindSpeed);

        Paint.FontMetrics fontMetrics = mWindPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }
        //画圆
        float radius = contentHeight>contentWidth?contentWidth:contentHeight;
        radius -= 2*(mFontMetrics.bottom-mFontMetrics.top);
        radius = radius/3.0f;
        float pointerL = radius / 1.5f;
        float cx = paddingLeft + (contentWidth) / 2;
        float cy = paddingTop + (contentHeight) / 2;
        canvas.drawCircle(cx, cy, radius, mCirclePaint);
        canvas.drawCircle(cx, cy, radius-20.0f, mCirclePaint1);

        canvas.drawLine(cx-radius,cy,cx-radius-pointerL,cy,mDashPaint);
        canvas.drawLine(cx+radius,cy,cx+radius+pointerL,cy,mDashPaint);
        canvas.drawLine(cx,cy-radius,cx,cy-radius-pointerL,mDashPaint);
        canvas.drawLine(cx,cy+radius,cx,cy+radius+pointerL,mDashPaint);

        canvas.save();
        canvas.translate(cx,cy);
        canvas.rotate(45.0f);
        canvas.drawLine(radius,0.0f,radius+pointerL/2.0f,0.0f,mDashPaint);
        canvas.rotate(90.0f);
        canvas.drawLine(radius,0.0f,radius+pointerL/2.0f,0.0f,mDashPaint);
        canvas.rotate(90.0f);
        canvas.drawLine(radius,0.0f,radius+pointerL/2.0f,0.0f,mDashPaint);
        canvas.rotate(90.0f);
        canvas.drawLine(radius,0.0f,radius+pointerL/2.0f,0.0f,mDashPaint);
        canvas.restore();
        // Draw the text.
        canvas.drawText(mWindSpeed,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + 2*mTextHeight) / 2 ,
                mWindPaint);
        //画指针
        float startX = (float) (radius*Math.cos(Math.toRadians(DEFAULT_POINTER_ANGLE/2.0f)));
        float startY = (float) (radius*Math.sin(Math.toRadians(DEFAULT_POINTER_ANGLE/2.0f)));
        float middleX = radius + pointerL;
        float middleY = 0.0f;
        float endX = startX;
        float endY = -(float) (radius*Math.sin(Math.toRadians(DEFAULT_POINTER_ANGLE/2.0f)));

        canvas.save();
        canvas.translate(cx,cy);
        canvas.rotate(mAngle);
        mPointerPath.moveTo(startX,startY);
        mPointerPath.lineTo(middleX,middleY);
        mPointerPath.lineTo(endX,endY);
        mPointerPath.arcTo(-radius,-radius,
                radius,radius,-15.0f,30.0f,false);
        mPointerPath.close();
        canvas.drawPath(mPointerPath,mPointerPaint);

        // Draw the angle text.
        canvas.save();
        int iAngle = (int)mAngle;
        String strAngle = String.valueOf((iAngle+90)%360) + "°";
        int currentCenterX = (int) (radius + pointerL - dp2px(5) + mAnglePaint.measureText(strAngle) / 2);
        canvas.translate(currentCenterX, 0);
        canvas.rotate(-mAngle);        //坐标系总旋转角度为360度

        int textBaseLine = (int) (0 + (mFontMetrics.bottom - mFontMetrics.top) /2 - mFontMetrics.bottom);

        canvas.drawText(strAngle,
                0,
                textBaseLine ,
                mAnglePaint);
        canvas.restore();
        canvas.restore();
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getWindSpeedString() {
        return mWindSpeed;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setWindSpeedString(String exampleString) {
        mWindSpeed = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mTextColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mTextColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mTextDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mTextDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
