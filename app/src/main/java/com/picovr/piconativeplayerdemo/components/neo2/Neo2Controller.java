package com.picovr.piconativeplayerdemo.components.neo2;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.picovr.cvclient.CVController;
import com.picovr.cvclient.CVControllerListener;
import com.picovr.cvclient.CVControllerManager;
import com.picovr.piconativeplayerdemo.PicoController;
import com.picovr.piconativeplayerdemo.components.g2.Raycast;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.MatrixUtil;
import com.picovr.piconativeplayerdemo.utils.SystemPropertiesUtil;
import com.picovr.picovrlib.cvcontrollerclient.ControllerClient;
import com.picovr.vractivity.Eye;
import com.psmart.vrlib.VrActivity;

public class Neo2Controller extends PicoController {
    public enum NEO2_CONTROLLER {
        NEO_CONTROLLER_LEFT,
        NEO_CONTROLLER_RIGHT
    }

    private static final boolean DEBUG = true;

    private CVControllerManager mCVControllerManager;
    private CVController mCVMainController;
    private CVController mCVSubController;

    //    private boolean mIsLeftMain = false;
    private float[] mOrientationLeft = new float[16];
    private float[] mOrientationRight = new float[16];

    private Controller mControllerLeftModel;
    private Controller mControllerRightModel;
    private Raycast mRayCast;

    public Neo2Controller(Context context) {
        super(context);
        mRayCast = new Raycast(context, 0.08f, 65, 36);
        mControllerLeftModel = new Controller(context, NEO2_CONTROLLER.NEO_CONTROLLER_LEFT);
        mControllerRightModel = new Controller(context, NEO2_CONTROLLER.NEO_CONTROLLER_RIGHT);

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
        //        if (mIsLeftMain) {
        triggerEvent = mCVMainController.getTriggerNum() > 200;
        //        } else {
        //            triggerEvent = mCVSubController.getTriggerNum() > 200;
        //        }
        return triggerEvent;
    }

    @Override
    public boolean isController() {
        return mControllerState > 0;
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
        //        if (DEBUG)
        //            Log.i("lhc", "onInitGL ： " + VrActivity.getPvrHandness(mContext) + " " + ControllerClient.getMainControllerIndex());

    }

    private int getMainIndex() {
        int index = ControllerClient.getMainControllerIndex();
        return index;
    }

    private int getMainControllerIndex() {
        int index = Integer.parseInt(SystemPropertiesUtil.getSystemProperties("persist.pvrcon.main.controller","-1"));
        return index;
    }

    @Override
    public void onFrameBegin(float[] eyes) {
        mRayCast.onFrameBegin(eyes);
        mControllerLeftModel.onFrameBegin(eyes);
        mControllerRightModel.onFrameBegin(eyes);
        if (DEBUG) {
//            Log.i("lhc", "onFrameBegin : getPvrHandness" + VrActivity.getPvrHandness(mContext));
            Log.i("lhc", "onFrameBegin : getMainIndex  " + getMainIndex());
            Log.i("lhc", "onFrameBegin : Main.getSerialNum  " + mCVMainController.getSerialNum());
            Log.i("lhc", "onFrameBegin : Sub.getSerialNum  " + mCVSubController.getSerialNum());
            Log.i("lhc", "onFrameBegin : getMainControllerIndex " + getMainControllerIndex());
        }

        if (mControllerState == 1) {
            if (getMainControllerIndex() == 0) {
                setLeftOrientation(mCVMainController.getOrientation());
            } else if (getMainControllerIndex() == 1) {
                setRightOrientation(mCVMainController.getOrientation());
            }
        } else if (mControllerState == 2) {
            if (mCVMainController.getSerialNum() == 0) {
                setLeftOrientation(mCVMainController.getOrientation());
                setRightOrientation(mCVSubController.getOrientation());
            } else if (mCVMainController.getSerialNum() == 1) {
                setRightOrientation(mCVMainController.getOrientation());
                setLeftOrientation(mCVSubController.getOrientation());
            }
        }

    }

    @Override
    public void onDrawSelf(Eye eye) {

        if (mControllerState == 1) {
            if (getMainControllerIndex() == 0) {
                drawController(eye, 15f, mOrientationLeft, mControllerLeftModel, true);
            } else if (getMainControllerIndex() == 1) {
                drawController(eye, -15f, mOrientationRight, mControllerRightModel, true);
            }
        } else if (mControllerState == 2) {
            drawController(eye, 15f, mOrientationLeft, mControllerLeftModel, getMainIndex() == 0);
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

        MatrixTool.pushMatrix();
        MatrixTool.rotate(90, 1, 0, 0);
        MatrixTool.rotate(180, 0, 0, 1);
        MatrixTool.scale(80f, 80f, 80f);
        controller.onDrawSelf(eye);
        MatrixTool.popMatrix();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

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

    private CVControllerListener mCVControllerListener = new CVControllerListener() {
        @Override
        public void onBindFail() {

        }

        @Override
        public void onThreadStart() {
            setIsController(mCVMainController.getConnectState() + mCVSubController.getConnectState());
        }

        @Override
        public void onConnectStateChanged(int i, int i1) {
            if (DEBUG)
                Log.i("lhc", "onConnectStateChanged ： " + i + " " + ControllerClient.getMainControllerIndex() );
            setIsController(mCVMainController.getConnectState() + mCVSubController.getConnectState());
        }

        @Override
        public void onMainControllerChanged(int i) {
            //            mIsLeftMain = VrActivity.getPvrHandness(mContext) == 1;
            if (DEBUG)
                Log.i("lhc", "onMainControllerChanged ： " + i + " " + ControllerClient.getMainControllerIndex());

            //            setIsLeftMain();
            mCVMainController = mCVControllerManager.getMainController();
            mCVSubController = mCVControllerManager.getSubController();

        }
    };
}
