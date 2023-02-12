package com.zhang.autotouch.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.zhang.autotouch.R;
import com.zhang.autotouch.TouchEventManager;
import com.zhang.autotouch.adapter.TouchPointAdapter;
import com.zhang.autotouch.bean.TouchEvent;
import com.zhang.autotouch.bean.TouchPoint;
import com.zhang.autotouch.utils.DensityUtil;
import com.zhang.autotouch.utils.DialogUtils;
import com.zhang.autotouch.utils.GsonUtils;
import com.zhang.autotouch.utils.ScreentShotUtil;
import com.zhang.autotouch.utils.SpUtils;
import com.zhang.autotouch.utils.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MenuDialog extends BaseServiceDialog implements View.OnClickListener {

    private Button btStop;
    private RecyclerView rvPoints;

    private AddPointDialog addPointDialog;
    private Listener listener;
    private TouchPointAdapter touchPointAdapter;
    private RecordDialog recordDialog;

    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String tessdata = DATAPATH + "tessdata";

    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static final String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

    public MenuDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_menu;
    }

    @Override
    protected int getWidth() {
        return DensityUtil.dip2px(getContext(), 450);
    }

    @Override
    protected int getHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @Override
    protected void onInited() {
        setCanceledOnTouchOutside(true);
        findViewById(R.id.bt_exit).setOnClickListener(this);
        findViewById(R.id.bt_add).setOnClickListener(this);
        findViewById(R.id.bt_record).setOnClickListener(this);
        findViewById(R.id.bt_clear).setOnClickListener(this);
        btStop = findViewById(R.id.bt_stop);
        btStop.setOnClickListener(this);
        rvPoints = findViewById(R.id.rv);
        touchPointAdapter = new TouchPointAdapter();
        touchPointAdapter.setOnItemClickListener(new TouchPointAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, TouchPoint touchPoint) {
                btStop.setVisibility(View.VISIBLE);
                dismiss();
                TouchEvent.postStartAction(touchPoint);
                ToastUtil.show("已开启触控点：" + touchPoint.getName());
            }
        });
        rvPoints.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPoints.setAdapter(touchPointAdapter);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (TouchEventManager.getInstance().isPaused()) {
                    TouchEvent.postContinueAction();
                }
            }
        });
        copyToSD(LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("悬浮菜单启动", "onStart");
        //如果正在触控，则暂停
        TouchEvent.postPauseAction();
        if (touchPointAdapter != null) {
            List<TouchPoint> touchPoints = SpUtils.getTouchPoints(getContext());
            Log.d("数据", GsonUtils.beanToJson(touchPoints));
            touchPointAdapter.setTouchPointList(touchPoints);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("click","vid"+v.getId());
        switch (v.getId()) {
            case R.id.bt_add:
                DialogUtils.dismiss(addPointDialog);
                addPointDialog = new AddPointDialog(getContext());
                addPointDialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        MenuDialog.this.show();
                    }
                });
                addPointDialog.show();
                dismiss();
                break;
            case R.id.bt_clear:
                Log.d("menu","clear"+getContext().toString());
                SpUtils.clear(getContext());
                List<TouchPoint> touchPoints = SpUtils.getTouchPoints(getContext());
                Log.d("数据", GsonUtils.beanToJson(touchPoints));
                touchPointAdapter.setTouchPointList(touchPoints);
                break;
            case R.id.bt_record:
                dismiss();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String fileName = format.format(new Date(System.currentTimeMillis())) + ".jpg";
                fileName ="/sdcard/Pictures/"+fileName;
                ScreentShotUtil.getInstance().takeScreenshot(getContext(),fileName);
                FileInputStream fis = null;
                try {
                    Log.d("ReadImg","ReadStart"+System.currentTimeMillis());
                    Thread.sleep(1000);
                    fis = new FileInputStream(fileName);
                    Bitmap bitmap  = BitmapFactory.decodeStream(fis);
                    Bitmap isInStation = DialogUtils.cropBitmapTop(bitmap,1174,218,1250,256);
                    DialogUtils.saveBitmap(isInStation,"isInStation");
                    TessBaseAPI tessBaseAPI = new TessBaseAPI();
                    tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
                    tessBaseAPI.setImage(isInStation);
                    String text = tessBaseAPI.getUTF8Text();
                    Log.i("ReadPict", System.currentTimeMillis()+"|Context:" + text);
                } catch (FileNotFoundException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case R.id.bt_stop:
                btStop.setVisibility(View.GONE);
                TouchEvent.postStopAction();
                ToastUtil.show("已停止触控");
                break;
            //                dismiss();
//                if (listener != null) {
//                    listener.onFloatWindowAttachChange(false);
//                    if (recordDialog ==null) {
//                        recordDialog = new RecordDialog(getContext());
//                        recordDialog.setOnDismissListener(new OnDismissListener() {
//                            @Override
//                            public void onDismiss(DialogInterface dialog) {
//                                listener.onFloatWindowAttachChange(true);
//                                MenuDialog.this.show();
//                            }
//                        });
//                        recordDialog.show();
//                    }
//                }
            case R.id.bt_exit:
                TouchEvent.postStopAction();
                if (listener != null) {
                    listener.onExitService();
                }
                break;

        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        /**
         * 悬浮窗显示状态变化
         * @param attach
         */
        void onFloatWindowAttachChange(boolean attach);

        /**
         * 关闭辅助
         */
        void onExitService();
    }

    /**
     * 将assets中的识别库复制到SD卡中
     * @param path  要存放在SD卡中的 完整的文件名。这里是"/storage/emulated/0/tessdata/chi_sim.traineddata"
     * @param name  assets中的文件名 这里是 "chi_sim.traineddata"
     */
    public void copyToSD(String path, String name) {
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
            is = getContext().getAssets().open(name);
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
