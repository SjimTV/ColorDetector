package com.sjimtv.colordetector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class ColorAnalyzer implements ImageAnalysis.Analyzer{

    private DetectActivity activity;
    private ArrayList<Color> colors;

    private boolean Alive = true;

    public ColorAnalyzer(Activity activity){
        this.activity = (DetectActivity) activity;
        colors = new ArrayList<>();
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Bitmap bitmap = convertPlanesToBitmap(image);
        int resultColor = averageColor(getDominantColor(bitmap));
        int inverseResultColor = inverseColor(resultColor);
        if (Alive){
            activity.updateResultColor(resultColor, inverseResultColor);
            image.close();
        } else {
            activity.setResultColor(resultColor,inverseResultColor, bitmap);
        }
    }

    public void stop(){
        Alive = false;
    }

    private Bitmap convertPlanesToBitmap(ImageProxy image){
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }


    private int getDominantColor(Bitmap bitmap){
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    private int averageColor(int color){
        colors.add(Color.valueOf(color));
        if (colors.size() > 5){
            colors.remove(0);
        }

        float a = 0;
        float r = 0;
        float g = 0;
        float b = 0;

        for (Color c : colors) {
            a += c.alpha();
            r += c.red();
            g += c.green();
            b += c.blue();
        }

        a /= colors.size();
        r /= colors.size();
        g /= colors.size();
        b /= colors.size();

        return Color.argb(a, r, g, b);
    }

    private int inverseColor(int color){
        Color c = Color.valueOf(color);
        float[] colours = new float[4];

        //if (c.alpha() == 1) colours[0] = 1f - c.alpha(); else colours[0] = 0.01f;
        colours[0] = 1;
        if (c.red() != 1) colours[1] = 1f - c.red(); else colours[1] = 0.01f;
        if (c.green() != 1) colours[2] = 1f - c.green(); else colours[2] = 0.01f;
        if (c.blue() != 1) colours[3] = 1f - c.blue(); else colours[3] = 0.01f;

        return Color.argb(colours[0], colours[1], colours[2], colours[3]);
    }
}