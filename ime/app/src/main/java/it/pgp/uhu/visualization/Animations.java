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
    public static View getViewByPosition(int pos, AbsListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    // web source:
    // https://ssaurel.medium.com/create-a-blink-effect-on-android-3c76b5e0e36b
    public static void highlightListViewItem(int pos, AbsListView listView, Runnable onAnimEnd) {
        final View view = getViewByPosition(pos, listView);
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
