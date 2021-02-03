package it.pgp.uhu.visualization;

/**
 * Adapted from it.pgp.xfiles.service.visualization.MovingRibbon
 */

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import it.pgp.uhu.adapters.ClipboardAdapter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ClipboardRibbon implements View.OnTouchListener {

    // layout content
    public RecyclerView clipboard_lv;
    public Switch interceptClipboard;
    public ImageButton closeOverlay;
    public ImageButton clearClipboard;
    public ImageButton resizeOverlay;

    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;

    public void destroy() {
        try{ wm.removeView(oView); } catch(Throwable ignored) {}
        try{ wm.removeView(topLeftView); } catch(Throwable ignored) {}
        AnyApplication.clipboardShown.set(false);
    }

    protected boolean overlayNotAvailable = false;
    protected WindowManager wm;
    protected LinearLayout oView;
    protected View topLeftView;
    protected final Context context;

    public void addViewToOverlay(View view, WindowManager.LayoutParams params) {
        if (overlayNotAvailable) return;
        try {
            wm.addView(view, params);
        }
        catch (Exception e) {
            overlayNotAvailable = true;
            e.printStackTrace();

            Toast.makeText(context,"Unable to draw system overlay, ensure you have granted overlay permissions",Toast.LENGTH_SHORT).show();
        }
    }

    public ClipboardRibbon(final Context context) {
        this.wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.context = context;
        if(AnyApplication.clipboardShown.get()) {
            Toast.makeText(context, "Clipboard is already visible", Toast.LENGTH_SHORT).show();
            return;
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        oView = (LinearLayout) inflater.inflate(R.layout.clipboard_ribbon, null);

        clipboard_lv = oView.findViewById(R.id.clipboard_lv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        ClipboardAdapter a = AnyApplication.instance.getClipboardAdapter(context);
        clipboard_lv.setLayoutManager(layoutManager);
        clipboard_lv.setAdapter(a);
        new ItemTouchHelper(new DragDropItemTouchHelperCallback(a,a.mru.keys)).attachToRecyclerView(clipboard_lv);

        interceptClipboard = oView.findViewById(R.id.interceptClipboard);
        interceptClipboard.setChecked(AnyApplication.clipboardIntercept.get());
        interceptClipboard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AnyApplication.setInterceptStatus(context, isChecked);
            Toast.makeText(context, "Clipboard intercept is "+(isChecked?"ON":"OFF"), Toast.LENGTH_SHORT).show();
        });

        closeOverlay = oView.findViewById(R.id.closeOverlay);
        closeOverlay.setOnClickListener(v -> destroy());

        resizeOverlay = oView.findViewById(R.id.resizeOverlay);
        resizeOverlay.setOnClickListener(v -> {
            WindowManager.LayoutParams c = ViewType.CONTAINER.getParams();
            boolean defaultSize = c.height==600;
            c.height = defaultSize?900:600;
            wm.updateViewLayout(oView,c);
            resizeOverlay.setImageResource(defaultSize?android.R.drawable.arrow_up_float:android.R.drawable.arrow_down_float);
        });

        clearClipboard = oView.findViewById(R.id.clearClipboard);
        clearClipboard.setOnClickListener(v -> AnyApplication.instance.getClipboardAdapter(context).clear());

        oView.setOnTouchListener(this);

        // wm.addView(oView, params);
        addViewToOverlay(oView,ViewType.CONTAINER.getParams());

        topLeftView = new View(context);

//        wm.addView(topLeftView,topLeftParams);
        addViewToOverlay(topLeftView,ViewType.ANCHOR.getParams());

        if(!overlayNotAvailable)
            AnyApplication.clipboardShown.set(true);
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

            params.x = newX - topLeftLocationOnScreen[0];
            params.y = newY - topLeftLocationOnScreen[1];

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
