package com.imber.shadowroller.ui;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0) {
            animateOut(child, coordinatorLayout);
        } else if (dyConsumed < 0) {
            animateIn(child);
        }
    }

    private void animateOut(FloatingActionButton fab, CoordinatorLayout layout) {
        fab.animate()
                .translationY(layout.getBottom() - fab.getTop())
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator());
    }

    private void animateIn(FloatingActionButton fab) {
        fab.animate()
                .translationY(0)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator());
    }
}

