package it.pgp.uhu.utils;

import java.util.LinkedList;

/**
 * MRU cache for clipboard entries
 * Adapted from it.pgp.xfiles.utils.GenericMRU
 */

public class SimpleMRU<T> {

    public final LinkedList<T> keys = new LinkedList<>();
    private final LinkedList<Long> modified = new LinkedList<>();

    public final int maxIndex;

    public static final int DEFAULT_SIZE = 10;

    public SimpleMRU(int maxIndex) {
        this.maxIndex = maxIndex;
    }

    public void clear() {
        modified.clear();
        keys.clear();
    }

    // find methods
    // in principle, this could be less than O(n), using another data structure rather than LinkedList,
    // however, using only 10 items there is no gain in changing findIndex logic
    protected int findIndex(T key) {
        int i=0;
        for (T k : keys) {
            if (k.equals(key))
                return i;
            i++;
        }
        return -1;
    }

    public synchronized void bringToTop(int i2) {
        if (i2 == 0) return; // already on top
        T item = keys.remove(i2);
        keys.addFirst(item);
        Long t = modified.remove(i2);
        modified.addFirst(t);
    }

    // unconditionally set latest
    public synchronized void setLatest(T key, Long modifiedDate) {
        // find current
        int foundIdx = findIndex(key);
        if (foundIdx >= 0) {
            // unconditionally overwrite old cache entry, and bring to top
            modified.set(foundIdx,modifiedDate);

            // simply bring this cache entry to top, swapping it with the current 0
            if (foundIdx != 0) bringToTop(foundIdx);
        }
        else {
            // add a new entry, overwriting the least recent one if the clipboard is already full
            if(keys.size() == maxIndex) {
                keys.remove(maxIndex-1);
                modified.remove(maxIndex-1);
            }
            keys.addFirst(key);
            modified.addFirst(modifiedDate);
        }
    }
}
