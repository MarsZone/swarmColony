package com.zhang.autotouch.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhang.autotouch.R;
import com.zhang.autotouch.TouchEventManager;
import com.zhang.autotouch.actions.CheckActions;
import com.zhang.autotouch.actions.ProcessActions;
import com.zhang.autotouch.adapter.TouchPointAdapter;
import com.zhang.autotouch.bean.TouchEvent;
import com.zhang.autotouch.bean.TouchPoint;
import com.zhang.autotouch.conf.Const;
import com.zhang.autotouch.dialog.message.MessageCenter;
import com.zhang.autotouch.utils.DensityUtil;
import com.zhang.autotouch.utils.DialogUtils;
import com.zhang.autotouch.utils.GsonUtils;
import com.zhang.autotouch.utils.SpUtils;
import com.zhang.autotouch.utils.StompUtils;
import com.zhang.autotouch.utils.ToastUtil;
import com.zhang.autotouch.utils.UuidGen;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

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
    public static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    public static final String tessdata = DATAPATH + "tessdata";

    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    public static final String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * assets中的文件名
     */
    public static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    public static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

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

    public String curProcess="";
    public String curNode="";
    TextView commandTextView;
    @Override
    protected void onInited() {
        setCanceledOnTouchOutside(true);
        findViewById(R.id.bt_exit).setOnClickListener(this);
        findViewById(R.id.bt_add).setOnClickListener(this);
        findViewById(R.id.bt_record).setOnClickListener(this);
        findViewById(R.id.bt_clear).setOnClickListener(this);
        findViewById(R.id.bt_action_open).setOnClickListener(this);
        findViewById(R.id.bt_check_kc).setOnClickListener(this);
        btStop = findViewById(R.id.bt_stop);
        btStop.setOnClickListener(this);
        rvPoints = findViewById(R.id.rv);
        commandTextView = findViewById(R.id.txcommond);
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
        DialogUtils.copyToSD(getContext(),LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
//        Log.d("悬浮菜单启动", "onStart");
        //如果正在触控，则暂停
        TouchEvent.postPauseAction();
        if (touchPointAdapter != null) {
            List<TouchPoint> touchPoints = SpUtils.getTouchPoints(getContext());
//            Log.d("数据", GsonUtils.beanToJson(touchPoints));
            touchPointAdapter.setTouchPointList(touchPoints);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverTouchEventMain(TouchEvent event) throws InterruptedException, FileNotFoundException {
        if(event.getAction()==TouchEvent.ACTION_START_ONCE_DONE){
            if(curProcess.equals(ProcessActions.MiningProcess)){
                String behaviour = ProcessActions.processMining(curNode,false);
                if(behaviour.equals("checkIsFull")){
                    boolean isFull = false;
                    isFull = CheckActions.isFull(getContext());
                    curNode="校验存储空间_step_2";
                    behaviour = ProcessActions.processMining(curNode,isFull);
                    if(behaviour.equals("unloadOre")){
                        curNode="卸货_1";
                        ProcessActions.unloadOre();
                    }
                    if(behaviour.equals("unFull")){
                        curNode="待出站";
                        ProcessActions.closeInventoryUI();
                    }
                }
                if(behaviour.equals("closeCurUI")){
                    curNode="待出站";
                    ProcessActions.closeInventoryUI();
                }
                if(behaviour.equals("waitLevelStation")){
                    curNode="待跃迁_矿区_1";
                    ProcessActions.levelStation();
                }
                if(behaviour.equals("waitJumpToMine")){
                    tempCheckHandler.postDelayed(runnableIsInStationCheck, 1500);
                }
                if(behaviour.equals("waitJumpArrive")){
                    tempCheckHandler.postDelayed(runnableIsArrivedCheck,5000);
                }
                if(behaviour.equals("end")){
                    Log.d("流程","库存未满结束");
                    curNode="流程结束";
                    ProcessActions.closeInventoryUI();
                }
            }
        }
    }

    Handler tempCheckHandler=new Handler();
    public Runnable runnableIsArrivedCheck = new Runnable() {
        @Override
        public void run() {
            try {
                boolean isSpaceJumping = CheckActions.isSpaceJumping(getContext());
                if(isSpaceJumping){
                    Log.d("isSpaceJumping","跃迁中");
                    tempCheckHandler.postDelayed(this,4000);
                }else {
                    Log.d("isSpaceJumping","跃迁完成");
                    //到达目的地开始挖矿
                    ProcessActions.beginMiningProcess();
                    tempCheckHandler.postDelayed(runnableIsInventoryFullCheck,8000);
                }

            } catch (InterruptedException | FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    };
    public Runnable runnableIsInventoryFullCheck = new Runnable() {
        @Override
        public void run() {
            try {
                boolean isFull = CheckActions.isFullFast(getContext());
                if(isFull){
                    //满了就回程
                    Log.d("IsInventory","回程");
                    ProcessActions.goBack();
                    tempCheckHandler.postDelayed(runnableIsGoBack,5000);
                }else{
                    //不满就继续
                    Log.d("IsInventory","继续挖");
                    ProcessActions.randomChangeTargetProcess();
                    //随机换个矿挖
                    Random random = new Random();
                    int nextTime = random.nextInt(3)+1;
                    tempCheckHandler.postDelayed(this,nextTime*30000);
                }
            } catch (FileNotFoundException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };
    public final Runnable runnableAgain = new Runnable() {
        @Override
        public void run() {
            try {
                ProcessActions.openInventory();
                curNode = "校验存储空间_step_1";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };
    public final Runnable runnableIsGoBack = new Runnable() {
        @Override
        public void run() {
            try {
                boolean isInStation = CheckActions.isInStation(getContext());
                if (isInStation) {
                    Log.d("IsInventory", "回城了");
                    tempCheckHandler.postDelayed(runnableAgain,10000);
                } else {
                    Log.d("IsInventory", "没到家");
                    tempCheckHandler.postDelayed(this, 5000);
                }
            } catch (InterruptedException | FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    };
    public Runnable runnableIsInStationCheck=new Runnable() {
        @Override
        public void run() {
            //判断是否在站内
            try {
                boolean isInStation = CheckActions.isInStation(getContext());
                if(isInStation){
                    //站内，每1.5秒校验一次
//                    Log.d("isInStationCheck","仍然在站内");
                    tempCheckHandler.postDelayed(this, 1500);
                }else{
//                    Log.d("isInStationCheck","已出站");
                    curNode="等待到达";
                    ProcessActions.goToMindPos();
                    //触发，前往矿带操作
                    //已出站
                    tempCheckHandler.removeCallbacks(runnableIsInStationCheck);
                }

            } catch (InterruptedException | FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    Handler updateTextViewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String busMessage = msg.obj.toString();
            //更新UI线程
            commandTextView.append(busMessage);
        }
    };
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
            case R.id.bt_action_open:
                dismiss();
                StompClient stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Const.address);
                StompUtils.lifecycle(stompClient);
                Toast.makeText(getContext(),"Start connecting to server", Toast.LENGTH_SHORT).show();
                // Connect to WebSocket server
                List<StompHeader> headers = new ArrayList<>();
                String uuid= UuidGen.generateShortUuid();
                StompHeader stompHeader = new StompHeader("uuid", uuid);
                headers.add(stompHeader);
                stompClient.connect(headers);
                StompUtils.lifecycle(stompClient);
                Context context = getContext();
                //绑定私聊
                stompClient.topic(Const.chatResponse.replace(Const.placeholder, uuid)).subscribe(stompMessage -> {
                    JSONObject jsonObject = new JSONObject(stompMessage.getPayload());
                    Log.i(Const.TAG, "Receive: " + jsonObject.toString());
                    Message message = new Message();
                    message.obj = jsonObject.getString("responseMessage") + "\n";
                    updateTextViewHandler.sendMessage(message);
                    MessageCenter.CommandCore(context,jsonObject,stompClient);
                });
                // 绑定广播
                Log.i(Const.TAG, "Subscribe broadcast endpoint to receive response");
                stompClient.topic(Const.broadcastResponse).subscribe(stompMessage -> {
                    JSONObject jsonObject = new JSONObject(stompMessage.getPayload());
                    Log.i(Const.TAG, "Receive: " + stompMessage.getPayload());
                    Message message = new Message();
                    message.obj = jsonObject.getString("responseMessage") + "\n";
//                    updateTextViewHandler.sendMessage(message);
                });

                break;
            case R.id.bt_check_kc:
                //校验库存量
                dismiss();
                try {
                    boolean isFull = CheckActions.isFullFast(getContext());
                } catch (FileNotFoundException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case R.id.bt_record:
                dismiss();
                //检查库存情况
                Log.d("流程","开启页面");
                try {
                    curProcess =ProcessActions.MiningProcess;
                    ProcessActions.openInventory();
                    curNode="校验存储空间_step_1";
                } catch ( InterruptedException e) {
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
                tempCheckHandler.removeCallbacks(runnableIsInStationCheck);
                tempCheckHandler.removeCallbacks(runnableIsGoBack);
                tempCheckHandler.removeCallbacks(runnableIsArrivedCheck);
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
}
