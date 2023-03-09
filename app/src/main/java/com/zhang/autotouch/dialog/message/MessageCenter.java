package com.zhang.autotouch.dialog.message;

import android.content.Context;

import com.zhang.autotouch.actions.CheckActions;
import com.zhang.autotouch.bean.TouchEvent;
import com.zhang.autotouch.bean.TouchPoint;
import com.zhang.autotouch.conf.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;

import ua.naiksoftware.stomp.StompClient;

public class MessageCenter {

    //第一个功能，上线，后台命令查询坐标点区域文字。
    public static void CommandCore(Context context, JSONObject jsonObject, StompClient stompClient) throws JSONException, FileNotFoundException, InterruptedException {
        String command =jsonObject.getString("command");
        JSONObject responseObject = new JSONObject();
        jsonObject.put("userID", "core");
        jsonObject.put("fromUserID", stompClient.getTopics());
        if(command.equals("1000")){
            JSONObject params= jsonObject.getJSONObject("params");
            int x1 = params.getInt("x1");
            int x2 = params.getInt("x2");
            int y1 = params.getInt("y1");
            int y2 = params.getInt("y2");
            CheckActions.MRectArea mRectArea = new CheckActions.MRectArea(x1,y1,x2,y2);
            String text = CheckActions.getCheckText(context,mRectArea,"星系");
            System.out.println(text);
            //返回数据
            jsonObject.put("message", text);
            stompClient.send(Const.chat, jsonObject.toString()).subscribe();
        }
        if(command.equals("2000")){
            JSONObject params = jsonObject.getJSONObject("params");
            MessageBlock block = getOneBlock(params);
            TouchPoint touchPoint = new TouchPoint(block);
            TouchEvent.postStartActionOnce(touchPoint);
        }
    }

    public static MessageBlock getOneBlock(JSONObject object) throws JSONException {
        MessageBlock block = new MessageBlock();
        block.setX1(object.getInt("x1"));
        block.y1 = object.getInt("x2");
        block.delay = object.getInt("delay");
        block.eventName = (String) object.get("eventName");
        return block;
    }
}
