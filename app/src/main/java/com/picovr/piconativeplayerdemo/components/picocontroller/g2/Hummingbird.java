package com.picovr.piconativeplayerdemo.components.picocontroller.g2;

import android.content.Context;
import android.opengl.Matrix;
import android.util.Log;

import com.picovr.client.HbController;
import com.picovr.client.HbListener;
import com.picovr.client.HbManager;
import com.picovr.client.Orientation;
import com.picovr.piconativeplayerdemo.components.picocontroller.PicoController;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.MatrixUtil;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

public class Hummingbird extends PicoController {

    private static final float HEIGHT = 11;
    private static final int N = 36;

    private HbManager mHbManager;
    private HbController mHbController;

    private float[] mOrientation = new float[16];
    private Raycast mRayCast;
    private Controller mController;
    private HbListener mHbListener = new HbListener() {
        @Override
        public void onConnect() {
            setIsController(2);
            Log.i("lhc", "onConnect : update  true");
        }

        @Override
        public void onDisconnect() {
            setIsController(0);
            Log.i("lhc", "onDisconnect : update false");
        }

        @Override
        public void onDataUpdate() {
            setIsController(mHbController.getConnectState());
            setOrientation(mHbController.getOrientation());
        }

        @Override
        public void onReCenter() {

        }

        @Override
        public void onBindService() {

        }
    };

    public Hummingbird(Context context) {
        super(context);
        mRayCast = new Raycast(context, 0.08f, 65, N);
        mController = new Controller(context);

        mHbManager = new HbManager(context);
        mHbManager.InitServices();
        mHbManager.setHbListener(mHbListener);
        mHbController = mHbManager.getHbController();
        mHbController.update();
    }

    @Override
    public void onResume() {
        mHbManager.Resume();
        mHbController.startUpdateThread();
        setIsController(mHbController.getConnectState());
    }

    @Override
    public void onPause() {
        mHbController.stopUpdateThread();
        mHbManager.Pause();
    }

    @Override
    public boolean getTriggerKeyEvent() {
        Log.i("lhc", "mHbController.getTrigerKeyEvent() " + mHbController.getTrigerKeyEvent());
        return mHbController.getTrigerKeyEvent() == 1;
    }

    @Override
    public boolean isController() {
        return mControllerState == 2;
    }

    @Override
    public void onInitGL(float[] frustum) {
        mRayCast.onInitGL(frustum);
        mController.onInitGL(frustum);
        Matrix.setIdentityM(mOrientation, 0);
    }

    @Override
    public void onFrameBegin(float[] eyes, HmdState hmdState) {
        mRayCast.onFrameBegin(eyes, hmdState);
        mController.onFrameBegin(eyes, hmdState);
    }

    @Override
    public void onDrawSelf(Eye eye) {
        MatrixTool.pushMatrix();
        MatrixTool.translate(0f, -15f, 40f);
        MatrixTool.rotate(90, 0, 0, 1);
        MatrixTool.multiplyMM(mOrientation);

        MatrixTool.pushMatrix();
        MatrixTool.rotate(90, 1, 0, 0);
        MatrixTool.rotate(180, 0, 0, 1);
        MatrixTool.scale(1000f, 1000f, 1000f);
        mController.onDrawSelf(eye);
        MatrixTool.popMatrix();

        MatrixTool.pushMatrix();
        MatrixTool.translate(0, HEIGHT / 2, 0);
        mRayCast.onDrawSelf(eye);
        MatrixTool.popMatrix();

        MatrixTool.popMatrix();
    }

    private void setOrientation(Orientation orientation) {
        mOrientation = MatrixUtil.quaternion2Matrix(new float[]{orientation.x, -orientation.z, orientation.y, orientation.w});
    }
}
