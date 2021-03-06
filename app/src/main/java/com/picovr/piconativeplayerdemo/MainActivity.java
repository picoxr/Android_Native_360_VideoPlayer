package com.picovr.piconativeplayerdemo;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.picovr.piconativeplayerdemo.components.CrossHair;
import com.picovr.piconativeplayerdemo.components.controllerpanel.ControllerPanel;
import com.picovr.piconativeplayerdemo.components.picocontroller.PicoController;
import com.picovr.piconativeplayerdemo.components.picocontroller.g2.Hummingbird;
import com.picovr.piconativeplayerdemo.components.picocontroller.neo2.Neo2Controller;
import com.picovr.piconativeplayerdemo.components.playercanvas.Player;
import com.picovr.piconativeplayerdemo.pickup.PickUpManager;
import com.picovr.piconativeplayerdemo.pickup.TouchableObject;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.piconativeplayerdemo.utils.MatrixUtil;
import com.picovr.piconativeplayerdemo.utils.ShaderUtil;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;
import com.picovr.vractivity.RenderInterface;
import com.picovr.vractivity.VRActivity;

public class MainActivity extends VRActivity implements RenderInterface {

    private static final boolean DEBUG = false;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.0f;

    private ControllerPanel mControllerPanel;
    private Player mPlayer;
    private PicoController mPicoController;
    private CrossHair mCrossHair;
    private float[] mCamera = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PickUpManager.getInstance().setNearAndFar(Z_NEAR, Z_FAR);
//        nativeSetMultisamples(nativePtr,2);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.test360;
        mPlayer = new Player(this, videoPath);
        mCrossHair = new CrossHair(this);
        mControllerPanel = new ControllerPanel(this);
        mControllerPanel.setOnClickListener(mPlayer);
        if (Build.MODEL.toLowerCase().contains("g2")) {
            mPicoController = new Hummingbird(this);
        } else if (Build.MODEL.toLowerCase().contains("neo")) {
            mPicoController = new Neo2Controller(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPicoController.onResume();
        mPlayer.onResume();
    }

    @Override
    protected void onPause() {
        mPicoController.onPause();
        mPlayer.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mPlayer.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (DEBUG)
            Log.i("lhc", "onKeyDown" + keyCode);
        if (keyCode == 1001) {
            click();
        }
        return super.onKeyDown(keyCode, event);
    }

    private float[] mHeadView = new float[16];

    @Override
    public void onFrameBegin(HmdState hmdState) {
        float[] headOrientation = hmdState.getOrientation();
        float[] headView = MatrixUtil.quaternion2Matrix(headOrientation);
        Matrix.invertM(headView, 0, headView, 0);
        ShaderUtil.checkGlError("onReadyToDraw");
        float[] eyes = new float[16];
        Matrix.multiplyMM(eyes, 0, headView, 0, mCamera, 0);
        if (!mPicoController.isController()) {
            PickUpManager.getInstance().setPickUpMatrix(eyes);
            mCrossHair.onFrameBegin(eyes);
        } else {
            mPicoController.onFrameBegin(eyes);
        }
        mPlayer.onFrameBegin(eyes);
        mControllerPanel.onFrameBegin(eyes);
        mControllerPanel.setIsPlaying(mPlayer.isPlaying());

        setOrientation(headOrientation);
    }

    public void setOrientation(float[] orientation) {
        mHeadView = MatrixUtil.quaternion2Matrix(new float[]{orientation[2], -orientation[0], -orientation[1], orientation[3]});
    }

    private boolean trigerClick = false;

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        GLES20.glEnable(GLES20.GL_CULL_FACE);
//        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        MatrixTool.pushMatrix();
        mPlayer.onDrawSelf(eye);
        MatrixTool.popMatrix();

        MatrixTool.pushMatrix();
        mControllerPanel.onDrawSelf(eye);
        MatrixTool.popMatrix();

        if (mPicoController.isController()) {
            MatrixTool.pushMatrix();
            mPicoController.onDrawSelf(eye);
            MatrixTool.popMatrix();
        } else {
            MatrixTool.pushMatrix();
            MatrixTool.multiplyMM(mHeadView);
            MatrixTool.translate(-48f, 0, 0);
            MatrixTool.rotate(180f, 0, 0, 1);
            MatrixUtil.logMatrix("Headview ", mHeadView);
            mCrossHair.onDrawSelf(eye);
            MatrixTool.popMatrix();
        }
        checkPickUp();
    }

    private void checkPickUp() {
        int pick = PickUpManager.getInstance().getPickUpIndex(mPicoController.isController());
        if (pick >= 0) {
            if (DEBUG)
                Log.i("lhc", "getPickUpIndex : " + pick);
            TouchableObject object = PickUpManager.getInstance().getTouchableObject(pick);
            setClick(object);
            if (mPicoController.isController()) {
                if (mPicoController.getTriggerKeyEvent() && !trigerClick) {
                    trigerClick = true;
                    click();
                }
            }
        } else {
            setClick(null);
        }
        if (!mPicoController.getTriggerKeyEvent()) {
            trigerClick = false;
        }
    }

    private TouchableObject mClickObject;

    private void setClick(TouchableObject object) {
        mClickObject = object;
    }

    private void click() {
        if (mClickObject != null) {
            mClickObject.onClick();
        }
    }

    @Override
    public void onFrameEnd() {

    }

    @Override
    public void onTouchEvent() {

    }

    @Override
    public void onRenderPause() {

    }

    @Override
    public void onRenderResume() {

    }

    @Override
    public void onRendererShutdown() {

    }

    @Override
    public void initGL(int i, int i1) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glViewport(0, 0, i, i1);
        GLES20.glClearColor(0f, 0.0f, 0.0f, 1f);
        Matrix.setLookAtM(mCamera, 0, 0f, 0.0f, 0.0f, -1.0f, 0f, 0f, 0f, 0.0f, -1.0f);
        float[] frustum = setFrustumM(Z_NEAR, Z_FAR, 51.f, 51.f, 51.f, 51.f, 0);
        mPlayer.onInitGL(frustum);
        mPicoController.onInitGL(frustum);
        mCrossHair.onInitGL(frustum);
        mControllerPanel.onInitGL(frustum);
    }

    private float[] setFrustumM(float near, float far, float left, float right, float bottom, float top, int offset) {
        float[] frustum = new float[16];
        float l = (float) (-Math.tan(Math.toRadians(left))) * near;
        float r = (float) Math.tan(Math.toRadians(right)) * near;
        float b = (float) (-Math.tan(Math.toRadians(bottom))) * near;
        float t = (float) Math.tan(Math.toRadians(top)) * near;
        Matrix.frustumM(frustum, offset, l, r, b, t, near, far);
        return frustum;
    }

    @Override
    public void renderEventCallBack(int i) {

    }

    @Override
    public void surfaceChangedCallBack(int i, int i1) {

    }


}
