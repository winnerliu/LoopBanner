package com.kad.banner.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kad.banner.R;
import com.kad.banner.adapter.RecyclePagerAdapter;
import com.kad.banner.config.BannerConfig;
import com.kad.banner.entity.AbstractPagerData;
import com.kad.banner.listener.OnBannerClickListener;

import static android.support.v4.view.ViewPager.OnPageChangeListener;

import java.lang.reflect.Field;
import java.util.ArrayList;


/**
 * Created by Winner on 2016/11/5.
 */

public class LoopBanner extends FrameLayout implements OnPageChangeListener {
    private static String TAG = "loopBanner";
    private int mIndicatorMargin = BannerConfig.MAIGIN_SIZE;
    private int mIndicatorWidth = BannerConfig.INDICATOR_SIZE;
    private int mIndicatorHeight = BannerConfig.INDICATOR_SIZE;
    private int mIndicatorSelectedResId = R.drawable.loopbanner_default_gray_radius;
    private int mIndicatorUnselectedResId = R.drawable.loopbanner_default_white_radius;
    private int delayTime = BannerConfig.TIME;
    private boolean isAutoPlay = BannerConfig.IS_AUTO_PLAY;
    private int gravity = BannerConfig.CENTER;
    private boolean IsShowIndicatorIfOnlyOne = true;

    private LoopViewPager mViewPager;
    private LinearLayout mIndicatorRoot;
    private BannerScroller mScroller;
    private int currentItem = 1;
    private LoopPagerAdapter adapter;
    private OnPageChangeListener mOnPageChangeListener;
    private int count = 0;
    private OnBannerClickListener mListener;
    private ArrayList<? extends AbstractPagerData> mDataList = new ArrayList<>();
    private ArrayList<ImageView> mIndicatorImageViews = new ArrayList<>();
    private Handler handler = new Handler();
    private int lastPosition = 1;
    private boolean isAttachedToWindow = false;


    public LoopBanner(Context context) {
        super(context);
        initView(context, null);
    }

    public LoopBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public LoopBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    private void initView(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_loopbanner, this, true);
        mViewPager = (LoopViewPager) view.findViewById(R.id.viewpager);
        mIndicatorRoot = (LinearLayout) view.findViewById(R.id.indicator_root);
        handleTypedArray(context, attrs);
        initViewPagerScroll();
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoopBanner);
        mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.LoopBanner_indicator_width, BannerConfig.INDICATOR_SIZE);
        mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.LoopBanner_indicator_height, BannerConfig.INDICATOR_SIZE);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.LoopBanner_indicator_margin, BannerConfig.MAIGIN_SIZE);
        mIndicatorSelectedResId = typedArray.getResourceId(R.styleable.LoopBanner_indicator_drawable_selected, R.drawable.loopbanner_default_gray_radius);
        mIndicatorUnselectedResId = typedArray.getResourceId(R.styleable.LoopBanner_indicator_drawable_unselected, R.drawable.loopbanner_default_white_radius);
        delayTime = typedArray.getInt(R.styleable.LoopBanner_delayTime, BannerConfig.TIME);
        isAutoPlay = typedArray.getBoolean(R.styleable.LoopBanner_isAutoPlay, BannerConfig.IS_AUTO_PLAY);
        gravity = typedArray.getInt(R.styleable.LoopBanner_indicator_gravity, BannerConfig.CENTER);
        typedArray.recycle();
    }

    private void initViewPagerScroll() {
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            mScroller = new BannerScroller(mViewPager.getContext());
            mField.set(mViewPager, mScroller);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public LoopBanner isAutoPlay(boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
        return this;
    }

    public LoopBanner setDelayTime(int delayTime) {
        this.delayTime = delayTime;
        return this;
    }

    public LoopBanner setIndicatorGravity(int type) {
        switch (type) {
            case BannerConfig.LEFT:
                this.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                break;
            case BannerConfig.CENTER:
                this.gravity = Gravity.CENTER;
                break;
            case BannerConfig.RIGHT:
                this.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                break;
        }
        return this;
    }


    public void notifyData(ArrayList<? extends AbstractPagerData> dataLists) {
        if (dataLists == null || dataLists.size() <= 0) {
            Log.e(TAG, "Please set the datas.");
            return;
        }
        count = dataLists.size();
        mDataList = dataLists;
        setData();
        createIndicator();
        if (isAutoPlay)
            startAutoPlay();
    }


    private void createIndicator() {
        mIndicatorImageViews.clear();
        mIndicatorRoot.removeAllViews();
        if (count <= 1 && IsShowIndicatorIfOnlyOne) {
            mIndicatorRoot.setVisibility(View.GONE);
        } else {
            mIndicatorRoot.setVisibility(View.VISIBLE);
        }

        setIndicatorGravity(gravity);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
            params.leftMargin = mIndicatorMargin;
            params.rightMargin = mIndicatorMargin;
            if (i == 0) {
                imageView.setImageResource(mIndicatorSelectedResId);
            } else {
                imageView.setImageResource(mIndicatorUnselectedResId);
            }
            mIndicatorImageViews.add(imageView);
            mIndicatorRoot.addView(imageView, params);
        }
        if (gravity != -1)
            mIndicatorRoot.setGravity(gravity);
    }

    private void setData() {
        currentItem = 1;
        if (adapter == null) {
            adapter = new LoopPagerAdapter();
            adapter.setOnBannerClickListener(mListener);
            mViewPager.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        mViewPager.setFocusable(true);
        mViewPager.setCurrentItem(1);
        mViewPager.addOnPageChangeListener(this);
        if (count <= 1)
            mViewPager.setScrollable(false);
        else
            mViewPager.setScrollable(true);
    }

    public void startAutoPlay() {
        handler.removeCallbacks(task);
        if (isAttachedToWindow && mDataList != null && mDataList.size() > 0) {
            handler.postDelayed(task, delayTime);
        }
    }

    public void stopAutoPlay() {
        handler.removeCallbacks(task);
    }

    private final Runnable task = new Runnable() {

        @Override
        public void run() {
            if (count > 1 && isAutoPlay) {
                currentItem = currentItem % (count + 1) + 1;
                if (currentItem == 1) {
                    mViewPager.setCurrentItem(currentItem, false);
                    handler.postDelayed(task, delayTime);
                } else if (currentItem == count + 1) {
                    mViewPager.setCurrentItem(currentItem);
                    handler.postDelayed(task, 500);
                } else {
                    mViewPager.setCurrentItem(currentItem);
                    handler.postDelayed(task, delayTime);
                }
            }
        }
    };


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i(TAG, ev.getAction() + "--" + isAutoPlay);
        if (isAutoPlay) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                    || action == MotionEvent.ACTION_OUTSIDE) {
                startAutoPlay();
            } else if (action == MotionEvent.ACTION_DOWN) {
                stopAutoPlay();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(state);
        }
        currentItem = mViewPager.getCurrentItem();
        switch (state) {
            case 0:
                if (currentItem == 0) {
                    mViewPager.setCurrentItem(count, false);
                } else if (currentItem == count + 1) {
                    mViewPager.setCurrentItem(1, false);
                }
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(position);
        }
        mIndicatorImageViews.get((lastPosition - 1 + count) % count).setImageResource(mIndicatorUnselectedResId);
        mIndicatorImageViews.get((position - 1 + count) % count).setImageResource(mIndicatorSelectedResId);
        lastPosition = position;

    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.mListener = listener;
        if (adapter != null) adapter.setOnBannerClickListener(mListener);
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    private class LoopPagerAdapter extends RecyclePagerAdapter<LoopPagerViewHolder> {
        private OnBannerClickListener mInnerListener;

        @Override
        public int getItemCount() {
            return (mDataList == null || mDataList.size() == 0) ? 0 : mDataList.size() + 2;
        }

        @Override
        public LoopPagerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LoopPagerViewHolder viewHolder = new LoopPagerViewHolder(parent);
            viewHolder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mInnerListener != null) {
                        AbstractPagerData data = (AbstractPagerData) viewHolder.itemView.getTag();
                        if (data != null)
                            mInnerListener.OnBannerClick(data);
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(LoopPagerViewHolder holder, int position) {
            AbstractPagerData data = null;
            if (position == 0 && mDataList.size() > 0) {
                data = mDataList.get(mDataList.size() - 1);
            } else if (position > mDataList.size()) {
                data = mDataList.get(0);
            } else if (position >= 1) {
                data = mDataList.get(position - 1);
            }
            holder.setData(data);

            holder.itemView.setTag(data);
        }

        public void setOnBannerClickListener(OnBannerClickListener listener) {
            this.mInnerListener = listener;
        }
    }

    private class LoopPagerViewHolder extends RecyclePagerAdapter.PagerViewHolder {
        private SimpleDraweeView simpleDraweeView;

        public LoopPagerViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.loopbanner_page_item, parent, false));
            simpleDraweeView = (SimpleDraweeView) itemView.findViewById(R.id.item_sdv);
            //Log.i(TAG, "===========");
        }

        public void setData(AbstractPagerData data) {
            if (data != null) {
                // Log.i(TAG, "Data: " + data.toString());
                simpleDraweeView.setImageURI(Uri.parse(data.getImageUrl()));
            }
        }

        public View getView() {
            return simpleDraweeView;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (adapter != null) adapter.notifyDataSetChanged();
        isAttachedToWindow = true;
        startAutoPlay();
        //Log.i("loopbanner", "========onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        stopAutoPlay();
        //Log.i("loopbanner", "========onDetachedFromWindow");
    }

    public void setIndictorVisibleIfOnlyOne(boolean isShowAble) {
        this.IsShowIndicatorIfOnlyOne = isShowAble;
    }

    public ArrayList<? extends AbstractPagerData> getmDataList() {
        return mDataList;
    }
}
