package com.zhang.autotouch.actions;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class BitMapUtils {
    public static int getAvgColor(Bitmap bitmap){
        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
        int pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++)
        {
            for (int x = 0; x < bitmap.getWidth(); x++)
            {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redBucket += Color.red(c);
                greenBucket += Color.green(c);
                blueBucket += Color.blue(c);
                // does alpha matter?
            }
        }

        Log.d("getAvgColor","pixelCount:"+pixelCount);
        Log.d("getAvgColor","redBucket:"+redBucket);
        Log.d("getAvgColor","greenBucket:"+greenBucket);
        Log.d("getAvgColor","blueBucket:"+blueBucket);

//        int averageColor = Color.rgb(redBucket / pixelCount,
//                greenBucket / pixelCount,
//                blueBucket / pixelCount);
        int averageColor = (redBucket/pixelCount) + (greenBucket/pixelCount) + (blueBucket/pixelCount);
        averageColor = averageColor/3;
        return averageColor;
    }
}
