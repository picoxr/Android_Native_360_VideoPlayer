package com.picovr.piconativeplayerdemo.components.picocontroller.g2;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.picovr.piconativeplayerdemo.R;
import com.picovr.piconativeplayerdemo.components.BasicComponent;
import com.picovr.piconativeplayerdemo.pickup.PickUpManager;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.MatrixUtil;
import com.picovr.piconativeplayerdemo.utils.ShaderUtil;
import com.picovr.piconativeplayerdemo.utils.TextureUtil;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Raycast extends BasicComponent {

    private int mProgram;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTexCoorHandle;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;

    private float[] mProjMatrix = new float[16];
    private float[] mCameraMatrix = new float[16];

    private int mTexture;
    private int vCount = 0;
    private float mRadius;
    private float mHeight;
    private int mN;

    public Raycast(Context context, float r, float h, int n) {
        super(context);
        mRadius = r;
        mHeight = h;
        mN = n;
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
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glUseProgram(mProgram);

        PickUpManager.getInstance().setPickUpMatrix(MatrixTool.getCurrentMatrix());

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, getFinalMatrix(), 0);

        GLES20.glVertexAttribPointer(
                maPositionHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                3 * 4,
                mVertexBuffer
        );
        GLES20.glVertexAttribPointer(
                maTexCoorHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                2 * 4,
                mTexCoorBuffer
        );
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

    }


    private void initVertexData() {
        float angdegSpan = 360.0f / mN;
        vCount = 3 * mN * 4;
        float[] vertices = new float[vCount * 3];
        float[] textures = new float[vCount * 2];
        int count = 0;
        int stCount = 0;
        for (float angdeg = 0; Math.ceil(angdeg) < 360; angdeg += angdegSpan) {
            double angrad = Math.toRadians(angdeg);
            double angradNext = Math.toRadians(angdeg + angdegSpan);
            vertices[count++] = (float) (-mRadius * Math.sin(angrad));
            vertices[count++] = 0;
            vertices[count++] = (float) (-mRadius * Math.cos(angrad));
            textures[stCount++] = (float) (angrad / (2 * Math.PI));
            textures[stCount++] = 1;

            vertices[count++] = (float) (-mRadius * Math.sin(angradNext));
            vertices[count++] = mHeight;
            vertices[count++] = (float) (-mRadius * Math.cos(angradNext));
            textures[stCount++] = (float) (angradNext / (2 * Math.PI));
            textures[stCount++] = 0;

            vertices[count++] = (float) (-mRadius * Math.sin(angrad));
            vertices[count++] = mHeight;
            vertices[count++] = (float) (-mRadius * Math.cos(angrad));
            textures[stCount++] = (float) (angrad / (2 * Math.PI));
            textures[stCount++] = 0;

            vertices[count++] = (float) (-mRadius * Math.sin(angrad));
            vertices[count++] = 0;
            vertices[count++] = (float) (-mRadius * Math.cos(angrad));
            textures[stCount++] = (float) (angrad / (2 * Math.PI));
            textures[stCount++] = 1;

            vertices[count++] = (float) (-mRadius * Math.sin(angradNext));
            vertices[count++] = 0;
            vertices[count++] = (float) (-mRadius * Math.cos(angradNext));
            textures[stCount++] = (float) (angradNext / (2 * Math.PI));
            textures[stCount++] = 1;

            vertices[count++] = (float) (-mRadius * Math.sin(angradNext));
            vertices[count++] = mHeight;
            vertices[count++] = (float) (-mRadius * Math.cos(angradNext));
            textures[stCount++] = (float) (angradNext / (2 * Math.PI));
            textures[stCount++] = 0;
        }
        float[] ray = new float[6];
        System.arraycopy(vertices, 0, ray, 0, 6);
        MatrixUtil.logMatrix("setRay ", ray);
        PickUpManager.getInstance().setControllerRay(ray);

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

        mTexture = TextureUtil.initTexture(mContext, R.drawable.ray);
    }

    private void initShader() {
        String vertexShader = ShaderUtil.loadFromAssetsFile("vertex.sh", mContext.getResources());
        String fragmentShader = ShaderUtil.loadFromAssetsFile("fragment.sh", mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexShader, fragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    private float[] getFinalMatrix() {
        float[] mVPMatrix = new float[16];
        Matrix.multiplyMM(mVPMatrix, 0, mCameraMatrix, 0, MatrixTool.getCurrentMatrix(), 0);
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVPMatrix, 0);
        return mVPMatrix;
    }
}
