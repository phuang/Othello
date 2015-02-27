package com.penghuang.othello;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by penghuang on 2/27/15.
 */
class OthelloRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "OthelloRenderer";
    
    private int mWidth;
    private int mHeight;

    private Program mProgram;
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        mProgram = new Program();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        GLES20.glViewport(0, 0, mWidth, mHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        draw();
    }

    private void draw() {
    }
}

class Program {
    private static final String VS_CODE =
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "void main() {\n" +
            "  gl_Position = uMVPMatrix * aPosition;\n" +
            "}\n";

    private static final String FS_CODE =
            "precision mediump float;\n" +
            "uniform vec4 uColor;\n" +
            "void main() {\n" +
            "  gl_FragColor = uColor;\n" +
            "}\n";
    private final int mVS;
    private final int mFS;
    private final int mProgram;

    private int m_aPositionLocation;
    private int m_uMVPMatrixLocation;
    private int m_uColorLocation;

    Program() {
        mVS = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(mVS, VS_CODE);
        mFS = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(mFS, FS_CODE);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, mVS);
        GLES20.glAttachShader(mProgram, mFS);
        GLES20.glLinkProgram(mProgram);
        m_aPositionLocation = GLES20.glGetAttribLocation(mProgram, "aPostion");
        m_uMVPMatrixLocation = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        m_uColorLocation = GLES20.glGetUniformLocation(mProgram, "uColor");
    }

    void draw(Line line) {
        GLES20.glUseProgram(mProgram);

    }
}

class Line {
}