package com.picovr.piconativeplayerdemo.components.picocontroller.neo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.picovr.piconativeplayerdemo.R;
import com.picovr.piconativeplayerdemo.components.BasicComponent;
import com.picovr.piconativeplayerdemo.model.ObjVertex;
import com.picovr.piconativeplayerdemo.utils.LoadObjUtil;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.ShaderUtil;
import com.picovr.piconativeplayerdemo.utils.TextureUtil;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Controller extends BasicComponent {

    private int mProgram;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTexCoorHandle;
    private int mVertexCount = 0;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;

    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];

    private NeoController.NEO_CONTROLLER mController;
    private int mTexture;

    Controller(Context context, NeoController.NEO_CONTROLLER id) {
        super(context);
        mController = id;
    }

    @Override
    public void onInitGL(float[] frustum) {
        initVertexData();
        initShader();
        System.arraycopy(frustum, 0, mProjMatrix, 0, 16);
    }

    @Override
    public void onFrameBegin(float[] eyes, HmdState hmdState) {
        System.arraycopy(eyes, 0, mVMatrix, 0, 16);
    }

    @Override
    public void onDrawSelf(Eye eye) {
        GLES20.glUseProgram(mProgram);
        ShaderUtil.checkGlError("glUseProgram");

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, getFinalMatrix(), 0);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);

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
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(maTexCoorHandle);
    }

    private float[] getFinalMatrix() {
        float[] mVPMatrix = new float[16];
        Matrix.multiplyMM(mVPMatrix, 0, mVMatrix, 0, MatrixTool.getCurrentMatrix(), 0);
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVPMatrix, 0);
//        if (mController == NeoController.NEO_CONTROLLER.NEO3_CONTROLLER_RIGHT | mController == NeoController.NEO_CONTROLLER.NEO3_CONTROLLER_LEFT) {
//            Matrix.scaleM(mVPMatrix, 0, 0.02f, 0.02f, 0.02f);
//        }
        return mVPMatrix;
    }

    private void initVertexData() {
        ObjVertex objVertex = null;
        switch (mController) {
            case NEO3_CONTROLLER_LEFT:
                objVertex = LoadObjUtil.loadFromAssets("neo3_controller_left.obj", mContext);
                break;
            case NEO3_CONTROLLER_RIGHT:
                objVertex = LoadObjUtil.loadFromAssets("neo3_controller_right.obj", mContext);
                break;
            case NEO2_CONTROLLER_LEFT:
                objVertex = LoadObjUtil.loadFromAssets("neo2_controller_left.obj", mContext);
                break;
            case NEO2_CONTROLLER_RIGHT:
                objVertex = LoadObjUtil.loadFromAssets("neo2_controller_right.obj", mContext);
                break;
            default:
                objVertex = LoadObjUtil.loadFromSystem("/system/media/images/controller_right.obj");
                break;
        }

        mVertexCount = objVertex.getVerticesCount();
        float[] ver = objVertex.getVertices();
        float[] uv = objVertex.getUvs();

        ByteBuffer vbb = ByteBuffer.allocateDirect(ver.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(ver);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(uv.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asFloatBuffer();
        mTexCoorBuffer.put(uv);
        mTexCoorBuffer.position(0);
    }

    private void initShader() {
        String vertexShader = ShaderUtil.loadFromAssetsFile("vertex.sh", mContext.getResources());
        String fragmentShader = ShaderUtil.loadFromAssetsFile("fragment.sh", mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexShader, fragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        if (mController == NeoController.NEO_CONTROLLER.NEO3_CONTROLLER_LEFT || mController == NeoController.NEO_CONTROLLER.NEO3_CONTROLLER_RIGHT) {
            mTexture = TextureUtil.initTexture(mContext, R.drawable.neo3_controller);
        } else {
            mTexture = TextureUtil.initTexture(mContext, R.drawable.neo2_controller);
        }
    }
}
