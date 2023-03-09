package com.zhang.autotouch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhang.autotouch.bean.TouchEvent;
import com.zhang.autotouch.fw_permission.FloatWinPermissionCompat;
import com.zhang.autotouch.service.AutoTouchService;
import com.zhang.autotouch.service.FloatingService;
import com.zhang.autotouch.utils.AccessibilityUtil;
import com.zhang.autotouch.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;


@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    private TextView tvStart;
    private final String STRING_START = "开始";
    private final String STRING_ACCESS = "辅助功能";
    private final String STRING_ALERT = "悬浮窗权限";/**
     * 权限请求值
     */
    private static final int PERMISSION_REQUEST_CODE=0;


    private Handler handler = new Handler(Looper.getMainLooper());

    public static final int REQUEST_MEDIA_PROJECTION = 10001;

    public MediaProjectionManager mProjectionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStart = findViewById(R.id.tv_start);
        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (tvStart.getText().toString()) {
                    case STRING_START:
                        ToastUtil.show(getString(R.string.app_name) + "已启用");
                        startService(new Intent(MainActivity.this, FloatingService.class));
                        moveTaskToBack(true);
                        break;
                    case STRING_ALERT:
                        requestPermissionAndShow();
                        break;
                    case STRING_ACCESS:
                        requestAcccessibility();
                        break;
                    default:
                        break;
                }
            }
        });
        ToastUtil.init(this);

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Button capBtn = findViewById(R.id.capbtn);
        capBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mProjectionManager != null) {
                    startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                }
            }
        });
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReciverTouchEventMain(TouchEvent event) {
        Log.d("MainActivityTouchEvent",event.toString());
        if(event.getAction()==TouchEvent.ACTION_CAPTURE){
            Log.d("MainActivityTouchEvent","cap");
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("WOW", "on result : requestCode = " + requestCode + " resultCode = " + resultCode +" data:"+data.toString());
        if (requestCode == REQUEST_MEDIA_PROJECTION && data != null) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data);
            DisplayMetrics outMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
            final int screenWidth = outMetrics.widthPixels;
            final int screenHeight = outMetrics.heightPixels;
            @SuppressLint("WrongConstant") ImageReader imageReader = ImageReader.newInstance(
                    screenWidth,screenHeight,PixelFormat.RGBA_8888, 1);
            VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay("screen-mirror",
                    screenWidth,
                    screenHeight,
                    outMetrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(), null, null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Image image = imageReader.acquireLatestImage();
                    if (image == null) {
                        return;
                    }
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * screenWidth;

                    Bitmap bitmap = Bitmap.createBitmap(screenWidth + rowPadding / pixelStride,
                            screenHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    String fileName = null;
                    try {
                        Date currentDate = new Date();
                        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddhhmmss");
                        File dir = getExternalFilesDir(null);
                        fileName = dir.getAbsolutePath() + "/" + date.format(currentDate) + ".png";
                        Log.d("file",fileName);
                        FileOutputStream fos = new FileOutputStream(fileName);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    image.close();
                    mediaProjection.stop();
                    virtualDisplay.release();

                    // 使用截图 bitmap
                }
            }, 500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkState();
    }

    private void checkState() {
        boolean hasAccessibility = AccessibilityUtil.isSettingOpen(AutoTouchService.class, MainActivity.this);
        boolean hasWinPermission = FloatWinPermissionCompat.getInstance().check(this);
        if (hasAccessibility) {
            if (hasWinPermission) {
                tvStart.setText(STRING_START);
            } else {
                tvStart.setText(STRING_ALERT);
            }
        } else {
            tvStart.setText(STRING_ACCESS);
        }
    }

    private void requestAcccessibility() {
        new AlertDialog.Builder(this).setTitle("无障碍服务未开启")
                .setMessage("你的手机没有开启无障碍服务，" + getString(R.string.app_name) + "将无法正常使用")
                .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 显示授权界面
                        try {
                            AccessibilityUtil.jumpToSetting(MainActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    /**
     * 开启悬浮窗权限
     */
    private void requestPermissionAndShow() {
        new AlertDialog.Builder(this).setTitle("悬浮窗权限未开启")
                .setMessage(getString(R.string.app_name) + "获得悬浮窗权限，才能正常使用应用")
                .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 显示授权界面
                        try {
                            FloatWinPermissionCompat.getInstance().apply(MainActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("取消", null).show();
    }
}
