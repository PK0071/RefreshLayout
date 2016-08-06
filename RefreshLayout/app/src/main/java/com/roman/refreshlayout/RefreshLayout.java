package com.roman.refreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;


/**
 * Created by roman
 * On 2016/8/5.
 */
public class RefreshLayout extends ViewGroup implements GestureDetector.OnGestureListener {

    private static final String TAG = "RefreshLayout";
    private static final int OFFSET_Y = 5;//使用scrollTo时Y每次变化的常量

    private LayoutInflater mInflater;
    private View mHeaderView;//下拉刷新头部View
    private View mContentView;//包裹的列表View
    private ListView mListView;
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private TextView mTextView;//刷新文字

    private boolean isFirstItem = false;//是否滚动第一条
    private boolean isFirst = true;//是否是第一次进来
    private boolean isRefreshing = false;//是否正在刷新
    private int mCnt = 0;
    private int mDownY;
    private int mCurrentY;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mGestureDetector = new GestureDetector(getContext(), this);
        mScroller = new Scroller(getContext());

        mInflater = LayoutInflater.from(getContext());
        mHeaderView = mInflater.inflate(R.layout.view_refresh_head, null);
        mTextView = (TextView) mHeaderView.findViewById(R.id.refresh_tv);
        addView(mHeaderView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mContentView == null) {
            mContentView = this.getChildAt(1);
            mListView = (ListView) mContentView;

            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem == 0) {
                        isFirstItem = true;
                    } else if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (isRefreshing) {
                            scrollTo(0, mHeaderView.getMeasuredHeight());
                        }
                    } else {
                        if (isRefreshing) {
                            scrollTo(0, mHeaderView.getMeasuredHeight());
                        }
                        isFirstItem = false;
                    }
                }
            });
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);//测量子view
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mCnt = mHeaderView.getMeasuredHeight();

        mHeaderView.layout(0, 0, mHeaderView.getMeasuredWidth(), mHeaderView.getMeasuredHeight());//在最上面
        mContentView.layout(0, mHeaderView.getMeasuredHeight(), mContentView.getMeasuredWidth(),
                mHeaderView.getMeasuredHeight()  + mContentView.getMeasuredHeight());
        scrollTo(0, mHeaderView.getMeasuredHeight());//第一次
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercepet = false;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentY = (int) ev.getY();
                if (isTop(mCurrentY - mDownY)) {//到顶的判断,传是上滑还是下滑进去
                    isIntercepet = true;//拦截
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        if (isIntercepet) {
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);//默认false
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getScrollY() >  30) {//自动弹回去
                scrollTo(0, mHeaderView.getMeasuredHeight());
                return super.onTouchEvent(event);
            }
        }

        return mGestureDetector.onTouchEvent(event);
    }

    private boolean isTop(int distance) {
        if (distance > 0) {//下拉
            if (isFirstItem) {
                return true;
            }
        }
        return false;
    }


    //----------------------GestureDetector-----------------------------
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
        //1.使用ScrollTo利用常量来产生滑动效果,最简单最暴力的是直接scrollTo(0, 0)，这样很生硬
        //所以下面用一个常量来进行平滑效果模拟，当拉到一半松手自动收回来，差不多拉到尽头，自动滑到尽头
        if (mCnt >= 0 && distanceY < 0) {//下拉
            if (getScrollY() <= 30) {//自动拉出来
                scrollTo(0, 0);
                refreshing();
            } else {//在拉
                scrollTo(0, mCnt -= OFFSET_Y);
            }
        } else if (distanceY > 0) {//下拉过程中上滑
            scrollTo(0, mHeaderView.getMeasuredHeight());
        }else {
            refreshing();
        }

        //2.使用scroller做平缓滑动
//        if (!isRefreshing) {
//            mScroller.startScroll(0, getScrollY(), 0, -mHeaderView.getMeasuredHeight() , 2000);
//            refreshing();
//            invalidate();
//        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    //----------------------GestureDetector-----------------------------


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     * 正在刷新，已经3秒后恢复状态
     */
    private void refreshing() {
        isRefreshing = true;
        mTextView.setText("正在刷新...");
        mHeaderView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollTo(0, mHeaderView.getMeasuredHeight());
                mCnt = mHeaderView.getMeasuredHeight();
                mTextView.setText("下拉刷新");
                isRefreshing = false;
            }
        }, 3000);
    }

}
