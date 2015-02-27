package com.penghuang.othello.ui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by penghuang on 2/27/15.
 */
public class OthelloView extends GLSurfaceView {
    public OthelloView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(new OthelloRenderer());
    }
}
