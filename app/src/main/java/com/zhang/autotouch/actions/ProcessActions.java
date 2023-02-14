package com.zhang.autotouch.actions;

import com.zhang.autotouch.R;
import com.zhang.autotouch.bean.TouchEvent;
import com.zhang.autotouch.bean.TouchPoint;

import java.util.Random;

public class ProcessActions {
    public static String MiningProcess= "挖矿";
    public static String processMining(String curNode,boolean check){
        if(curNode.equals("校验存储空间_step_1")){
            return "checkIsFull";
        }
        if(curNode.equals("校验存储空间_step_2")){
            if(check){
                return "unloadOre";
            }else{
                //库存不满，结束
                return "unFull";
            }
        }
        if(curNode.equals("卸货_1")){
            return "closeCurUI";
        }
        if(curNode.equals("待出站")){
            return "waitLevelStation";
        }
        if(curNode.equals("待跃迁_矿区_1")){
            return "waitJumpToMine";
        }
        if(curNode.equals("等待到达")){
            return "waitJumpArrive";
        }
        return "";
    }
    public static void openInventory() throws InterruptedException {
        TouchPoint touchPoint = new TouchPoint("库存界面",27,116,500,1);
        TouchEvent.postStartActionOnce(touchPoint);
    }

    public static void unloadOre() throws InterruptedException {
        TouchPoint touchPoint = new TouchPoint("卸货_全选",983,657,500);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("卸货_移动至",145,151,1500);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("卸货_物品机库",478,162,2500,1);
        TouchEvent.postStartActionOnce(touchPoint);
    }

    public static void closeInventoryUI() throws InterruptedException {
        TouchPoint touchPoint = new TouchPoint("关闭",1236,40,1000,1);
        TouchEvent.postStartActionOnce(touchPoint);
    }
    public static void levelStation() throws InterruptedException {
        TouchPoint touchPoint = new TouchPoint("离站",1161,242,1000,1);
        TouchEvent.postStartActionOnce(touchPoint);
    }
    public static TouchPoint firstRow = new TouchPoint("",1093,95,0);
    //站外
    public static void goToMindPos() throws InterruptedException{
        TouchPoint touchPoint = new TouchPoint("打开概览",1231,404,2000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("选项",1045,30,3500);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("矿点",1074,486,5000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("小行星带",1251,83,6000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("第一个小行星带",firstRow.getX(),firstRow.getY(),7000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("跃迁",844,174,8000,1);
        TouchEvent.postStartActionOnce(touchPoint);
    }
    //采矿
    public static void beginMiningProcess()throws InterruptedException{
        TouchPoint touchPoint = new TouchPoint("矿点",1252,209,1000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("第一个",firstRow.getX(),firstRow.getY(),2000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("环绕",845,250,3000);
        TouchEvent.postStartActionOnce(touchPoint);

        touchPoint = new TouchPoint("启动矿机1",846,659,4500);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("启动矿机2",937,660,5500);
        TouchEvent.postStartActionOnce(touchPoint);
    }

    public static void randomChangeTargetProcess()throws InterruptedException{
        TouchPoint touchPoint = new TouchPoint("矿点",1252,209,1000);
        TouchEvent.postStartActionOnce(touchPoint);
        Random random = new Random();
        int next = random.nextInt(3);
        touchPoint = new TouchPoint("第一个",firstRow.getX(),firstRow.getY()+next*70,2000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("环绕",845,250+next*70,3000);
        TouchEvent.postStartActionOnce(touchPoint);
    }

    public static void goBack()throws InterruptedException{
        TouchPoint touchPoint = new TouchPoint("空间站",1249,147,1000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("第一个",firstRow.getX(),firstRow.getY(),2000);
        TouchEvent.postStartActionOnce(touchPoint);
        touchPoint = new TouchPoint("停靠",845,93,3000,1);
        TouchEvent.postStartActionOnce(touchPoint);
    }
}
