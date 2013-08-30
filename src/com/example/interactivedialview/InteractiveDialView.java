package com.example.interactivedialview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;

public class InteractiveDialView extends ViewGroup implements OnTouchListener {

	// Static button attributes
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_HEIGHT = 100;
	private static final int BUTTON_OFFSET = 10;

	private static final int DIAL_OFFSET = 50;

	private static final float DEFAULT_START_ANGLE = 90.0f;
	private static final float DEFAULT_START_SWEEP_ANGLE = 90.0f;

	private Button mButton;

	private DialView mDial;
	private Rect mDialLayout;

	int dialX;
	int dialY;

	private float mSweepAngle = DEFAULT_START_SWEEP_ANGLE;
	private float mStartAngle = DEFAULT_START_ANGLE;

	public InteractiveDialView(Context context) {
		super(context);
		init();

	}

	public InteractiveDialView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public InteractiveDialView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setStartAngle(float startAngle) {
		mStartAngle = startAngle;
	}

	private void init() {
		mDial = new DialView(getContext());

		mButton = new Button(getContext());

		mDialLayout = new Rect();

		mButton.setOnTouchListener(this);

		addView(mDial);
		addView(mButton);
	}

	private void initDialView() {
		mDialLayout.top = getTop() + DIAL_OFFSET;
		mDialLayout.left = getLeft() + DIAL_OFFSET;
		mDialLayout.right = getRight() - DIAL_OFFSET;
		mDialLayout.bottom = getBottom() - DIAL_OFFSET;

		mDial.layout(mDialLayout.left, mDialLayout.top, mDialLayout.right,
				mDialLayout.bottom);

	}

	private void initButtonControl() {
		mButton.setTextColor(Color.WHITE);
		mButton.setTextSize(10);
		mButton.setBackgroundResource(R.drawable.dial_button_shape);
		// mButton.setBackgroundColor(Color.BLACK);
		mButton.setHeight(BUTTON_HEIGHT);
		mButton.setWidth(BUTTON_WIDTH);

		float radius = mDial.getDialRadius();
		float centerX = mDial.getDialCenterX();
		float centerY = mDial.getDialCenterY();

		double angleInRadians = Math.toRadians(mStartAngle);

		double left = centerX + radius * Math.cos(angleInRadians);
		double top = centerY + radius * Math.sin(angleInRadians);

		mButton.setLeft((int) left);
		mButton.setTop((int) top);
		mButton.setRight((int) left + BUTTON_WIDTH);
		mButton.setBottom((int) top + BUTTON_HEIGHT);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		// Get the center of the DialView
		double centerX = mDial.getDialCenterX();
		double centerY = mDial.getDialCenterY();
		float dialRadius = mDial.getDialRadius();

		double angle = 0;
		double x = 0;
		double y = 0;

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			angle = Math.atan2(event.getRawY() - centerY, event.getRawX()
					- centerX);

			x = centerX + dialRadius * Math.cos(angle);
			y = centerY + dialRadius * Math.sin(angle);

			mSweepAngle = (float) Math.toDegrees(angle);
			mSweepAngle = mSweepAngle < 0 ? mSweepAngle + 360 : mSweepAngle;
			mSweepAngle = mSweepAngle < mStartAngle ? mSweepAngle + 360
					: mSweepAngle;

			v.setX((float) x);
			v.setY((float) y);

			mButton.setText(String.valueOf(mSweepAngle));

			break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawPoint(mButton.getX(), mButton.getY(), new Paint(Color.BLACK));
		invalidate();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.v("onMeasure", "onMeasure");
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.v("parent size changed", "parent size changed");
		initDialView();
		initButtonControl();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
	}

	class DialView extends View {

		private int mViewWidth;
		private int mViewHeight;
		private RectF mBigOval;
		private RectF mSmallOval;
		private RectF mInnerCircleStroke;

		private Paint mOuterStrokePaint;
		private Paint mInnerStrokePaint;
		private Paint mFillPaint;

		private static final float INNER_CIRCLE_DIFF = 100;
		private static final float OUTER_CIRCLE_OFFSET = BUTTON_OFFSET;

		public DialView(Context context) {
			super(context);
			init(context);
		}

		public DialView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init(context);
		}

		public DialView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init(context);
		}

		public float getDialCenterX() {
			return mBigOval.centerX();
		}

		public float getDialCenterY() {
			return mBigOval.centerY();
		}

		public float getDialRadius() {
			return mBigOval.centerX() - mBigOval.left;
		}

		private void init(Context context) {
			Resources res = getResources();

			int color = res.getColor(R.color.oraange_fill);
			mFillPaint = getFillPaint(color);
			mOuterStrokePaint = getOuterStrokePaint(color);

			color = res.getColor(R.color.custom_gray);
			mInnerStrokePaint = getInnerStrokePaint(color);

			mBigOval = new RectF();
			mSmallOval = new RectF();
			mInnerCircleStroke = new RectF();
		}

		private Paint getOuterStrokePaint(int color) {
			Paint paint = new Paint();
			paint.setColor(color);
			paint.setStrokeWidth(10);
			paint.setStyle(Paint.Style.STROKE);
			return paint;
		}

		private Paint getInnerStrokePaint(int color) {
			Paint paint = new Paint();
			paint.setColor(color);
			paint.setStrokeWidth(10);
			paint.setStyle(Paint.Style.STROKE);
			return paint;
		}

		private Paint getFillPaint(int color) {
			Paint paint = new Paint();
			paint.setColor(color);
			paint.setStrokeWidth(10);
			paint.setStyle(Paint.Style.FILL);
			return paint;
		}

		private void drawArcs(Canvas canvas, RectF oval, boolean useCenter,
				Paint paint, boolean animated) {
			if (animated) {
				float angleDelta = mSweepAngle - mStartAngle;
				canvas.drawArc(oval, mStartAngle, angleDelta, useCenter, paint);
			} else {
				canvas.drawArc(oval, 0, 360.0f, true, paint);
			}
		}

		private void initDialSize() {
			float left = 0, top = 0, right = mViewWidth, bottom = mViewHeight;

			Log.v("Width D", String.valueOf(mViewWidth));
			Log.v("Height D", String.valueOf(mViewHeight));

			int max = Math.max(mViewWidth, mViewHeight);
			int min = Math.min(mViewWidth, mViewHeight);

			int rotation = getDisplay().getRotation();

			if (rotation == 0) {
				top = (max - min) / 2;
				bottom = max - top;
			} else {
				left = (max - min) / 2;
				right = max - left;
			}
			mBigOval.set(left + OUTER_CIRCLE_OFFSET, top + OUTER_CIRCLE_OFFSET,
					right - OUTER_CIRCLE_OFFSET, bottom - OUTER_CIRCLE_OFFSET);

			mSmallOval.set(left + INNER_CIRCLE_DIFF + OUTER_CIRCLE_OFFSET, top
					+ INNER_CIRCLE_DIFF + OUTER_CIRCLE_OFFSET, right
					- INNER_CIRCLE_DIFF - OUTER_CIRCLE_OFFSET, bottom
					- INNER_CIRCLE_DIFF - OUTER_CIRCLE_OFFSET);

			mInnerCircleStroke.set(left + INNER_CIRCLE_DIFF
					+ OUTER_CIRCLE_OFFSET, top + INNER_CIRCLE_DIFF
					+ OUTER_CIRCLE_OFFSET, right - INNER_CIRCLE_DIFF
					- OUTER_CIRCLE_OFFSET, bottom - INNER_CIRCLE_DIFF
					- OUTER_CIRCLE_OFFSET);
		}

		@Override
		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			Log.v("dialView on size", "dialView on size");
			mViewWidth = w;
			mViewHeight = h;
			initDialSize();
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			Log.v("dialView on measure", "dialView on measure");
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			drawArcs(canvas, mBigOval, true, mOuterStrokePaint, false);
			drawArcs(canvas, mBigOval, true, mFillPaint, true);
			drawArcs(canvas, mSmallOval, true, getFillPaint(Color.WHITE), false);
			drawArcs(canvas, mInnerCircleStroke, true, mInnerStrokePaint, false);

			invalidate();
		}
	}
}
