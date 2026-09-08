package com.reader.alldocviewer.pdfeditor.pdfviewer.viewpagerChange;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.os.ParcelableCompat;
import androidx.core.os.ParcelableCompatCreatorCallbacks;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.HashMap;


public class RtlViewPager extends ViewPager {
    private int mLayoutDirection;
    private HashMap<OnPageChangeListener, ReversingOnPageChangeListener> mPageChangeListeners;

    public RtlViewPager(Context context) {
        super(context);
        this.mLayoutDirection = 0;
        this.mPageChangeListeners = new HashMap<>();
    }

    public RtlViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLayoutDirection = 0;
        this.mPageChangeListeners = new HashMap<>();
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {
            return false;
        }
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception unused) {
            return false;
        }
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {
            return false;
        }
        try {
            return super.onTouchEvent(ev);
        } catch (Exception unused) {
            return false;
        }
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        int i = layoutDirection != 1 ? 0 : 1;
        if (i != this.mLayoutDirection) {
            PagerAdapter adapter = super.getAdapter();
            int currentItem = adapter != null ? getCurrentItem() : 0;
            this.mLayoutDirection = i;
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                setCurrentItem(currentItem);
            }
        }
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void setAdapter(PagerAdapter adapter) {
        if (adapter != null) {
            adapter = new ReversingAdapter(adapter);
        }
        super.setAdapter(adapter);
        setCurrentItem(0);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public PagerAdapter getAdapter() {
        ReversingAdapter reversingAdapter = (ReversingAdapter) super.getAdapter();
        if (reversingAdapter == null) {
            return null;
        }
        return reversingAdapter.getDelegate();
    }

    public final boolean isRtl() {
        return this.mLayoutDirection == 1;
    }

    @Override // androidx.viewpager.widget.ViewPager
    public int getCurrentItem() {
        int currentItem = super.getCurrentItem();
        PagerAdapter adapter = super.getAdapter();
        return (adapter == null || !isRtl()) ? currentItem : (adapter.getCount() - currentItem) - 1;
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void setCurrentItem(int position, boolean smoothScroll) {
        PagerAdapter adapter = super.getAdapter();
        if (adapter != null && isRtl()) {
            position = (adapter.getCount() - position) - 1;
        }
        super.setCurrentItem(position, smoothScroll);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void setCurrentItem(int position) {
        PagerAdapter adapter = super.getAdapter();
        if (adapter != null && isRtl()) {
            position = (adapter.getCount() - position) - 1;
        }
        super.setCurrentItem(position);
    }

    
    public static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() { // from class: com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.RtlViewPager.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // androidx.core.os.ParcelableCompatCreatorCallbacks
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // androidx.core.os.ParcelableCompatCreatorCallbacks
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
        public final int mLayoutDirection;
        public final Parcelable mViewPagerSavedState;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public SavedState(Parcelable viewPagerSavedState, int layoutDirection) {
            this.mViewPagerSavedState = viewPagerSavedState;
            this.mLayoutDirection = layoutDirection;
        }

        public SavedState(Parcel in, ClassLoader loader) {
            this.mViewPagerSavedState = in.readParcelable(loader == null ? getClass().getClassLoader() : loader);
            this.mLayoutDirection = in.readInt();
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(this.mViewPagerSavedState, flags);
            out.writeInt(this.mLayoutDirection);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this.mLayoutDirection);
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        this.mLayoutDirection = savedState.mLayoutDirection;
        super.onRestoreInstanceState(savedState.mViewPagerSavedState);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        super.setOnPageChangeListener(new ReversingOnPageChangeListener(listener));
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        ReversingOnPageChangeListener reversingOnPageChangeListener = new ReversingOnPageChangeListener(listener);
        this.mPageChangeListeners.put(listener, reversingOnPageChangeListener);
        super.addOnPageChangeListener(reversingOnPageChangeListener);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        ReversingOnPageChangeListener remove = this.mPageChangeListeners.remove(listener);
        if (remove != null) {
            super.removeOnPageChangeListener(remove);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners();
        this.mPageChangeListeners.clear();
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            int i = 0;
            for (int i2 = 0; i2 < getChildCount(); i2++) {
                View childAt = getChildAt(i2);
                childAt.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int measuredHeight = childAt.getMeasuredHeight();
                if (measuredHeight > i) {
                    i = measuredHeight;
                }
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(i, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    
    public class ReversingOnPageChangeListener implements OnPageChangeListener {
        public final OnPageChangeListener mListener;

        public ReversingOnPageChangeListener(OnPageChangeListener listener) {
            this.mListener = listener;
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int width = RtlViewPager.this.getWidth();
            PagerAdapter adapter = RtlViewPager.super.getAdapter();
            if (RtlViewPager.this.isRtl() && adapter != null) {
                int count = adapter.getCount();
                int pageWidth = ((int) ((1.0f - adapter.getPageWidth(position)) * (float) width)) + positionOffsetPixels;
                while (position < count && pageWidth > 0) {
                    position++;
                    pageWidth -= (int) (adapter.getPageWidth(position) * (float) width);
                }
                position = (count - position) - 1;
                positionOffsetPixels = -pageWidth;
                positionOffset = positionOffsetPixels / ((float) width * adapter.getPageWidth(position));
            }
            this.mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageSelected(int position) {
            PagerAdapter adapter = RtlViewPager.super.getAdapter();
            if (RtlViewPager.this.isRtl() && adapter != null) {
                position = (adapter.getCount() - position) - 1;
            }
            this.mListener.onPageSelected(position);
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int state) {
            this.mListener.onPageScrollStateChanged(state);
        }
    }

    
    public class ReversingAdapter extends DelegatingPagerAdapter {
        public ReversingAdapter(@NonNull PagerAdapter adapter) {
            super(adapter);
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (RtlViewPager.this.isRtl()) {
                position = (getCount() - position) - 1;
            }
            super.destroyItem(container, position, object);
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public void destroyItem(View container, int position, Object object) {
            if (RtlViewPager.this.isRtl()) {
                position = (getCount() - position) - 1;
            }
            super.destroyItem(container, position, object);
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public int getItemPosition(Object object) {
            int itemPosition = super.getItemPosition(object);
            if (RtlViewPager.this.isRtl()) {
                if (itemPosition == -1 || itemPosition == -2) {
                    return -2;
                }
                return (getCount() - itemPosition) - 1;
            }
            return itemPosition;
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public CharSequence getPageTitle(int position) {
            if (RtlViewPager.this.isRtl()) {
                position = (getCount() - position) - 1;
            }
            return super.getPageTitle(position);
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public float getPageWidth(int position) {
            if (RtlViewPager.this.isRtl()) {
                position = (getCount() - position) - 1;
            }
            return super.getPageWidth(position);
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public Object instantiateItem(ViewGroup container, int position) {
            if (RtlViewPager.this.isRtl()) {
                position = (getCount() - position) - 1;
            }
            return super.instantiateItem(container, position);
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public Object instantiateItem(View container, int position) {
            if (RtlViewPager.this.isRtl()) {
                position = (getCount() - position) - 1;
            }
            return super.instantiateItem(container, position);
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public void setPrimaryItem(View container, int position, Object object) {
            try {
                if (RtlViewPager.this.isRtl()) {
                    position = (getCount() - position) - 1;
                }
                if (!(object instanceof Fragment) || ((Fragment) object).isAdded()) {
                    super.setPrimaryItem(container, position, object);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            try {
                if (RtlViewPager.this.isRtl()) {
                    position = (getCount() - position) - 1;
                }
                if (!(object instanceof Fragment) || ((Fragment) object).isAdded()) {
                    super.setPrimaryItem(container, position, object);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
