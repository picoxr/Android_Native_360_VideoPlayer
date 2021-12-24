package com.picovr.piconativeplayerdemo.components.picocontroller.neo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.picovr.cvclient.CVController;
import com.picovr.cvclient.CVControllerListener;
import com.picovr.cvclient.CVControllerManager;
import com.picovr.piconativeplayerdemo.components.picocontroller.PicoController;
import com.picovr.piconativeplayerdemo.components.picocontroller.Raycast;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.MatrixUtil;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

/**
 * @author Admin
 */
public class NeoController extends PicoController {

    private final CVControllerManager mCVControllerManager;
    private final Controller mControllerRightModel;
    private final Controller mControllerLeftModel;
    private final Raycast mRayCast;
    private CVController mCVRightController;
    private CVController mCVLeftController;
    private float[] mOrientationRight = new float[16];
    private float[] mOrientationLeft = new float[16];

    public NeoController(Context context, int flag) {
        super(context);
        mRayCast = new Raycast(context, 0.002f, 65, 36);
        if (3 == flag) {
            mControllerRightModel = new Controller(context, NEO_CONTROLLER.NEO3_CONTROLLER_RIGHT);
            mControllerLeftModel = new Controller(context, NEO_CONTROLLER.NEO3_CONTROLLER_LEFT);
        } else {
            mControllerRightModel = new Controller(context, NEO_CONTROLLER.NEO2_CONTROLLER_RIGHT);
            mControllerLeftModel = new Controller(context, NEO_CONTROLLER.NEO2_CONTROLLER_LEFT);
        }

        mCVControllerManager = new CVControllerManager(context);
        CVControllerListener mCVControllerListener = new CVControllerListener() {
            @Override
            public void onBindSuccess() {
                setIsController(mCVRightController.getConnectState() + mCVLeftController.getConnectState());
            }

            @Override
            public void onBindFail() {

            }

            @Override
            public void onThreadStart() {
                setIsController(mCVRightController.getConnectState() + mCVLeftController.getConnectState());
            }

            @Override
            public void onConnectStateChanged(int i, int i1) {
                setIsController(mCVRightController.getConnectState() + mCVLeftController.getConnectState());
            }

            @Override
            public void onMainControllerChanged(int i) {
                mCVRightController = mCVControllerManager.getRightController();
                mCVLeftController = mCVControllerManager.getLeftController();

            }

            @Override
            public void onChannelChanged(int i, int i1) {

            }
        };
        mCVControllerManager.setListener(mCVControllerListener);

    }

    @Override
    public void onResume() {
        mCVControllerManager.bindService();
        mCVRightController = mCVControllerManager.getRightController();
        mCVLeftController = mCVControllerManager.getLeftController();
        setIsController(mCVRightController.getConnectState() + mCVLeftController.getConnectState());
    }

    @Override
    public void onPause() {
        mCVControllerManager.unbindService();
    }

    @Override
    public boolean getTriggerKeyEvent() {
        return mCVRightController.getTriggerNum() > 200 | mCVLeftController.getTriggerNum() > 200;
    }

    @Override
    public boolean isController() {
        return mControllerState > 0;
    }

    @Override
    public void onInitGL(float[] frustum) {
        mRayCast.onInitGL(frustum);
        mControllerRightModel.onInitGL(frustum);
        Matrix.setIdentityM(mOrientationRight, 0);
        mControllerLeftModel.onInitGL(frustum);
        Matrix.setIdentityM(mOrientationLeft, 0);

        mCVRightController = mCVControllerManager.getRightController();
        mCVLeftController = mCVControllerManager.getLeftController();
    }

    @Override
    public void onFrameBegin(float[] eyes, HmdState hmdState) {
        mRayCast.onFrameBegin(eyes, hmdState);
        mControllerRightModel.onFrameBegin(eyes, hmdState);
        mControllerLeftModel.onFrameBegin(eyes, hmdState);

        if (mCVRightController.getConnectState() == 1) {
            setRightOrientation(mCVRightController.getOrientation());
        }

        if (mCVLeftController.getConnectState() == 1) {
            setLeftOrientation(mCVLeftController.getOrientation());
        }

        float[] hmdOrientation = hmdState.getOrientation();
        float[] hmdPosition = hmdState.getPos();
        float[] hmdData = new float[7];
        hmdData[0] = hmdOrientation[0];
        hmdData[1] = hmdOrientation[1];
        hmdData[2] = hmdOrientation[2];
        hmdData[3] = hmdOrientation[3];

        hmdData[4] = hmdPosition[0];
        hmdData[5] = hmdPosition[1];
        hmdData[6] = hmdPosition[2];

        mCVControllerManager.updateControllerData(hmdData);
    }

    @Override
    public void onDrawSelf(Eye eye) {
        if (mCVRightController.getConnectState() == 1) {
            drawController(eye, mCVRightController.getPosition(), mOrientationRight, mControllerRightModel, true);
        }

        if (mCVLeftController.getConnectState() == 1) {
            drawController(eye, mCVLeftController.getPosition(), mOrientationLeft, mControllerLeftModel, false);
        }
    }

    private void drawController(Eye eye, float[] pos, float[] orientation, Controller controller, boolean isMain) {
        MatrixTool.pushMatrix();
        MatrixTool.translate(pos[2], -pos[0], -pos[1]);
        MatrixTool.rotate(90, 0, 0, 1);
        MatrixTool.multiplyMM(orientation);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        MatrixTool.pushMatrix();
        MatrixTool.rotate(90, 1, 0, 0);
        MatrixTool.rotate(180, 0, 0, 1);
        MatrixTool.scale(0.01f, 0.01f, 0.01f);
        controller.onDrawSelf(eye);
        MatrixTool.popMatrix();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        if (isMain) {
            MatrixTool.pushMatrix();
            MatrixTool.translate(0, 0.09f, -0.025f);
            mRayCast.onDrawSelf(eye);
            MatrixTool.popMatrix();
        }
        MatrixTool.popMatrix();
    }

    private void setRightOrientation(float[] orientation) {
        mOrientationRight = MatrixUtil.quaternion2Matrix(new float[]{orientation[0], -orientation[2], orientation[1], orientation[3]});
    }

    private void setLeftOrientation(float[] orientation) {
        mOrientationLeft = MatrixUtil.quaternion2Matrix(new float[]{orientation[0], -orientation[2], orientation[1], orientation[3]});
    }

    public enum NEO_CONTROLLER {
        NEO2_CONTROLLER_LEFT,
        NEO2_CONTROLLER_RIGHT,
        NEO3_CONTROLLER_LEFT,
        NEO3_CONTROLLER_RIGHT
    }
}
