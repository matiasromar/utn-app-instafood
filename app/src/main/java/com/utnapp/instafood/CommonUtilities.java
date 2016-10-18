package com.utnapp.instafood;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class CommonUtilities {
    public static String BitMapToString(Bitmap image) {
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.NO_WRAP);
        return temp;
    }

    public static Bitmap StringToBitMap(String image) {
        byte [] encodeByte=Base64.decode(image,Base64.NO_WRAP);
        Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        return bitmap;
    }
}
