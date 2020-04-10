package com.picovr.piconativeplayerdemo.utils;

import android.opengl.Matrix;
import android.util.Log;

import java.util.Stack;

public class MatrixTool {
    private static float[] mCurrentMatrix = new float[16];
    private static Stack<float[]> mMatrixStack = new Stack<>();

    static {
        Log.i("lhc", "setIdentityM mCurrentMatrix");
        Matrix.setIdentityM(mCurrentMatrix, 0);
    }

    public static void pushMatrix() {
        float[] temp = new float[16];
        System.arraycopy(mCurrentMatrix, 0, temp, 0, 16);
        mMatrixStack.push(temp);
    }

    public static void popMatrix() {
        if (mMatrixStack.size() > 0) {
            System.arraycopy(mMatrixStack.pop(), 0, mCurrentMatrix, 0, 16);
        } else {
            Matrix.setIdentityM(mCurrentMatrix, 0);
        }
    }

    public static void translate(float x, float y, float z) {
        Matrix.translateM(mCurrentMatrix, 0, x, y, z);
    }

    public static void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(mCurrentMatrix, 0, angle, x, y, z);
    }

    public static void scale(float x, float y, float z) {
        Matrix.scaleM(mCurrentMatrix, 0, x, y, z);
    }

    public static void multiplyMM(float[] matrix) {
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, mCurrentMatrix, 0, matrix, 0);
        System.arraycopy(result, 0, mCurrentMatrix, 0, 16);
    }

    public static float[] getCurrentMatrix() {
        return mCurrentMatrix;
    }

}
