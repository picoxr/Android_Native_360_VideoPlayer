package com.picovr.piconativeplayerdemo.components.picocontroller.neo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.picovr.cvclient.CVController;
import com.picovr.cvclient.CVControllerListener;
import com.picovr.cvclient.CVControllerManager;
import com.picovr.piconativeplayerdemo.components.picocontroller.PicoController;
import com.picovr.piconativeplayerdemo.components.picocontroller.g2.Raycast;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.MatrixUtil;
import com.picovr.piconativeplayerdemo.utils.SystemPropertiesUtil;
import com.picovr.picovrlib.cvcontrollerclient.ControllerClient;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

public class NeoController extends PicoController {
    private static final boolean DEBUG = true;
    private CVControllerManager mCVControllerManager;
    private CVController mCVMainController;
    private CVController mCVSubController;
    private float[] mOrientationLeft = new float[16];
    private float[] mOrientationRight = new float[16];
    private Controller mControllerLeftModel;
    private Controller mControllerRightModel;
    private Raycast mRayCast;
    private CVControllerListener mCVControllerListener = new CVControllerListener() {
        @Override
        public void onBindSuccess() {
            setIsController(mCVMainController.getConnectState() + mCVSubController.getConnectState());
        }

        @Override
        public void onBindFail() {

        }

        @Override
        public void onThreadStart() {
            setIsController(mCVMainController.getConnectState() + mCVSubController.getConnectState());
        }

        @Override
        public void onConnectStateChanged(int i, int i1) {
            if (DEBUG) {
                Log.i("lhc", "onConnectStateChanged ： " + i + " " + ControllerClient.getMainControllerIndex());
            }
            setIsController(mCVMainController.getConnectState() + mCVSubController.getConnectState());
        }

        @Override
        public void onMainControllerChanged(int i) {
            if (DEBUG) {
                Log.i("lhc", "onMainControllerChanged ： " + i + " " + ControllerClient.getMainControllerIndex());
            }

            mCVMainController = mCVControllerManager.getMainController();
            mCVSubController = mCVControllerManager.getSubController();

        }

        @Override
        public void onChannelChanged(int i, int i1) {

        }
    };

    public NeoController(Context context, int flag) {
        super(context);
        mRayCast = new Raycast(context, 0.08f, 65, 36);
        if (3 == flag) {
            mControllerLeftModel = new Controller(context, NEO_CONTROLLER.NEO3_CONTROLLER_LEFT);
            mControllerRightModel = new Controller(context, NEO_CONTROLLER.NEO3_CONTROLLER_RIGHT);
        } else {
            mControllerLeftModel = new Controller(context, NEO_CONTROLLER.NEO2_CONTROLLER_LEFT);
            mControllerRightModel = new Controller(context, NEO_CONTROLLER.NEO2_CONTROLLER_RIGHT);
        }

        mCVControllerManager = new CVControllerManager(context);
        mCVControllerManager.setListener(mCVControllerListener);

    }

    @Override
    public void onResume() {
        mCVControllerManager.bindService();
        mCVMainController = mCVControllerManager.getMainController();
        mCVSubController = mCVControllerManager.getSubController();
        setIsController(mCVMainController.getConnectState() + mCVSubController.getConnectState());
    }

    @Override
    public void onPause() {
        mCVControllerManager.unbindService();
    }

    @Override
    public boolean getTriggerKeyEvent() {
        boolean triggerEvent;
        if (getMainIndex() == 0) {
            triggerEvent = mCVMainController.getTriggerNum() > 200;
        } else {
            triggerEvent = mCVSubController.getTriggerNum() > 200;
        }
        return triggerEvent;
    }

    @Override
    public boolean isController() {
        return mControllerState > 0;
    }

    private int getMainIndex() {
        int index = ControllerClient.getMainControllerIndex();
        return index;
    }

    @Override
    public void onInitGL(float[] frustum) {
        mRayCast.onInitGL(frustum);
        mControllerLeftModel.onInitGL(frustum);
        mControllerRightModel.onInitGL(frustum);
        Matrix.setIdentityM(mOrientationLeft, 0);
        Matrix.setIdentityM(mOrientationRight, 0);

        mCVMainController = mCVControllerManager.getMainController();
        mCVSubController = mCVControllerManager.getSubController();
    }

    @Override
    public void onFrameBegin(float[] eyes, HmdState hmdState) {
        mRayCast.onFrameBegin(eyes, hmdState);
        mControllerLeftModel.onFrameBegin(eyes, hmdState);
        mControllerRightModel.onFrameBegin(eyes, hmdState);

        if (mCVMainController.getConnectState() == 1) {
            setLeftOrientation(mCVMainController.getOrientation());
        }

        if (mCVSubController.getConnectState() == 1) {
            setRightOrientation(mCVSubController.getOrientation());
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
        if (mCVMainController.getConnectState() == 1) {
            drawController(eye, 15f, mOrientationLeft, mControllerLeftModel, getMainIndex() == 0);
        }

        if (mCVSubController.getConnectState() == 1) {
            drawController(eye, -15f, mOrientationRight, mControllerRightModel, getMainIndex() == 1);
        }
    }

    private void drawController(Eye eye, float v, float[] orientation, Controller controller, boolean isMain) {
        MatrixTool.pushMatrix();
        MatrixTool.translate(0f, v, 40f);
        MatrixTool.rotate(90, 0, 0, 1);
        MatrixTool.multiplyMM(orientation);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        MatrixTool.pushMatrix();
        MatrixTool.rotate(90, 1, 0, 0);
        MatrixTool.rotate(180, 0, 0, 1);
        MatrixTool.scale(80f, 80f, 80f);
        controller.onDrawSelf(eye);
        MatrixTool.popMatrix();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        if (isMain) {
            MatrixTool.pushMatrix();
            MatrixTool.translate(0, 6, 0);
            mRayCast.onDrawSelf(eye);
            MatrixTool.popMatrix();
        }
        MatrixTool.popMatrix();
    }

    private void setLeftOrientation(float[] orientation) {
        mOrientationLeft = MatrixUtil.quaternion2Matrix(new float[]{orientation[0], -orientation[2], orientation[1], orientation[3]});
    }

    private void setRightOrientation(float[] orientation) {
        mOrientationRight = MatrixUtil.quaternion2Matrix(new float[]{orientation[0], -orientation[2], orientation[1], orientation[3]});
    }

    private int getMainControllerIndex() {
        int index = Integer.parseInt(SystemPropertiesUtil.getSystemProperties("persist.pvrcon.main.controller", "-1"));
        return index;
    }

    public enum NEO_CONTROLLER {
        NEO2_CONTROLLER_LEFT,
        NEO2_CONTROLLER_RIGHT,
        NEO3_CONTROLLER_LEFT,
        NEO3_CONTROLLER_RIGHT
    }
}
