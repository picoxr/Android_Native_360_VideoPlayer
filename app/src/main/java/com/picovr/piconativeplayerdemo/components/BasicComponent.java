package com.picovr.piconativeplayerdemo.components;

import android.content.Context;

import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

public abstract class BasicComponent {

    public Context mContext;
    public BasicComponent(Context context){
        mContext = context;
    }

    public abstract void onInitGL(float[] frustum);
    public abstract void onFrameBegin(float[] eyes, HmdState hmdState);
    public abstract void onDrawSelf(Eye eye);

}
