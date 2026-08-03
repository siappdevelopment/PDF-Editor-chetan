package com.reader.alldocviewer.pdfeditor.pdfviewer;

import android.graphics.RectF;
import androidx.annotation.Nullable;

import com.reader.alldocviewer.pdfeditor.pdfviewer.model.PagePart;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/* loaded from: classes11.dex */
public class CacheManager {
    public final PriorityQueue<PagePart> activeCache;
    public final PagePartComparator orderComparator;
    public final Object passiveActiveLock = new Object();
    public final PriorityQueue<PagePart> passiveCache;
    public final List<PagePart> thumbnails;

    public CacheManager() {
        PagePartComparator pagePartComparator = new PagePartComparator();
        this.orderComparator = pagePartComparator;
        this.activeCache = new PriorityQueue<>(Constants.Cache.CACHE_SIZE, pagePartComparator);
        this.passiveCache = new PriorityQueue<>(Constants.Cache.CACHE_SIZE, pagePartComparator);
        this.thumbnails = new ArrayList();
    }

    public void cachePart(PagePart part) {
        synchronized (this.passiveActiveLock) {
            makeAFreeSpace();
            this.activeCache.offer(part);
        }
    }

    public void makeANewSet() {
        synchronized (this.passiveActiveLock) {
            this.passiveCache.addAll(this.activeCache);
            this.activeCache.clear();
        }
    }

    public final void makeAFreeSpace() {
        synchronized (this.passiveActiveLock) {
            while (this.activeCache.size() + this.passiveCache.size() >= Constants.Cache.CACHE_SIZE && !this.passiveCache.isEmpty()) {
                try {
                    this.passiveCache.poll().getRenderedBitmap().recycle();
                } catch (Throwable th) {
                    throw th;
                }
            }
            while (this.activeCache.size() + this.passiveCache.size() >= Constants.Cache.CACHE_SIZE && !this.activeCache.isEmpty()) {
                this.activeCache.poll().getRenderedBitmap().recycle();
            }
        }
    }

    public void cacheThumbnail(PagePart part) {
        synchronized (this.thumbnails) {
            while (this.thumbnails.size() >= Constants.Cache.THUMBNAILS_CACHE_SIZE) {
                try {
                    this.thumbnails.remove(0).getRenderedBitmap().recycle();
                } catch (Throwable th) {
                    throw th;
                }
            }
            addWithoutDuplicates(this.thumbnails, part);
        }
    }

    public boolean upPartIfContained(int page, RectF pageRelativeBounds, int toOrder) {
        PagePart pagePart = new PagePart(page, null, pageRelativeBounds, false, 0);
        synchronized (this.passiveActiveLock) {
            try {
                PagePart find = find(this.passiveCache, pagePart);
                boolean z = true;
                if (find != null) {
                    this.passiveCache.remove(find);
                    find.setCacheOrder(toOrder);
                    this.activeCache.offer(find);
                    return true;
                }
                if (find(this.activeCache, pagePart) == null) {
                    z = false;
                }
                return z;
            } finally {
            }
        }
    }

    public boolean containsThumbnail(int page, RectF pageRelativeBounds) {
        PagePart pagePart = new PagePart(page, null, pageRelativeBounds, true, 0);
        synchronized (this.thumbnails) {
            try {
                for (PagePart pagePart2 : this.thumbnails) {
                    if (pagePart2.equals(pagePart)) {
                        return true;
                    }
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public final void addWithoutDuplicates(Collection<PagePart> collection, PagePart newPart) {
        for (PagePart pagePart : collection) {
            if (pagePart.equals(newPart)) {
                newPart.getRenderedBitmap().recycle();
                return;
            }
        }
        collection.add(newPart);
    }

    @Nullable
    public static PagePart find(PriorityQueue<PagePart> vector, PagePart fakePart) {
        Iterator<PagePart> it = vector.iterator();
        while (it.hasNext()) {
            PagePart next = it.next();
            if (next.equals(fakePart)) {
                return next;
            }
        }
        return null;
    }

    public List<PagePart> getPageParts() {
        ArrayList arrayList;
        synchronized (this.passiveActiveLock) {
            arrayList = new ArrayList(this.passiveCache);
            arrayList.addAll(this.activeCache);
        }
        return arrayList;
    }

    public List<PagePart> getThumbnails() {
        List<PagePart> list;
        synchronized (this.thumbnails) {
            list = this.thumbnails;
        }
        return list;
    }

    public void recycle() {
        synchronized (this.passiveActiveLock) {
            try {
                Iterator<PagePart> it = this.passiveCache.iterator();
                while (it.hasNext()) {
                    it.next().getRenderedBitmap().recycle();
                }
                this.passiveCache.clear();
                Iterator<PagePart> it2 = this.activeCache.iterator();
                while (it2.hasNext()) {
                    it2.next().getRenderedBitmap().recycle();
                }
                this.activeCache.clear();
            } finally {
            }
        }
        synchronized (this.thumbnails) {
            try {
                for (PagePart pagePart : this.thumbnails) {
                    pagePart.getRenderedBitmap().recycle();
                }
                this.thumbnails.clear();
            } finally {
            }
        }
    }

    /* loaded from: classes11.dex */
    public class PagePartComparator implements Comparator<PagePart> {
        public PagePartComparator() {
        }

        @Override // java.util.Comparator
        public int compare(PagePart part1, PagePart part2) {
            if (part1.getCacheOrder() == part2.getCacheOrder()) {
                return 0;
            }
            return part1.getCacheOrder() > part2.getCacheOrder() ? 1 : -1;
        }
    }
}
