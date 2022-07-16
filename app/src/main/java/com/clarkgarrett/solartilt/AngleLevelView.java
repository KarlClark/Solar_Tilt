package com.clarkgarrett.solartilt;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class AngleLevelView extends View {
	
	private Paint mLinePaint, mLinePaint2, mBorderPaint , mTextPaint;
	private double mAngle=0, mTargetAngle=0;
	private float mTextSpace, mTextSize, mTextLocation, mTotalHeight, mTotalWidth, mWorkingHeight,mCenterLine;
	private float mLinePaintStrokeWidth=3, mBorderPaintStrokeWidth=10;
	private String mDesiredAngle="0", mCurrentAngle;
	private float mBorder = 15, mSeparation=100, mTopBorder = 3, mBottomBorder =15; //Borders for the text
	private Rect mBounds= new Rect();
	private DecimalFormat mRound1 = new DecimalFormat("#.#");
	private int mColor = getResources().getColor(R.color.blue);
	private int mMergeColor = getResources().getColor(R.color.red);
	private float mMergeAngleTolerance=0.1f;
	
	private static final String TAG ="## My Info ##";
	
	public AngleLevelView(Context context){
		super(context);
		initAngleLevelView();
	}
	
	public AngleLevelView(Context context, AttributeSet attrs) {
		super(context,attrs);
		
		// These custom attributes come form the fragment_solar_tilt_angle_level.xml
		// layout file and the attrs.xml file in the values folder. 
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.AngleLevelView,
		        0, 0);
		
		 try {
		       mTopBorder = a.getFloat(R.styleable.AngleLevelView_topBorder, 3);
		       mBottomBorder = a.getFloat(R.styleable.AngleLevelView_bottomBorder, 20);
		       mColor = a.getInteger(R.styleable.AngleLevelView_anglecolor, getResources().getColor(R.color.blue));
		   } finally {
		       a.recycle();
		   }



		initAngleLevelView();
	}
	
	public AngleLevelView(Context context, AttributeSet attrs,int defaultStyle){
		super(context, attrs,defaultStyle);
		initAngleLevelView();
	}
	
	protected void initAngleLevelView(){
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setColor(mColor);
		mLinePaint.setStrokeWidth(mLinePaintStrokeWidth);
		mLinePaint.setStyle(Paint.Style.STROKE);
		
		mLinePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint2.setColor(mColor);
		mLinePaint2.setStrokeWidth(mLinePaintStrokeWidth);
		mLinePaint2.setStyle(Paint.Style.STROKE);
		
		mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBorderPaint.setColor(mColor);
		mBorderPaint.setStrokeWidth(mBorderPaintStrokeWidth);
		mBorderPaint.setStyle(Paint.Style.STROKE);
		
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setColor(mColor);
		mTextPaint.setStrokeWidth(3);
		
		// Convert pixels to dips.
		mBorder = mBorder * getResources().getDisplayMetrics().density;
		mTopBorder = mTopBorder * getResources().getDisplayMetrics().density;
		mSeparation = mSeparation * getResources().getDisplayMetrics().density;
		mLinePaintStrokeWidth = mLinePaintStrokeWidth * getResources().getDisplayMetrics().density;
		mBorderPaintStrokeWidth = mBorderPaintStrokeWidth * getResources().getDisplayMetrics().density;
	}
	
	public void setAngle(double angle){
		// The angle the device is currently oriented at,
		// so redraw our moving line.
		mAngle = angle;
		invalidate();
		requestLayout();
	}
	
	public void setTargetAngle(double targetAngle){
		// The angle where the lines should merge.
		mTargetAngle = targetAngle;
		mDesiredAngle =("Desired Angle= " + mRound1.format(Math.abs(mTargetAngle)) +  "\u00b0");
	}
	
	public void setColor(int color){
		mLinePaint.setColor(color);
		mBorderPaint.setColor(color);
		mTextPaint.setColor(color);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Save the width and height of our view and set the measured dimension
		// to the maximum value.
		mTotalWidth = measure(widthMeasureSpec);
		mTotalHeight = measure(heightMeasureSpec);
		
		setMeasuredDimension((int)mTotalWidth,(int)mTotalHeight);
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		
		// Calculate text size that will fit on the screen.
		mTextSize=mTotalHeight/10f;  // Starting guess for a good text size.
		mTextPaint.setTextSize(mTextSize);
		// The following call returns the smallest Rect that can contain the text in mBounds.
		mTextPaint.getTextBounds(mDesiredAngle, 0, mDesiredAngle.length()-1, mBounds);
		float textLength = 2*mBounds.width() + 2*mBorder + mSeparation; // Length for 2 strings etc.
		// Run a loop reducing the text size until it will fit on the screen.
		while (textLength > mTotalWidth ){
			mTextPaint.setTextSize(mTextSize--);
			mTextPaint.getTextBounds(mDesiredAngle, 0, mDesiredAngle.length()-1, mBounds);
			textLength = 2*mBounds.width() + 2*mBorder + mSeparation;
			if (mTextSize < 10){
				break;
			}
		}
		// Now that we know how big the text is, we know how big we
		// can make everything else.
		mTextSpace = mBounds.height()+ mTopBorder + mBottomBorder;
		mWorkingHeight = mTotalHeight-mTextSpace;
		mCenterLine=mWorkingHeight/2  + mTextSpace-mLinePaintStrokeWidth;
		mTextLocation = mTextSize+mTopBorder; 

		//Draw a frame around the entire view, and a line under the text.
		canvas.drawRect(0, 0, mTotalWidth-1, mTotalHeight-1, mBorderPaint);
		canvas.drawLine(0,mTextSpace,mTotalWidth,mTextSpace,mLinePaint2);
		
		// Draw the text
		canvas.drawText(mDesiredAngle,mBorder , mTextLocation, mTextPaint);
		mCurrentAngle=("Current Angle= " + mRound1.format(Math.abs(mAngle)) +  "\u00b0");
		canvas.drawText(mCurrentAngle, mBounds.width()+mSeparation , mTextLocation, mTextPaint);
		
		// Depending on the orientation of the the phone, the pivot point could be on either end
		// of the phone, therefore the the if/else statement to handle either condition. 
		if (mAngle >= 0){
			double deg = mAngle-Math.abs(mTargetAngle);
			float x2 =  mTotalWidth*(float)Math.cos(Math.toRadians(deg));
			float y2 = (-mTotalWidth*(float)Math.sin(Math.toRadians(deg)))+mCenterLine;
			
			// Make sure the moving line doesn't cross the line underlining the text.
			if (y2 <= mTextSpace){
				x2 =(mCenterLine-mTextSpace)/((float)Math.tan(Math.toRadians(deg)));
				y2=mTextSpace;
			}
			
			//When the line merge, change the color of the lines.
			if (deg > mMergeAngleTolerance || deg < -mMergeAngleTolerance){
				mLinePaint.setColor(mColor);
			}else{
				mLinePaint.setColor(mMergeColor);
			}
			canvas.drawLine(0, mCenterLine, x2 , y2 , mLinePaint);
			canvas.drawLine(0, mCenterLine, mTotalWidth, mCenterLine, mLinePaint);
		}else{
			double deg = mAngle+ Math.abs(mTargetAngle);
			
			float x2 = mTotalWidth - mTotalWidth*(float)Math.cos(Math.toRadians(deg));
			float y2 = (mTotalWidth*(float)Math.sin(Math.toRadians(deg)))+mCenterLine;
			if(y2 <= mTextSpace){
				x2 =mTotalWidth + ((mCenterLine-mTextSpace)/((float)Math.tan(Math.toRadians(deg))));
				y2=mTextSpace;
			}
			if (deg > mMergeAngleTolerance || deg < -mMergeAngleTolerance){
				mLinePaint.setColor(mColor);
			}else{
				mLinePaint.setColor(mMergeColor);
			}
			canvas.drawLine(mTotalWidth, mCenterLine,x2 , y2 , mLinePaint);
			canvas.drawLine(0, mCenterLine, mTotalWidth, mCenterLine, mLinePaint);
		}
	}
	
	private int measure(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED){
			result = 200;
		}else {
			result=specSize;
		}
		return result;
	}	

}
