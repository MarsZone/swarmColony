package com.zhang.autotouch.utils;

import android.util.Log;


import static com.zhang.autotouch.conf.Const.TAG;

import java.util.TreeMap;

import ua.naiksoftware.stomp.StompClient;

public class StompUtils {
    @SuppressWarnings({"ResultOfMethodCallIgnored", "CheckResult"})
    public static void lifecycle(StompClient stompClient) {
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d(TAG, "Stomp connection opened");
                    TreeMap<String, String> map = lifecycleEvent.getHandshakeResponseHeaders();
                    String s = map.get("Set-Cookie");
                    break;
                case ERROR:
                    Log.e(TAG, "Error", lifecycleEvent.getException());
                    break;

                case CLOSED:
                    Log.d(TAG, "Stomp connection closed");
                    break;
            }
        });
    }
}
