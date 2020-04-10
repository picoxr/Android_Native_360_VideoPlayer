package com.picovr.piconativeplayerdemo.pickup;


import android.content.Context;

import com.picovr.piconativeplayerdemo.components.BasicComponent;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.vractivity.Eye;

import androidx.annotation.CallSuper;

public abstract class TouchableObject extends BasicComponent {

    public interface OnClickListener {
        public void onClick();
    }

    private boolean mPickedUpState = false;
    private AABB3 mPreBox;
    private float[] mMatrix = new float[16];

    public TouchableObject(Context context, float[] matrix) {
		super(context);
		mPreBox = new AABB3(matrix);
    }

    public TouchableObject(Context context){
        super(context);
    }

    public void setmPreBox(float[] matrix) {
        mPreBox = new AABB3(matrix);
    }

    public abstract void onClick();
    public TouchableObject.OnClickListener mOnClickListener;
    public void setOnClickListener(TouchableObject.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public boolean ismPickedUp() {
        return mPickedUpState;
    }

    void setmPickedUpState(boolean state) {
        this.mPickedUpState = state;
    }

    AABB3 getCurrBox() {
        if (mPreBox == null) {
            return null;
        }
        return mPreBox.setToTransformedBox(mMatrix);
    }

    private void copyM() {
        System.arraycopy(MatrixTool.getCurrentMatrix(), 0, mMatrix, 0, 16);
    }

    @CallSuper
    public void onDrawSelf(Eye eye) {
        copyM();
    }

}
