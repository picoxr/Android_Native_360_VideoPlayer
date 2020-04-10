package com.picovr.piconativeplayerdemo.components;

import android.content.Context;

import com.picovr.vractivity.Eye;

public abstract class BasicComponent {

    public Context mContext;
    public BasicComponent(Context context){
        mContext = context;
    }

    public abstract void onInitGL(float[] frustum);
    public abstract void onFrameBegin(float[] eyes);
    public abstract void onDrawSelf(Eye eye);

}
