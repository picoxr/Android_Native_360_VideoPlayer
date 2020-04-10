package com.picovr.piconativeplayerdemo;

import android.content.Context;

import com.picovr.piconativeplayerdemo.components.BasicComponent;


public abstract class PicoController extends BasicComponent {

    public int mControllerState = 0;

    public PicoController(Context context) {
        super(context);

    }

    public abstract void onResume();

    public abstract  void onPause();

    public abstract boolean getTriggerKeyEvent();

    public void setIsController(int controllerState) {
        mControllerState = controllerState ;
    }

    public abstract boolean isController();
}
