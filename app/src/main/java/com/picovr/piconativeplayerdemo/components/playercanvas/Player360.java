package com.picovr.piconativeplayerdemo.components.playercanvas;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.picovr.piconativeplayerdemo.R;
import com.picovr.piconativeplayerdemo.components.BasicComponent;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.ShaderUtil;
import com.picovr.piconativeplayerdemo.utils.TextureUtil;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Player360 extends BasicComponent {
    private static final float radius = 100f;
    private static final double angleSpan = Math.PI / 90f;
    private int mVCount = 0;
    private FloatBuffer mPosBuffer;
    private FloatBuffer mTexBuffer;
    private int mTextureId;

    private int mProgram;
    private int muMVPMatrixHandle;
    private int mPositionHandle;
    private int mUSTMMatrixHandle;
    private int mATextureCoordHandle;

    private float[] mProjMatrix = new float[16];
    private float[] mCameraMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    Player360(Context context) {
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
        GLES20.glUseProgram(mProgram);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, getFinalMatrix(), 0);
        GLES20.glUniformMatrix4fv(mUSTMMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mPosBuffer);
        GLES20.glEnableVertexAttribArray(mATextureCoordHandle);
        GLES20.glVertexAttribPointer(mATextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVCount);
    }

    private float[] getFinalMatrix() {
        float[] mVPMatrix = new float[16];
        Matrix.multiplyMM(mVPMatrix, 0, mCameraMatrix, 0, MatrixTool.getCurrentMatrix(), 0);
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVPMatrix, 0);
        return mVPMatrix;
    }

    private void initVertexData() {
        calculateAttribute();
    }

    private void initShader() {
        String vertexShader = ShaderUtil.loadFromRaw(mContext, R.raw.vertex_shader);
        String fragmentShader = ShaderUtil.loadFromRaw(mContext, R.raw.fragment_shader);
        mProgram = ShaderUtil.createProgram(vertexShader, fragmentShader);
        ShaderUtil.checkGlError("programId program params");

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mUSTMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        mATextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        mTextureId = TextureUtil.createTexture3D();
    }

    private void calculateAttribute() {
        ArrayList<Float> alVertix = new ArrayList<>();
        ArrayList<Float> textureVertix = new ArrayList<>();

        for (double vAngle = 0; vAngle < Math.PI; vAngle = vAngle + angleSpan) {

            for (double hAngle = 0; hAngle < 2 * Math.PI; hAngle = hAngle + angleSpan) {
                float x0 = (float) (radius * Math.sin(vAngle) * Math.cos(hAngle));
                float y0 = (float) (radius * Math.sin(vAngle) * Math.sin(hAngle));
                float z0 = (float) (radius * Math.cos((vAngle)));

                float x1 = (float) (radius * Math.sin(vAngle) * Math.cos(hAngle + angleSpan));
                float y1 = (float) (radius * Math.sin(vAngle) * Math.sin(hAngle + angleSpan));
                float z1 = (float) (radius * Math.cos(vAngle));

                float x2 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan));
                float y2 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan));
                float z2 = (float) (radius * Math.cos(vAngle + angleSpan));

                float x3 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle));
                float y3 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle));
                float z3 = (float) (radius * Math.cos(vAngle + angleSpan));

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);

                alVertix.add(x0);
                alVertix.add(y0);
                alVertix.add(z0);

                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);

                float s0 = (float) (hAngle / Math.PI / 2);
                float s1 = (float) ((hAngle + angleSpan) / Math.PI / 2);
                float t0 = (float) (vAngle / Math.PI);
                float t1 = (float) ((vAngle + angleSpan) / Math.PI);

                textureVertix.add(s1);
                textureVertix.add(t0);
                textureVertix.add(s0);
                textureVertix.add(t0);
                textureVertix.add(s0);
                textureVertix.add(t1);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);

                textureVertix.add(s1);
                textureVertix.add(t0);
                textureVertix.add(s0);
                textureVertix.add(t1);
                textureVertix.add(s1);
                textureVertix.add(t1);
            }
        }
        mVCount = alVertix.size() / 3;
        mPosBuffer = convertToFloatBuffer(alVertix);
        mTexBuffer = convertToFloatBuffer(textureVertix);
    }

    private FloatBuffer convertToFloatBuffer(ArrayList<Float> data) {
        float[] d = new float[data.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = data.get(i);
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(data.size() * 4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer ret = buffer.asFloatBuffer();
        ret.put(d);
        ret.position(0);
        return ret;
    }

    float[] getTransformMatrix() {
        return mSTMatrix;
    }

    int getTextureId() {
        return mTextureId;
    }

}
