package com.picovr.piconativeplayerdemo.utils;

import android.util.Log;

public class MatrixUtil {

    public static float[] quaternion2Matrix(float[] Q) {
        float x = -1.0f * Q[0];
        float y = -1.0f * Q[1];
        float z = -1.0f * Q[2];
        float w = Q[3];
        float ww = w * w;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;

        float[] M = new float[16];
        M[0] = (ww + xx - yy - zz);
        M[1] = 2 * (x * y - w * z);
        M[2] = 2 * (x * z + w * y);
        M[3] = 0.f;

        M[4] = 2 * (x * y + w * z);
        M[5] = (ww - xx + yy - zz);
        M[6] = 2 * (y * z - w * x);
        M[7] = 0.f;

        M[8] = 2 * (x * z - w * y);
        M[9] = 2 * (y * z + w * x);
        M[10] = (ww - xx - yy + zz);
        M[11] = 0.f;

        M[12] = 0.0f;
        M[13] = 0.0f;
        M[14] = 0.0f;
        M[15] = 1.f;
        return M;
    }

    public static void logMatrix(String tag, float[] Q) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < Q.length; i++) {
            sb.append(Q[i]).append(", ");
        }
        Log.i("lhc", tag + " " + sb.toString());
    }
}
