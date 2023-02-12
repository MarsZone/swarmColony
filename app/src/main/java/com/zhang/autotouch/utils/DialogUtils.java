package com.zhang.autotouch.utils;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DialogUtils {

    public static void dismiss(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    /**
     * 裁剪一定高度保留上半部分
     *
     * @param srcBitmap 原图
     * @param x         起始坐标x
     * @param y         起始坐标y
     * @return
     */
    public static Bitmap cropBitmapTop(Bitmap srcBitmap, int x, int y, int xend, int yend) {

        int width = xend-x;
        int height = yend-y;
        /**裁剪关键步骤*/
        Bitmap cropBitmap = Bitmap.createBitmap(srcBitmap, x, y, width, height);
        return cropBitmap;
    }

    /**
     * 保存bitmap到本地
     *
     * @param bitmap Bitmap
     */
    static  boolean isSave = true;
    public static void saveBitmap(Bitmap bitmap, String fName) {
        if (false == isSave){
            return;
        }
        isSave = false;
        String savePath;
        File filePic;
        savePath =  "/sdcard/Pictures/"+File.separator+fName+".jpg";
        try {
            filePic = new File(savePath);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.d("saveBitmap","Error"+e.toString());
            return;
        }
    }
}
