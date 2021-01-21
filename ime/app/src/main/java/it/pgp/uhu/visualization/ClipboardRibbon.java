package it.pgp.uhu.visualization;

/**
 * Adapted from it.pgp.xfiles.service.visualization.MovingRibbon
 */

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.anysoftkeyboard.ime.AnySoftKeyboardBase;
import com.menny.android.anysoftkeyboard.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ClipboardRibbon implements View.OnTouchListener {

    // layout content
    public ListView clipboard_lv;
    public ImageButton closeOverlay;
    public ImageButton clearClipboard;

    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;

    public void destroy() {
        try{ wm.removeView(oView); } catch(Throwable ignored) {}
        try{ wm.removeView(topLeftView); } catch(Throwable ignored) {}
    }

    protected boolean overlayNotAvailable = false;
    protected WindowManager wm;
    protected LinearLayout oView;
    protected View topLeftView;
    protected final Service service;
    protected final Activity activity;

    public void addViewToOverlay(View view, WindowManager.LayoutParams params) {
        if (overlayNotAvailable) return;
        try {
            wm.addView(view, params);
        }
        catch (Exception e) {
            overlayNotAvailable = true;
            e.printStackTrace();

            Toast.makeText(service,"Unable to draw system overlay, ensure you have granted overlay permissions",Toast.LENGTH_SHORT).show();
        }
    }

    public ClipboardRibbon(final Service service, final Activity activity) {
        this.wm = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        this.service = service;
        this.activity = activity;
        LayoutInflater inflater = (LayoutInflater) service.getSystemService(LAYOUT_INFLATER_SERVICE);
        oView = (LinearLayout) inflater.inflate(R.layout.clipboard_ribbon, null);

        clipboard_lv = oView.findViewById(R.id.clipboard_lv);
        clipboard_lv.setAdapter(AnySoftKeyboardBase.instance.getClipboardAdapter(service));

        closeOverlay = oView.findViewById(R.id.closeOverlay);
        closeOverlay.setOnClickListener(v -> {
            destroy();
            activity.finishAffinity();
        });

        clearClipboard = oView.findViewById(R.id.clearClipboard);
        clearClipboard.setOnClickListener(v -> AnySoftKeyboardBase.instance.getClipboardAdapter(service).clear());

        oView.setOnTouchListener(this);

        // wm.addView(oView, params);
        addViewToOverlay(oView,ViewType.CONTAINER.getParams());

        topLeftView = new View(service);

//        wm.addView(topLeftView,topLeftParams);
        addViewToOverlay(topLeftView,ViewType.ANCHOR.getParams());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            v.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int[] topLeftLocationOnScreen = new int[2];
            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

//            Log.i("onTouch","topLeftY="+topLeftLocationOnScreen[1]);
//            Log.i("onTouch","originalY="+originalYPos);

            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) v.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            wm.updateViewLayout(v, params);
            moving = true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving) {
                return true;
            }
        }

        return false;
    }
}
