package com.android.systemui.statusbar;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

/** On-going action progress chip view group stores all elements of chip */
public class OnGoingActionProgressGroup {
    public final View rootView;
    public final ImageView iconView;
    public final ProgressBar progressBarView;

    public OnGoingActionProgressGroup(View rootView, ImageView iconView,
                                      ProgressBar progressBarView){
        this.rootView = rootView;
        this.iconView = iconView;
        this.progressBarView = progressBarView;
    }
}