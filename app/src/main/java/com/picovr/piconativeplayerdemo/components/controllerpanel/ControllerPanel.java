package com.picovr.piconativeplayerdemo.components.controllerpanel;

import android.content.Context;
import android.opengl.GLES20;

import com.picovr.piconativeplayerdemo.components.BasicComponent;
import com.picovr.piconativeplayerdemo.pickup.PickUpManager;
import com.picovr.piconativeplayerdemo.pickup.TouchableObject;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.vractivity.Eye;

public class ControllerPanel extends BasicComponent {

    private boolean mIsPlaying;
    private ControllerButton mStartButton;

    public ControllerPanel(Context context) {
        super(context);
        mStartButton = new ControllerButton(context);
        PickUpManager.getInstance().addTouchableObject(mStartButton);
    }

    public void setIsPlaying(boolean isPlaying) {
        mIsPlaying = isPlaying;
    }

    @Override
    public void onInitGL(float[] frustum) {
        mStartButton.onInitGL(frustum);
    }

    @Override
    public void onFrameBegin(float[] eyes) {
        mStartButton.onFrameBegin(eyes);
        mStartButton.setIsPlaying(mIsPlaying);
    }

    @Override
    public void onDrawSelf(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        MatrixTool.pushMatrix();
        MatrixTool.translate(-50f, 0, 0);
        MatrixTool.rotate(-90f, 1, 0, 0);
        MatrixTool.rotate(90,0,1,0);
        mStartButton.onDrawSelf(eye);
        MatrixTool.popMatrix();
    }

    public void setOnClickListener(TouchableObject.OnClickListener listener) {
        mStartButton.setOnClickListener(listener);
    }
}
