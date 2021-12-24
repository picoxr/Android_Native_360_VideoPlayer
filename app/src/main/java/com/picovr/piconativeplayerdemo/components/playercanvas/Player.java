package com.picovr.piconativeplayerdemo.components.playercanvas;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.view.Surface;

import com.picovr.piconativeplayerdemo.components.BasicComponent;
import com.picovr.piconativeplayerdemo.pickup.TouchableObject;
import com.picovr.piconativeplayerdemo.utils.MatrixTool;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;

import java.io.IOException;

public class Player extends BasicComponent implements SurfaceTexture.OnFrameAvailableListener, TouchableObject.OnClickListener {

    private Player360 mPlayer360;
    private MediaPlayer mMediaPlayer;
    private SurfaceTexture mSurfaceTexture;
    private boolean mPlayerPrepared;
    private boolean mUpdateSurface;
    private PLAY_TYPE mCurrentPlayType = PLAY_TYPE.PLAY_TYPE_360;
    public Player(Context context, String videoPath) {
        super(context);
        initMediaPlayer(videoPath);
        mPlayer360 = new Player360(context);
    }

    private void initMediaPlayer(String videoPath) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext, Uri.parse(videoPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setLooping(true);

        if (!mPlayerPrepared) {
            try {
                mMediaPlayer.prepare();
                mPlayerPrepared = true;
            } catch (IOException t) {
            }
        }
    }

    @Override
    public void onClick() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onInitGL(float[] frustum) {
        mPlayer360.onInitGL(frustum);
        if (mCurrentPlayType == PLAY_TYPE.PLAY_TYPE_360) {
            setMediaPlayerSurface(mPlayer360.getTextureId());
        }
    }

    @Override
    public void onFrameBegin(float[] eyes, HmdState hmdState) {
        mPlayer360.onFrameBegin(eyes, hmdState);
    }

    @Override
    public void onDrawSelf(Eye eye) {
        GLES20.glEnable(GLES20.GL_CULL_FACE); //Open back clipping
        GLES20.glCullFace(GLES20.GL_FRONT);

        synchronized (this) {
            if (mUpdateSurface) {
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mPlayer360.getTransformMatrix());
                mUpdateSurface = false;
            }
        }
        MatrixTool.pushMatrix();
        mPlayer360.onDrawSelf(eye);
        MatrixTool.popMatrix();
        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }

    private void setMediaPlayerSurface(int textureId) {
        mSurfaceTexture = new SurfaceTexture(textureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        Surface surface = new Surface(mSurfaceTexture);
        mMediaPlayer.setSurface(surface);
        surface.release();
//        mMediaPlayer.start();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mUpdateSurface = true;
    }

    public void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void onResume() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void onDestroy() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public enum PLAY_TYPE {
        PLAY_TYPE_360
    }
}