package com.zhang.autotouch.bean;

import android.util.Log;

import androidx.annotation.NonNull;

import com.zhang.autotouch.utils.GsonUtils;

import org.greenrobot.eventbus.EventBus;

public class TouchEvent {

    public static final int ACTION_START = 1;
    public static final int ACTION_PAUSE = 2;
    public static final int ACTION_CONTINUE = 3;
    public static final int ACTION_STOP = 4;
    public static final int ACTION_CAPTURE=5;
    public static final int ACTION_START_ONCE = 6;
    public static final int ACTION_START_ONCE_DONE = 7;
    private int action;
    private TouchPoint touchPoint;

    private TouchEvent(int action) {
        this(action, null);
    }

    private TouchEvent(int action, TouchPoint touchPoint) {
        this.action = action;
        this.touchPoint = touchPoint;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public TouchPoint getTouchPoint() {
        return touchPoint;
    }

    public static void postStartAction(TouchPoint touchPoint) {
        postAction(new TouchEvent(ACTION_START, touchPoint));
    }
    public static void postStartActionOnce(TouchPoint touchPoint) {
        postAction(new TouchEvent(ACTION_START_ONCE, touchPoint));
    }
    public static void postStartActionOnceDone() {
        postAction(new TouchEvent(ACTION_START_ONCE_DONE));
    }
    public static void postPauseAction() {
        postAction(new TouchEvent(ACTION_PAUSE));
    }

    public static void postContinueAction() {
        postAction(new TouchEvent(ACTION_CONTINUE));
    }

    public static void postCapture(){
        postAction(new TouchEvent(ACTION_CAPTURE));
    }

    public static void postStopAction() {
        postAction(new TouchEvent(ACTION_STOP));
    }

    private static void postAction(TouchEvent touchEvent) {
        Log.d( "postAction",touchEvent.toString());
        EventBus.getDefault().post(touchEvent);
    }

    @NonNull
    @Override
    public String toString() {
        return "action=" + action + " touchPoint=" + (touchPoint == null ? "null" : GsonUtils.beanToJson(touchPoint));
    }
}
