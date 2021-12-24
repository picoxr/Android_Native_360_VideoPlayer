package com.picovr.piconativeplayerdemo.components.controllerpanel;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.picovr.piconativeplayerdemo.R;
import com.picovr.piconativeplayerdemo.pickup.TouchableObject;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.ShaderUtil;
import com.picovr.piconativeplayerdemo.utils.TextureUtil;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ControllerButton extends TouchableObject {

    private int mVCount = 0;
    private float xAngle = 0;
    private static final float ANGLE_SPAN = 0.375f;
/*    private static final float UNIT_SIZE = 4f;
    private static final float[] BTN_MATRIX = new float[]{
            0, -UNIT_SIZE, UNIT_SIZE,
            0, UNIT_SIZE, UNIT_SIZE,
            0, -UNIT_SIZE, -UNIT_SIZE,
            0, UNIT_SIZE, UNIT_SIZE,
            0, -UNIT_SIZE, -UNIT_SIZE,
            0, UNIT_SIZE, -UNIT_SIZE};

    private static final float[] BTN_TEXTURE_MATRIX = new float[]{
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f,};*/

    private int mProgram;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTextureCoordHandle;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;

    private float[] mProjMatrix = new float[16];
    private float[] mCameraMatrix = new float[16];

    private int mPlayTexture;
    private int mPlayPickUpTexture;

    private int mPauseTexture;
    private int mPausePickUpTexture;

    private boolean mIsPlaying;

    void setIsPlaying(boolean isPlaying) {
        mIsPlaying = isPlaying;
    }

    ControllerButton(Context context) {
        super(context);
    }

    @Override
    public void onInitGL(float[] frustum) {
        initVertexData();
        initShader();
        System.arraycopy(frustum, 0, mProjMatrix, 0, 16);
        new RotateThread().start();
    }

    @Override
    public void onFrameBegin(float[] eyes, HmdState hmdState) {
        System.arraycopy(eyes, 0, mCameraMatrix, 0, 16);
    }

    @Override
    public void onDrawSelf(Eye eye) {
        super.onDrawSelf(eye);
        GLES20.glUseProgram(mProgram);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, getFinalMatrix(), 0);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTextureCoordHandle);

        GLES20.glVertexAttribPointer(
                maPositionHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                3 * 4,
                mVertexBuffer
        );
        GLES20.glVertexAttribPointer(
                maTextureCoordHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                2 * 4,
                mTexCoorBuffer
        );
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if (!ismPickedUp()) {
            if (mIsPlaying) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPlayTexture);
            } else {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPauseTexture);
            }
        } else {
            if (mIsPlaying) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPlayPickUpTexture);
            } else {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPausePickUpTexture);
            }
        }
        //        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, BTN_MATRIX.length / 3);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVCount);

        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(maTextureCoordHandle);
    }

    private void initShader() {
        String vertexShader = ShaderUtil.loadFromAssetsFile("vertex.sh", mContext.getResources());
        String fragmentShader = ShaderUtil.loadFromAssetsFile("fragment.sh", mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexShader, fragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    private float[] getFinalMatrix() {
        float[] mVPMatrix = new float[16];
        Matrix.multiplyMM(mVPMatrix, 0, mCameraMatrix, 0, MatrixTool.getCurrentMatrix(), 0);
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVPMatrix, 0);
        return mVPMatrix;
    }

    @Override
    public void onClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick();
        }
    }

    private void initVertexData() {
        float angdegSpan = 360.0f / 36;
        mVCount = 3 * 36;
        int r = 4;

        float[] vertices = new float[mVCount * 3];
        float[] textures = new float[mVCount * 2];
        int count = 0;
        int stCount = 0;
        for (float angdeg = 0; Math.ceil(angdeg) < 360; angdeg += angdegSpan) {
            double angrad = Math.toRadians(angdeg);
            double angradNext = Math.toRadians(angdeg + angdegSpan);
            vertices[count++] = 0;
            vertices[count++] = 0;
            vertices[count++] = 0;

            textures[stCount++] = 0.5f;
            textures[stCount++] = 0.5f;

            vertices[count++] = (float) (-r * Math.sin(angrad));
            vertices[count++] = (float) (r * Math.cos(angrad));
            vertices[count++] = 0;

            textures[stCount++] = (float) (0.5f - 0.5f * Math.sin(angrad));
            textures[stCount++] = (float) (0.5f - 0.5f * Math.cos(angrad));

            vertices[count++] = (float) (-r * Math.sin(angradNext));
            vertices[count++] = (float) (r * Math.cos(angradNext));
            vertices[count++] = 0;

            textures[stCount++] = (float) (0.5f - 0.5f * Math.sin(angradNext));
            textures[stCount++] = (float) (0.5f - 0.5f * Math.cos(angradNext));
        }
        setmPreBox(vertices);

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(textures.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asFloatBuffer();
        mTexCoorBuffer.put(textures);
        mTexCoorBuffer.position(0);

        mPlayTexture = TextureUtil.initTexture(mContext, R.drawable.play);
        mPlayPickUpTexture = TextureUtil.initTexture(mContext, R.drawable.play_pickup);
        mPauseTexture = TextureUtil.initTexture(mContext, R.drawable.pause);
        mPausePickUpTexture = TextureUtil.initTexture(mContext, R.drawable.pause_pickup);

    }

    public class RotateThread extends Thread {
        boolean flag = true;

        @Override
        public void run() {
            while (flag) {
                xAngle = xAngle + ANGLE_SPAN;
                try {
                    Thread.sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
