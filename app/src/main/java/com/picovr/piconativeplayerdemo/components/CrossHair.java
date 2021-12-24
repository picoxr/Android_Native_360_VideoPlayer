package com.picovr.piconativeplayerdemo.components;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.picovr.piconativeplayerdemo.R;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.ShaderUtil;
import com.picovr.piconativeplayerdemo.utils.TextureUtil;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CrossHair extends BasicComponent {

    private static final float UNIT_SIZE = 0.4f;
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
            1.0f, 1.0f, 1.0f,};
    private final float[] mProjMatrix = new float[16];
    private final float[] mCameraMatrix = new float[16];
    private int mProgram;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTextureCoordHandle;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;
    private int mPlayTexture;

    public CrossHair(Context context) {
        super(context);
    }

    @Override
    public void onInitGL(float[] frustum) {
        initVertexData();
        initShader();
        System.arraycopy(frustum, 0, mProjMatrix, 0, 16);
    }

    @Override
    public void onFrameBegin(float[] eyes, HmdState hmdState) {
        System.arraycopy(eyes, 0, mCameraMatrix, 0, 16);
    }

    @Override
    public void onDrawSelf(Eye eye) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUseProgram(mProgram);

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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPlayTexture);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, BTN_MATRIX.length / 3);

        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(maTextureCoordHandle);
    }

    private float[] getFinalMatrix() {
        float[] mVPMatrix = new float[16];
        Matrix.multiplyMM(mVPMatrix, 0, mCameraMatrix, 0, MatrixTool.getCurrentMatrix(), 0);
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVPMatrix, 0);
        return mVPMatrix;
    }

    private void initVertexData() {
        ByteBuffer vbb = ByteBuffer.allocateDirect(BTN_MATRIX.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(BTN_MATRIX);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(BTN_TEXTURE_MATRIX.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asFloatBuffer();
        mTexCoorBuffer.put(BTN_TEXTURE_MATRIX);
        mTexCoorBuffer.position(0);
        mPlayTexture = TextureUtil.initTexture(mContext, R.drawable.crosshair);
    }

    private void initShader() {
        String vertexShader = ShaderUtil.loadFromAssetsFile("vertex.sh", mContext.getResources());
        String fragmentShader = ShaderUtil.loadFromAssetsFile("fragment.sh", mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexShader, fragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

}
