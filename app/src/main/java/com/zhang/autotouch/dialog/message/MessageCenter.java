package com.zhang.autotouch.dialog.message;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.zhang.autotouch.R;
import com.zhang.autotouch.actions.CheckActions;
import com.zhang.autotouch.bean.TouchEvent;
import com.zhang.autotouch.bean.TouchPoint;
import com.zhang.autotouch.conf.Const;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.naiksoftware.stomp.StompClient;

public class MessageCenter {
    public static String uuid="";
    public static void sendMessage(StompClient stompClient,Response response) throws JSONException {
        if(uuid.equals("")){
            //Bus消息提示
        }else {
            JSONObject responseObject = new JSONObject();
            responseObject.put("userID", "core");
            responseObject.put("fromUserID", uuid);
            //返回数据
            Gson gson = new Gson();
            String responseJson = gson.toJson(response);
            responseObject.put("message", responseJson);
            stompClient.send(Const.chat, responseObject.toString()).subscribe();
        }
    }

    public static String getCheckText(Context context, JSONObject jsonObject) throws JSONException, FileNotFoundException, InterruptedException {
        String text = "";
        JSONObject params= jsonObject.getJSONObject("params");
        int x1 = params.getInt("x1");
        int x2 = params.getInt("x2");
        int y1 = params.getInt("y1");
        int y2 = params.getInt("y2");
        String desc = params.getString("desc");
        CheckActions.MRectArea mRectArea = new CheckActions.MRectArea(x1,y1,x2,y2);
        text = CheckActions.getCheckText(context,mRectArea,desc);
        return text;
    }
    //第一个功能，上线，后台命令查询坐标点区域文字。
    public static void CommandCore(Context context, JSONObject jsonObject, StompClient stompClient) throws JSONException, FileNotFoundException, InterruptedException {
        String command =jsonObject.getString("command");
        if(command.equals("10")){
            JSONObject params= jsonObject.getJSONObject("params");
            String content = params.getString("content");
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }

        if(command.equals("CK1000")){
            //判断当前用户界面有没有打开，有就返回是，没有返回否
            String text = getCheckText(context,jsonObject);
            //返回数据
            sendMessage(stompClient,new Response("CKR1000",text));
        }

        if(command.equals("1000")){
            String text = getCheckText(context,jsonObject);
            //返回数据
            sendMessage(stompClient,new Response("C1010",text));
            System.out.println(text);
        }
        if(command.equals("2000")){
            JSONObject params = jsonObject.getJSONObject("params");
            MessageBlock block = getOneBlock(params);
            TouchPoint touchPoint = new TouchPoint(block);
            TouchEvent.postStartActionOnce(touchPoint);
        }
        if(command.equals("3000")){
            JSONObject params = jsonObject.getJSONObject("params");
            JSONArray list =  params.getJSONArray("blocks");
            List<MessageBlock> messageBlocks = new ArrayList<>();
            for(int i=0;i<list.length();i++){
                MessageBlock newBlock = getOneBlock(list.getJSONObject(i));
                messageBlocks.add(newBlock);
            }
            for(MessageBlock block: messageBlocks){
                TouchPoint touchPoint = new TouchPoint(block);
                TouchEvent.postStartActionOnce(touchPoint);
            }
        }
    }

    public static MessageBlock getOneBlock(JSONObject object) throws JSONException {
        MessageBlock block = new MessageBlock();
        block.x1 = object.getInt("x1");
        block.y1 = object.getInt("y1");
        block.delay = object.getInt("delay");
        block.eventName = (String) object.get("eventName");
        return block;
    }
}
