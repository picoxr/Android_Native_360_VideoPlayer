package com.picovr.piconativeplayerdemo.pickup;

import android.opengl.Matrix;

import java.util.ArrayList;

/**
 * PickUp Manager
 *
 * @author licky
 */
public class PickUpManager {

    private float mZNear = 0.1f, mZFar = 1000.0f;
    private ArrayList<TouchableObject> mOBList;

    private static PickUpManager mPickUpManager;

    public static PickUpManager getInstance(){
        if (mPickUpManager == null) {
            mPickUpManager = new PickUpManager();
        }
        return mPickUpManager;
    }

    public void setNearAndFar(float zNear, float zFar) {
        mZNear = zNear;
        mZFar = zFar;
    }

    private PickUpManager() {
        mOBList = new ArrayList<>();
    }

    public synchronized void addTouchableObject(Object obj) {
        if (mOBList != null && obj instanceof TouchableObject) {
            mOBList.add((TouchableObject) obj);
        }
    }

    public synchronized TouchableObject getTouchableObject(int index) {
        if (mOBList == null || index < 0 || index >= mOBList.size()) {
            return null;
        }
        return mOBList.get(index);
    }

    public synchronized void removeTouchableObject(int index) {
        if (mOBList == null || index < 0 || index >= mOBList.size()) {
            return;
        }
        mOBList.remove(index);
    }

    public synchronized void replaceTouchableObject(int index, TouchableObject obj) {
        if (mOBList == null || index < 0 || index >= mOBList.size()) {
            return;
        }
        mOBList.set(index, obj);
    }

    public synchronized void replaceTouchableObject(int start, ArrayList<TouchableObject> arr) {
        if (mOBList == null || start < 0 || start + arr.size() >= mOBList.size()) {
            return;
        }
        for (int i = 0; i < arr.size(); i++) {
            mOBList.set(start + i, arr.get(i));
        }
    }

    private synchronized int getOBsSize() {
        if (mOBList == null) {
            mOBList = new ArrayList<TouchableObject>();
        }
        return mOBList.size();
    }

    public synchronized void clearOBs() {
        if (mOBList != null) {
            mOBList.clear();
        } else {
            mOBList = new ArrayList<TouchableObject>();
        }
    }

    public int getPickUpIndex(boolean isController) {
        float[] AB;
        if (!isController) {
            AB = calculateHeadABPosition(mZNear, mZFar);
        } else {
            AB = calculateControllerABPosition(mZNear, mZFar);
        }
        Vector3f start = new Vector3f(AB[0], AB[1], AB[2]);
        Vector3f end = new Vector3f(AB[3], AB[4], AB[5]);
        Vector3f dir = end.minus(start);
        int tmpIndex = -1;
        float minProportion = 1;
        TouchableObject objPickUped = null;
        TouchableObject objTmp = null;
        int size = getOBsSize();
        for (int i = 0; i < size; i++) {
            objTmp = getTouchableObject(i);
            if (objTmp == null) {
                continue;
            }
            objTmp.setmPickedUpState(false);
            AABB3 box = objTmp.getCurrBox();
            if (box == null) {
                continue;
            }
            float t = box.rayIntersect(start, dir, null);
            if (t <= minProportion) {
                minProportion = t;
                tmpIndex = i;
                objPickUped = objTmp;
            }
        }

        if (objPickUped != null) {
            objPickUped.setmPickedUpState(true);
        }
        return tmpIndex;
    }

    private float[] calculateHeadABPosition(float near, float far) {
        float[] A = calculateHeadRay(new float[]{0, 0, -near});
        float[] B = calculateHeadRay(new float[]{0, 0, -far});
        return new float[]{A[0], A[1], A[2], B[0], B[1], B[2]};
    }

    private float[] mRay = new float[6];
    public void setControllerRay(float[] ray) {
        System.arraycopy(ray,0,mRay,0,6);
    }

    private float[] calculateControllerABPosition(float near, float far) {
        float[] rayA = new float[]{mRay[0], mRay[1],mRay[2]};
        float[] rayB = new float[]{mRay[3], mRay[4],mRay[5]};
        float[] G = calculateControllerRay1(rayA);
        float[] H = calculateControllerRay1(rayB);
        float[] rayGH = new float[]{G[0], G[1], G[2], H[0], H[1], H[2]};
        return rayGH;
    }

    private float[] calculateHeadRay(float[] p) {
        float[] inverM = getHeadInvertMvMatrix();
        float[] preP = new float[4];
        Matrix.multiplyMV(preP, 0, inverM, 0, new float[]{p[0], p[1], p[2], 1}, 0);
        return new float[]{preP[0], preP[1], preP[2]};
    }

    private float[] calculateControllerRay(float[] p) {
        float[] inverM = getHeadInvertMvMatrix();
        float[] preP = new float[4];
        Matrix.multiplyMV(preP, 0, inverM, 0, new float[]{p[0], p[1], p[2], 1}, 0);
        return new float[]{preP[0], preP[1], preP[2]};
    }

    private float[] calculateControllerRay1(float[] p) {
        float[] preP = new float[4];
        Matrix.multiplyMV(preP, 0, mPickUpMatrix, 0, new float[]{p[0], p[1], p[2], 1}, 0);
        return new float[]{preP[0], preP[1], preP[2]};
    }

    private float[] mPickUpMatrix = new float[16];

    public void setPickUpMatrix(float[] matrix) {
        System.arraycopy(matrix, 0, mPickUpMatrix, 0, 16);
    }

    private float[] getHeadInvertMvMatrix() {
        float[] invM = new float[16];
        Matrix.invertM(invM, 0, mPickUpMatrix, 0);
        return invM;
    }
}
