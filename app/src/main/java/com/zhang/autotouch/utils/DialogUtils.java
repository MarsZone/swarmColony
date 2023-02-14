package com.zhang.autotouch.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    /**
     * 将assets中的识别库复制到SD卡中
     * @param path  要存放在SD卡中的 完整的文件名。这里是"/storage/emulated/0/tessdata/chi_sim.traineddata"
     * @param name  assets中的文件名 这里是 "chi_sim.traineddata"
     */
    public static void copyToSD(Context context, String path, String name) {
        String TAG = "copy";
        Log.i(TAG, "copyToSD: "+path);
        Log.i(TAG, "copyToSD: "+name);

        //如果存在就删掉
        File f = new File(path);
        if (f.exists()){
            f.delete();
        }
        if (!f.exists()){
            File p = new File(f.getParent());
            if (!p.exists()){
                p.mkdirs();
            }
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        InputStream is=null;
        OutputStream os=null;
        try {
            is = context.getAssets().open(name);
            File file = new File(path);
            os = new FileOutputStream(file);
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
