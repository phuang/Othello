package com.penghuang.othello;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by penghuang on 2/27/15.
 */
class OthelloView extends GLSurfaceView {
    private static final String TAG = "OthelloView";
    final private OthelloRenderer mRenderer;

    public OthelloView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRenderer = new OthelloRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return OthelloView.this.onTouch(event);
            }
        });
    }

    private boolean onTouch(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final float x = event.getX();
                final float y = event.getY();
                break;
        }
        return false;
    }
}
