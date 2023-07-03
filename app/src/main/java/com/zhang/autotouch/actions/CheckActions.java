package com.zhang.autotouch.actions;

import static com.zhang.autotouch.dialog.MenuDialog.DATAPATH;
import static com.zhang.autotouch.dialog.MenuDialog.DEFAULT_LANGUAGE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.zhang.autotouch.utils.DialogUtils;
import com.zhang.autotouch.utils.ScreentShotUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckActions {
    //Todo 封装坐标
    public static boolean isFull(Context context) throws FileNotFoundException, InterruptedException {
        String fileName = saveCurrentScreen(context,"1","isFullTemp");
        Thread.sleep(500);
        //读取整个图。。
        FileInputStream fis = null;
        fis = new FileInputStream(fileName);
        Bitmap orgBitmap  = BitmapFactory.decodeStream(fis);
        Bitmap checkArea = DialogUtils.cropBitmapTop(orgBitmap,760,687,773,699);
        int avgColor = BitMapUtils.getAvgColor(checkArea);
        Log.d("FullCheck",""+avgColor);
        //63 22
        if(avgColor>=60 && avgColor<=70){
            return true;
        }else{
            return false;
        }
//        if(avgColor>=18 && avgColor<=26){
//            return false;
//        }
    }
    public static boolean isFullFast(Context context) throws FileNotFoundException, InterruptedException {
        String fileName = saveCurrentScreen(context,"1","isFullTemp");
        Thread.sleep(500);
        //读取整个图。。
        FileInputStream fis = null;
        fis = new FileInputStream(fileName);
        Bitmap orgBitmap  = BitmapFactory.decodeStream(fis);
        Bitmap checkArea = DialogUtils.cropBitmapTop(orgBitmap,34,96,35,97);
        int avgColor = BitMapUtils.getAvgColor(checkArea);
        Log.d("FullCheck",""+avgColor);
        if(avgColor>=0 && avgColor<=8){
            return false;
        }else{
            return false;
        }
//        if(avgColor>=18 && avgColor<=26){
//            return false;
//        }
    }
    public static class MRectArea{
        public int x;
        public int y;
        public int xEnd;
        public int yEnd;
        public MRectArea(int _x, int _y, int _xEnd, int _yEnd){
            x=_x;y=_y;xEnd=_xEnd;yEnd=_yEnd;
        }
    }
    public static boolean isInStation(Context context) throws InterruptedException, FileNotFoundException {
        MRectArea mRectArea = new MRectArea(1174,218,1250,256);
        String text = getCheckText(context,mRectArea,"isInStation");
        Log.i("ReadPict", System.currentTimeMillis()+"|Context:" + text);
        if(text.equals("离站")||text.equals("中止")){
            return true;
        }else {
            return false;
        }
    }
    public static boolean isSpaceJumping(Context context) throws InterruptedException, FileNotFoundException {
        MRectArea mRectArea = new MRectArea(605,677,641,691);
        String text = getCheckText(context,mRectArea,"speed");
        Log.i("ReadPict", System.currentTimeMillis()+"|Context:" + text);
        if(text.equals("眯")||text.equals("米")){
            return false;
        }else {
            return true;
        }
    }

    public static String getCheckText(Context context,MRectArea mRectArea,String checkDesc) throws InterruptedException, FileNotFoundException {
        String fileName = saveCurrentScreen(context,"1","checkTextTemp");
        //读取整个图。。
        FileInputStream fis = null;
        fis = new FileInputStream(fileName);
        Bitmap orgBitmap  = BitmapFactory.decodeStream(fis);
        Bitmap cachesBitmap  = DialogUtils.cropBitmapTop(orgBitmap,mRectArea.x,mRectArea.y,mRectArea.xEnd,mRectArea.yEnd);
        DialogUtils.saveBitmap(cachesBitmap,checkDesc);
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
        tessBaseAPI.setImage(cachesBitmap);
        String text = tessBaseAPI.getUTF8Text();
        return text;
    }


    public static String saveCurrentScreen(Context context,String type,String name){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName="";
        if(type.equals("")){
            fileName= format.format(new Date(System.currentTimeMillis())) + ".jpg";
        }
        if(type.equals("1")){
            fileName= name + ".jpg";
        }
        fileName ="/sdcard/Pictures/"+fileName;
        ScreentShotUtil.getInstance().takeScreenshot(context,fileName);
        Log.d("ReadImg","ReadStart"+System.currentTimeMillis());
        return fileName;
    }
}
