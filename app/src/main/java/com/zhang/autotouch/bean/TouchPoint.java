package com.zhang.autotouch.bean;

import com.zhang.autotouch.dialog.message.MessageBlock;

public class TouchPoint {
    private String name;
    private int x;
    private int y;
    private int delay;

    private int hasNext;

    public TouchPoint(String name, int x, int y, int delay) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.hasNext = 0;
        this.delay = delay;
    }
    public TouchPoint(MessageBlock block){
        this.name = block.getEventName();
        this.x = block.getX1();
        this.y = block.getY1();
        this.hasNext = block.getHasNext();
        this.delay = block.getDelay();
    }
    public TouchPoint(String name, int x, int y, int delay,int hasNext) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.hasNext =hasNext;
        this.delay = delay;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDelay() {
        return delay;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getHasNext() {
        return hasNext;
    }

    public void setHasNext(int hasNext) {
        this.hasNext = hasNext;
    }
}
