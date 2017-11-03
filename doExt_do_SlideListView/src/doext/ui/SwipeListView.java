package doext.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import core.interfaces.DoIUIModuleView;

@SuppressLint({ "ClickableViewAccessibility", "Recycle" })
public class SwipeListView extends ListView {
	private Boolean mIsHorizontal;
	private View mPreItemView;
	private View mCurrentItemView;
	private float mFirstX;
	private float mFirstY;
	private int mRightViewWidth;
	private int mLeftViewWidth;
	// private boolean mIsInAnimation = false;
	private final int mDuration = 100;
	private final int mDurationStep = 10;
	private boolean mIsShown;
	//如果为true标示子view没有消费掉事件，隐藏菜单，不触发doListView_Touch事件
	private boolean mTouchEvent = false;
	/**
	 * 是否允许footer or Header Swipe
	 */
	private boolean mIsFooterCanSwipe = false;
	private boolean mIsHeaderCanSwipe = false;

	/** 禁止侧滑模式 */
	public static int MOD_FORBID = 0;
	public static int MOD_LEFT = 1;
	public static int MOD_RIGHT = 2;
	/** 当前的模式 */
	private int mode = MOD_FORBID;
	public static final int LEFT_MENU_VIEW = 0x00000001;
	public static final int RIGHT_MENU_VIEW = 0x00000002;
	public static final int CONTENT_VIEW = 0x00000003;

	public SwipeListView(Context context) {
		super(context);
	}

	public SwipeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SwipeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * return true, deliver to listView. return false, deliver to child. if
	 * move, return true
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float lastX = ev.getX();
		float lastY = ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsHorizontal = null;
			mFirstX = lastX;
			mFirstY = lastY;
			int motionPosition = pointToPosition((int) mFirstX, (int) mFirstY);
			if (motionPosition >= 0) {
				mTouchEvent = false;
				View currentItemView = getChildAt(motionPosition - getFirstVisiblePosition());
				mPreItemView = mCurrentItemView;
				mCurrentItemView = currentItemView;
				DoIUIModuleView leftModuleView = (DoIUIModuleView) currentItemView.findViewById(LEFT_MENU_VIEW);
				DoIUIModuleView rightModuleView = (DoIUIModuleView) currentItemView.findViewById(RIGHT_MENU_VIEW);
				mRightViewWidth = null != rightModuleView ? (int) rightModuleView.getModel().getRealWidth() : 0;
				mLeftViewWidth = null != leftModuleView ? (int) leftModuleView.getModel().getRealWidth() : 0;
			}
			break;

		case MotionEvent.ACTION_MOVE:
			float dx = lastX - mFirstX;
			float dy = lastY - mFirstY;
			if (Math.abs(dx) >= 3 && Math.abs(dy) >= 3) {
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mIsShown && (mPreItemView != mCurrentItemView || isHitCurItemLeft(lastX) || isHitCurItemRight(lastX))) {
				if (mode == MOD_LEFT) {
					hiddenRight(mPreItemView, false);
				}
				if (mode == MOD_RIGHT) {
					hiddenLeft(mPreItemView);
				}
			}
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	private boolean isHitCurItemLeft(float x) {
		return x < getWidth() - mLeftViewWidth;
	}

	private boolean isHitCurItemRight(float x) {
		return x > getWidth() - mRightViewWidth;
	}

	/**
	 * @param dx
	 * @param dy
	 * @return judge if can judge scroll direction
	 */
	private boolean judgeScrollDirection(float dx, float dy) {
		boolean canJudge = true;

		if (Math.abs(dx) > 30 && Math.abs(dx) > 2 * Math.abs(dy)) {
			mIsHorizontal = true;
		} else if (Math.abs(dy) > 30 && Math.abs(dy) > 2 * Math.abs(dx)) {
			mIsHorizontal = false;
		} else {
			canJudge = false;
		}

		return canJudge;
	}

	/**
	 * @param posX
	 * @param posY
	 * @return judge if can footer judge
	 */
	private boolean judgeFooterView(float posX, float posY) {
		// if footer can swipe
		if (mIsFooterCanSwipe) {
			return true;
		}
		// footer cannot swipe
		int selectPos = pointToPosition((int) posX, (int) posY);
		if (selectPos >= (getCount() - getFooterViewsCount())) {
			// is footer ,can not swipe
			return false;
		}
		// not footer can swipe
		return true;
	}

	/**
	 * @param posX
	 * @param posY
	 * @return judge if can judge scroll direction
	 */
	private boolean judgeHeaderView(float posX, float posY) {
		// if header can swipe
		if (mIsHeaderCanSwipe) {
			return true;
		}
		// header cannot swipe
		int selectPos = pointToPosition((int) posX, (int) posY);
		if (selectPos >= 0 && selectPos < getHeaderViewsCount()) {
			// is header ,can not swipe
			return false;
		}
		// not header can swipe
		return true;
	}

	/**
	 * return false, can't move any direction. return true, cant't move
	 * vertical, can move horizontal. return super.onTouchEvent(ev), can move
	 * both.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		float lastX = ev.getX();
		float lastY = ev.getY();
		// test footer and header
		if (!judgeFooterView(mFirstX, mFirstY) || !judgeHeaderView(mFirstX, mFirstY)) {
			return super.onTouchEvent(ev);
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;

		case MotionEvent.ACTION_MOVE:
			float dx = lastX - mFirstX;
			float dy = lastY - mFirstY;
			if (Math.abs(dx) >= 3 && Math.abs(dy) >= 3) {
				if (dx < 0 && mRightViewWidth > 0) {//向左滑动
					mode = MOD_LEFT;
				}
				if (dx > 0 && mLeftViewWidth > 0) {//向右滑动
					mode = MOD_RIGHT;
				}
			}
			// confirm is scroll direction
			if (mIsHorizontal == null) {
				if (!judgeScrollDirection(dx, dy)) {
					break;
				}
			}
			if (mIsHorizontal) {
				if (mIsShown && mPreItemView != mCurrentItemView) {
					/**
					 * 情况二：
					 * <p>
					 * 一个Item的右边布局已经显示，
					 * <p>
					 * 这时候左右滑动另外一个item,那个右边布局显示的item隐藏其右边布局
					 * <p>
					 * 向左滑动只触发该情况，向右滑动还会触发情况五
					 */
					if (mode == MOD_LEFT) {
						hiddenRight(mPreItemView, true);
					}
					if (mode == MOD_RIGHT) {
						hiddenLeft(mPreItemView);
					}
				}

				if (mode == MOD_LEFT) {
					//向左滑动
					if (mIsShown && mPreItemView == mCurrentItemView) {
						dx = dx - mRightViewWidth;
					}

					// can't move beyond boundary
					if (dx < 0 && dx > -mRightViewWidth && null != mCurrentItemView) {
						mCurrentItemView.scrollTo((int) (-dx), 0);
					}
				}
				if (mode == MOD_RIGHT) {
					//向右滑动
					if (mIsShown && mPreItemView == mCurrentItemView) {
						dx = dx + mLeftViewWidth;
					}
					if (dx > 0 && dx < mLeftViewWidth && null != mCurrentItemView) {
						mCurrentItemView.scrollTo((int) (-dx), 0);
					}
				}
				return true;
			} else {
				if (mIsShown) {
					/**
					 * 情况三：
					 * <p>
					 * 一个Item的右边布局已经显示，
					 * <p>
					 * 这时候上下滚动ListView,那么那个右边布局显示的item隐藏其右边布局
					 */
					if (mode == MOD_LEFT) {
						hiddenRight(mPreItemView, false);
					}
					if (mode == MOD_RIGHT) {
						hiddenLeft(mPreItemView);
					}
				}
			}

			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (null != mCurrentItemView) {
				clearPressedState();
			}
			if (mIsShown) {
				/**
				 * 情况四：
				 * <p>
				 * 一个Item的右边布局已经显示，
				 * <p>
				 * 这时候左右滑动当前一个item,那个右边布局显示的item隐藏其右边布局
				 */
				if (mode == MOD_LEFT) {
					hiddenRight(mPreItemView, true);
				}
				if (mode == MOD_RIGHT) {
					hiddenLeft(mPreItemView);
				}
				mTouchEvent = true;
			}

			if (mIsHorizontal != null && mIsHorizontal) {
				if (mode == MOD_LEFT) {
					//向左滑动
					if (mFirstX - lastX > mRightViewWidth / 2) {
						showRight(mCurrentItemView);
					} else {
						/**
						 * 情况五：
						 * <p>
						 * 向右滑动一个item,且滑动的距离超过了右边View的宽度的一半，隐藏之。
						 */
						hiddenRight(mCurrentItemView, true);
					}
				}
				if (mode == MOD_RIGHT) {
					//向右滑动
					if (mFirstX - lastX < (mLeftViewWidth / 2) * -1) {
						showLeft(mCurrentItemView);
					} else {
						/**
						 * 情况五：
						 * <p>
						 * 向右滑动一个item,且滑动的距离超过了右边View的宽度的一半，隐藏之。
						 */
						hiddenLeft(mCurrentItemView);
					}
				}
				MotionEvent obtain = MotionEvent.obtain(ev);
				obtain.setAction(MotionEvent.ACTION_CANCEL);
				super.onTouchEvent(obtain);
				return true;
			}

			break;
		}

		return super.onTouchEvent(ev);
	}

	private void clearPressedState() {
		// TODO current item is still has background, issue
		mCurrentItemView.setPressed(false);
		setPressed(false);
		setFocusable(false);
		refreshDrawableState();
		// invalidate();
	}

	private MoveHandler mHandler;

	private void showRight(View view) {
		mHandler = new MoveHandler();
		Message msg = mHandler.obtainMessage();
		msg.obj = view;
		msg.arg1 = view.getScrollX();
		msg.arg2 = mRightViewWidth;
		msg.sendToTarget();
		mIsShown = true;
	}

	private void showLeft(View view) {
		mHandler = new MoveHandler();
		Message msg = mHandler.obtainMessage();
		msg.obj = view;
		msg.arg1 = view.getScrollX();
		msg.arg2 = -mLeftViewWidth;
		msg.sendToTarget();

		mIsShown = true;
	}

	private void hiddenRight(View view, boolean isAnimation) {
		if (mCurrentItemView == null || view == null) {
			return;
		}
		if (isAnimation) {
			mHandler = new MoveHandler();
			Message msg = mHandler.obtainMessage();
			msg.obj = view;
			msg.arg1 = view.getScrollX();
			msg.arg2 = 0;
			msg.sendToTarget();
		} else {
			//先关闭showView的动画
			mHandler.cancel();
			//直接隐藏，不需要执行动画
			view.scrollTo(0, 0);
		}
		mode = MOD_FORBID;
		mIsShown = false;
	}

	private void hiddenLeft(View view) {
		if (mCurrentItemView == null || view == null) {
			return;
		}
		mHandler = new MoveHandler();
		Message msg = mHandler.obtainMessage();
		msg.obj = view;
		msg.arg1 = view.getScrollX();
		msg.arg2 = 0;

		msg.sendToTarget();
		mode = MOD_FORBID;
		mIsShown = false;
	}

	public boolean isShowMenu() {
		if (mode != MOD_FORBID && mIsShown) {
			return true;
		}
		return false;
	}

	/**
	 * show or hide right layout animation
	 */
	@SuppressLint("HandlerLeak")
	private class MoveHandler extends Handler {
		int stepX = 0;
		int fromX;
		int toX;
		View view;
		private boolean mIsInAnimation = false;

		private void animatioOver() {
			mIsInAnimation = false;
			stepX = 0;
		}

		public void cancel() {
			this.removeMessages(0);
			//如果发现已经调用hideView 了，那就直接关掉
			animatioOver();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (stepX == 0) {
				if (mIsInAnimation) {
					return;
				}
				mIsInAnimation = true;
				view = (View) msg.obj;
				fromX = msg.arg1;
				toX = msg.arg2;
				stepX = (int) ((toX - fromX) * mDurationStep * 1.0 / mDuration);
				if (stepX < 0 && stepX > -1) {
					stepX = -1;
				} else if (stepX > 0 && stepX < 1) {
					stepX = 1;
				}
				if (Math.abs(toX - fromX) < 10) {
					view.scrollTo(toX, 0);
					animatioOver();
					return;
				}
			}

			fromX += stepX;
			boolean isLastStep = (stepX > 0 && fromX > toX) || (stepX < 0 && fromX < toX);
			if (isLastStep) {
				fromX = toX;
			}

			view.scrollTo(fromX, 0);
			invalidate();

			if (!isLastStep) {
				this.sendEmptyMessageDelayed(0, mDurationStep);
			} else {
				animatioOver();
			}
		}

	}

	public int getRightViewWidth() {
		return mRightViewWidth;
	}

	public void setRightViewWidth(int mRightViewWidth) {
		this.mRightViewWidth = mRightViewWidth;
	}

	public int getLeftViewWidth() {
		return mLeftViewWidth;
	}

	public void setLeftViewWidth(int mLeftViewWidth) {
		this.mLeftViewWidth = mLeftViewWidth;
	}

	/**
	 * 设置list的脚是否能够swipe
	 * 
	 * @param canSwipe
	 */
	public void setFooterViewCanSwipe(boolean canSwipe) {
		mIsFooterCanSwipe = canSwipe;
	}

	/**
	 * 设置list的头是否能够swipe
	 * 
	 * @param canSwipe
	 */
	public void setHeaderViewCanSwipe(boolean canSwipe) {
		mIsHeaderCanSwipe = canSwipe;
	}

	/**
	 * 设置 footer and header can swipe
	 * 
	 * @param footer
	 * @param header
	 */
	public void setFooterAndHeaderCanSwipe(boolean footer, boolean header) {
		mIsHeaderCanSwipe = header;
		mIsFooterCanSwipe = footer;
	}

	public boolean isTouchEvent() {
		return mTouchEvent;
	}

}
