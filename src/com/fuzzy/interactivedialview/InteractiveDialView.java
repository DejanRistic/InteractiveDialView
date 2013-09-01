package com.fuzzy.interactivedialview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InteractiveDialView extends ViewGroup implements OnTouchListener {

	// Static button attributes
	private static final int BUTTON_WIDTH = 120;
	private static final int BUTTON_HEIGHT = 120;
	private static final int BUTTON_OFFSET = 12;

	private static final int DIAL_OFFSET = 50;

	private static final int FULL_CIRCLE_ANGLE = 360;

	private static final float DEFAULT_START_ANGLE = 90.0f;
	private static final float DEFAULT_START_SWEEP_ANGLE = 90.0f;
	private static final float DEFAULT_START_RANGE = 100;

	private Button mDialControl;

	private LinearLayout testView;
	private DialView mDial;
	private Rect mDialLayout;

	int dialX;
	int dialY;

	private float mSweepAngle = DEFAULT_START_SWEEP_ANGLE;
	private float mStartAngle = DEFAULT_START_ANGLE;
	private float mDialRange = DEFAULT_START_RANGE;

	private float mRangeFactor = mDialRange / FULL_CIRCLE_ANGLE;

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

	/**
	 * Any layout manager that doesn't scroll will want this.
	 */
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}

	public void setStartAngle(float startAngle) {
		mStartAngle = startAngle;
	}

	private void init() {
		mDial = new DialView(getContext());
		testView = new LinearLayout(getContext());
		mDialControl = new Button(getContext());

		mDialLayout = new Rect();

		mDialControl.setOnTouchListener(this);

		addView(mDial);
		addView(mDialControl);
		addView(testView);
	}

	private void initDialView() {
		mDialLayout.top = getTop() + DIAL_OFFSET;
		mDialLayout.left = getLeft() + DIAL_OFFSET;
		mDialLayout.right = getRight() - DIAL_OFFSET;
		mDialLayout.bottom = getBottom() - DIAL_OFFSET;

		mDial.layout(mDialLayout.left, mDialLayout.top, mDialLayout.right,
				mDialLayout.bottom);

	}

	private void initDialControl() {
		mDialControl.setTextColor(Color.WHITE);
		mDialControl.setTextSize(10);
		mDialControl.setBackgroundResource(R.drawable.dial_button_shape);
		mDialControl.setHeight(BUTTON_HEIGHT);
		mDialControl.setWidth(BUTTON_WIDTH);

		// Get the radius and center position of our DialView.
		float radius = mDial.getDialRadius();
		float centerX = mDial.getDialCenterX();
		float centerY = mDial.getDialCenterY();

		// Ensure that the position of the dial control button reflects the
		// defined start angle.
		double angleInRadians = Math.toRadians(mStartAngle);
		double x = centerX + radius * Math.cos(angleInRadians);
		double y = centerY + radius * Math.sin(angleInRadians);

		positionDialControl((int) x, (int) y);
	}

	private void positionDialControl(int x, int y) {
		mDialControl.setLeft(x);
		mDialControl.setTop(y);
		mDialControl.setRight(x + BUTTON_WIDTH);
		mDialControl.setBottom(y + BUTTON_HEIGHT);
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

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:

			// Make sure that the rawX and rawY we get is relative to our view
			// and not to the entire screen size. The button width and height is
			// used to make sure the moving button stays in the center of the
			// touching object (finger), otherwise the buttons top left corner
			// would be where the touch is. This would cause a jumping effect
			// (button instantly jumps into a slightly offset position) when a
			// move event is first detected.
			int[] location = new int[2];
			getLocationOnScreen(location);
			x = (event.getRawX() - location[0]) - BUTTON_WIDTH / 2;
			y = (event.getRawY() - location[1]) - BUTTON_HEIGHT / 2;

			// Get the angle so we know where to position the button that
			// controls the dial.
			angle = Math.atan2(y - centerY, x - centerX);

			// Get the points based on our angle that will be used to directly
			// position the button on the circles edge.
			x = centerX + dialRadius * Math.cos(angle);
			y = centerY + dialRadius * Math.sin(angle);

			// Ensure that our sweep angle is always in a 360 degree range. For
			// example if the default start angle is 90 degrees, this means that
			// the sweep angle will always be between 90 and 450.
			mSweepAngle = (float) Math.toDegrees(angle);
			mSweepAngle = mSweepAngle < 0 ? mSweepAngle + 360 : mSweepAngle;
			mSweepAngle = mSweepAngle < mStartAngle ? mSweepAngle + 360
					: mSweepAngle;

			mDialControl.setGravity(Gravity.CENTER);
			mDialControl.setText(String.valueOf(mRangeFactor
					* (mSweepAngle - mStartAngle)));

			// Position our dial control button
			positionDialControl((int) x, (int) y);

			break;
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.v("parent size changed", "parent size changed");
		initDialView();
		initDialControl();
		// testView.setBackgroundResource(R.drawable.inner_view_circle_mask);
		// testView.layout(mDialLayout.left + DIAL_OFFSET + 100, mDialLayout.top
		// + DIAL_OFFSET + 235, mDialLayout.right - DIAL_OFFSET - 100,
		// mDialLayout.bottom - DIAL_OFFSET - 235);
		//
		// Log.v("LEFT:", String.valueOf(testView.getLeft()));
		// Log.v("TOP:", String.valueOf(testView.getTop()));
		// Log.v("RIGHT:", String.valueOf(testView.getRight()));
		// Log.v("BOTTOM:", String.valueOf(testView.getBottom()));

	}

	class DialView extends View {

		private int mViewWidth;
		private int mViewHeight;

		private RectF mBigOval;
		private RectF mSmallOval;
		private RectF mInnerCircleStroke;
		private Point mTextPosition;

		private Paint mOuterStrokePaint;
		private Paint mInnerStrokePaint;
		private Paint mFillPaint;
		private Paint mTextPaint;

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

			mTextPaint = getTextPaint(Color.BLACK);

			mBigOval = new RectF();
			mSmallOval = new RectF();
			mInnerCircleStroke = new RectF();
		}

		private Paint getOuterStrokePaint(int color) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(color);
			paint.setStrokeWidth(10);
			paint.setStyle(Paint.Style.STROKE);
			return paint;
		}

		private Paint getTextPaint(int color) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(color);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(100.0f);

			return paint;
		}

		private Paint getInnerStrokePaint(int color) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(color);
			paint.setStrokeWidth(10);
			paint.setStyle(Paint.Style.STROKE);
			return paint;
		}

		private Paint getFillPaint(int color) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

			// The majority of the work here is to ensure that we have square
			// bounds setup, by doing this we can make a perfect circle and not
			// an oval.

			// Get the max and min.
			int max = Math.max(mViewWidth, mViewHeight);
			int min = Math.min(mViewWidth, mViewHeight);

			// Get orientation.
			int rotation = getDisplay().getRotation();

			// Set proper bounds depending on orientation.
			if (rotation == 0) {
				top = (max - min) / 2;
				bottom = max - top;
			} else {
				left = (max - min) / 2;
				right = max - left;
			}

			// Setup our RectF's that we will use for drawing the dial later.

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

			// Setup text position for default numbers in the middle of the
			// dial.

			// Make sure text is vertically aligned by getting the size of the
			// text and creating an offset.
			Rect bounds = new Rect();
			mTextPaint.getTextBounds("00", 0, 1, bounds);
			int offset = bounds.height() / 2;

			mTextPosition = new Point(
					(int) ((left + INNER_CIRCLE_DIFF + OUTER_CIRCLE_OFFSET) + (mInnerCircleStroke
							.width() / 2)),
					(int) ((top + INNER_CIRCLE_DIFF + OUTER_CIRCLE_OFFSET) + (mInnerCircleStroke
							.height() / 2)) + offset);
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

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			drawArcs(canvas, mBigOval, true, mOuterStrokePaint, false);
			drawArcs(canvas, mBigOval, true, mFillPaint, true);
			drawArcs(canvas, mSmallOval, true, getFillPaint(Color.WHITE), false);
			drawArcs(canvas, mInnerCircleStroke, true, mInnerStrokePaint, false);

			canvas.drawText(
					String.valueOf((int) (mRangeFactor * (mSweepAngle - mStartAngle))),
					mTextPosition.x, mTextPosition.y, mTextPaint);

			invalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}
}
