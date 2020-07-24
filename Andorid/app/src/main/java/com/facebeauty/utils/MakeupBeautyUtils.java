package com.facebeauty.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

import com.amnix.skinsmoothness.AmniXSkinSmooth;
public class MakeupBeautyUtils {
    private final AmniXSkinSmooth amniXSkinSmooth = AmniXSkinSmooth.getInstance();

    public MakeupBeautyUtils(){
    }

    public Bitmap handle(final Bitmap bitmap){
        amniXSkinSmooth.storeBitmap(bitmap, false);
        amniXSkinSmooth.initSdk();
        amniXSkinSmooth.startFullBeauty(10, 10);
//        amniXSkinSmooth.startSkinSmoothness(200);
//        amniXSkinSmooth.startSkinWhiteness(10);
        amniXSkinSmooth.unInitSdk();
        Bitmap result = amniXSkinSmooth.getBitmapAndFree();
        return result;
    }

    public void destroy(){
        if(amniXSkinSmooth != null) {
            amniXSkinSmooth.onDestroy();
        }
    }
}
