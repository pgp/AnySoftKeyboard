package it.pgp.uhu;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.anysoftkeyboard.ime.AnySoftKeyboardBase;

import it.pgp.uhu.enums.PermReqCodes;
import it.pgp.uhu.visualization.ClipboardRibbon;

public class UhuActivity extends Activity {

    static {
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX);
    }

    public static void simulateHomePress(Activity activity) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(startMain);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void openOverlayPermissionsManagement() {
        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), PermReqCodes.OVERLAYS.ordinal());
    }

    public void onPermOK() {
        manager = AnySoftKeyboardBase.instance.getClipboardManager(getApplicationContext());
        
        new ClipboardRibbon(AnySoftKeyboardBase.instance, this);

        // neither this nor polling will work if activity is not on focus or is not an IME, on Android 10+
        manager.addPrimaryClipChangedListener(() -> {
            ClipData clipData = manager.getPrimaryClip();
            String clipText = clipData.getItemAt(0).getText().toString();
            // Toast.makeText(UhuActivity.this, "intercepted: "+clipText, Toast.LENGTH_SHORT).show();
            AnySoftKeyboardBase.instance.getClipboardAdapter(this).add(clipText); // relies on MRU logic
        });

        new Handler().postDelayed(()->simulateHomePress(this), 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PermReqCodes prc = PermReqCodes.values()[requestCode];
        boolean canDrawOverlays = Settings.canDrawOverlays(this);
        switch (prc) {
            case OVERLAYS:
                if(canDrawOverlays) onPermOK();
                else {
                    Toast.makeText(this, "Overlay permission is necessary for having a floating clipboard window, exiting...", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
        }
    }

    ClipboardManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add overlay and pause activity immediately
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                Settings.canDrawOverlays(this)) {
            onPermOK();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openOverlayPermissionsManagement();
        }
    }
}