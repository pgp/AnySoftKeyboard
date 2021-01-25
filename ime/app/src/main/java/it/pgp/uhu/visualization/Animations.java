package it.pgp.uhu.visualization;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AbsListView;

public class Animations {

    // web source:
    // https://ssaurel.medium.com/create-a-blink-effect-on-android-3c76b5e0e36b
    public static void highlightListViewItem(View view, Runnable onAnimEnd) {
        final Drawable oldBg = view.getBackground();
        int oldBgColor = (oldBg instanceof ColorDrawable) ? ((ColorDrawable) oldBg).getColor() : Color.TRANSPARENT;
        ObjectAnimator anim = ObjectAnimator.ofInt(view,"backgroundColor", oldBgColor, Color.GREEN, oldBgColor);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimEnd.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        anim.setDuration(750);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatCount(0);
        anim.start();
    }
}
