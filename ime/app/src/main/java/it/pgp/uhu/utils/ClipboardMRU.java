package it.pgp.uhu.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.util.Log;

public class ClipboardMRU extends SimpleMRU<String> {
    public final ClipboardManager manager;

    public ClipboardMRU(int maxIndex, ClipboardManager manager) {
        super(maxIndex);
        this.manager = manager;
    }

    public boolean addClipboardItemFromSystem() {
        try {
            ClipData clipData = manager.getPrimaryClip();
            if(clipData==null) {
                Log.e(getClass().getName(),"AnySoftKeyBoard is not the default IME, cannot access clipboard");
                return false;
            }

            String clipText = clipData.getItemAt(0).getText().toString();
            return addItem(clipText);
        }
        catch(NullPointerException n) {
            Log.i(getClass().getName(),"Clipboard is empty, no item to take");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addItem(String clipboardItem) {
        if(clipboardItem==null || clipboardItem.isEmpty()) {
            Log.w(getClass().getName(),"Won't add null or empty items to history");
            return false;
        }
        int idx = findIndex(clipboardItem);

        // if index is -1, add it on top, recycle oldest item
        if(idx == -1) {
            Log.e(getClass().getName(),"New item: "+clipboardItem);
            setLatest(clipboardItem, System.currentTimeMillis());
            return true;
        }

        // if it's latest (index 0), it means the item is already on top of the history
        else if(idx == 0) {
            Log.d(getClass().getName(), "No event detected");
            return false;
        }
        // if index > 0, swap mru objects
        else if (idx > 0) {
            // TODO may it also be desirable to have duplicated history items? in that case, setLatest should be used instead of swap
            Log.d(getClass().getName(), "Text already in history, bringing it up");
            bringToTop(idx);
            return true;
        }
        else throw new RuntimeException("Guard block");
    }

    public synchronized void removeItem(int position) {
        keys.remove(position);
        modified.remove(position);
    }
}
