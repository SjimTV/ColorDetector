package com.sjimtv.colordetector;

import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;

public class MyColor {
    private Color color;

    private int[] rgbValues;
    private int[] rgbwValues;
    private String htmlValue;
    private float[] hsvValues;

    public MyColor(int colorValue){

        // fill color Value
        this.color = Color.valueOf(colorValue);

        // fill RGB array
        int RED = (int) (color.red() * 255);
        int GREEN = (int) (color.green() * 255);
        int BLUE = (int) (color.blue() * 255);
        rgbValues = new int[] {RED, GREEN, BLUE};

        // fill RGBW array
        int WHITE = getMinValue(rgbValues);
        RED -= (WHITE / 3);
        GREEN -= (WHITE / 3);
        BLUE -= (WHITE / 3);
        rgbwValues = new int[] {RED, GREEN, BLUE, WHITE};

        // fill HTML string
        htmlValue = htmlBuilder();

        // fill HSV array
        hsvValues = new float[3];
        Color.colorToHSV(color.toArgb(), hsvValues);
    }

    public MyColor(JsonColor jsonColor){
        if (jsonColor.containsWhite()){
            rgbwCase(jsonColor);
            Log.i("CONVERTED", "RGBW");
        } else {
            rgbCase(jsonColor);
            Log.i("CONVERTED", "RGB");
        }


    }

    private void rgbwCase(JsonColor jsonColor){
        // fill RGBW Array
        int RED = jsonColor.getRed();
        int GREEN = jsonColor.getGreen();
        int BLUE = jsonColor.getBlue();
        int WHITE = jsonColor.getWhite();
        rgbwValues = new int[] {RED, GREEN, BLUE, WHITE};

        // fill RGB Array
        rgbValues = rgbwToRgb(rgbwValues);

        // fill color value
        color = Color.valueOf(jsonColor.getColor());

        // fill HTML string
        htmlValue = htmlBuilder();

        // fill HSV array
        hsvValues = new float[3];
        Color.colorToHSV(color.toArgb(), hsvValues);
    }

    private void rgbCase(JsonColor jsonColor){
        // fill RGB Array
        int RED = jsonColor.getRed();
        int GREEN = jsonColor.getGreen();
        int BLUE = jsonColor.getBlue();
        rgbValues = new int[] {RED, GREEN, BLUE};

        // fill color value
        color = Color.valueOf(jsonColor.getColor());

        // fill RGBW array
        rgbwValues = rgbToRgbw(rgbValues);

        // fill HTML string
        htmlValue = htmlBuilder();

        // fill HSV array
        hsvValues = new float[3];
        Color.colorToHSV(color.toArgb(), hsvValues);
    }

    private String htmlBuilder(){
        String agrbHtml = String.format("#%X", color.toArgb());
        StringBuilder htmlBuilder = new StringBuilder(agrbHtml);
        htmlBuilder.deleteCharAt(1);
        htmlBuilder.deleteCharAt(1);
        return htmlBuilder.toString();
    }

    public int[] getRgbValues() {
        return rgbValues;
    }

    public int[] getRgbwValues() {
        return rgbwValues;
    }

    public String getHtmlValue() {
        return htmlValue;
    }

    public int getColor() {
        return color.toArgb();
    }

    public int getComplementaryColor() {
        float RED = 1 - color.red();
        float GREEN = 1 - color.green();
        float BLUE = 1 - color.blue();
        return Color.rgb(RED, GREEN, BLUE);
    }

    public int[] getTriadicColor() {
        float RED1 = color.blue();
        float GREEN1 = color.red();
        float BLUE1 = color.green();

        float RED2 = color.green();
        float GREEN2 = color.blue();
        float BLUE2 = color.red();

        return new int[] {Color.rgb(RED1, GREEN1, BLUE1), Color.rgb(RED2, GREEN2, BLUE2)};
    }

    public int[] getMonochromaticColor(){
        float brightnessScale = 0.15f;

        int upperColor;
        int middleColor;
        int lowerColor;

        // Look for brightest color
        if (hsvValues[2] > 0.75f){
            // original color goes on top
            float middleBrightness = hsvValues[2] - brightnessScale;
            float lowerBrightness = hsvValues[2] - (brightnessScale * 2);

            upperColor = color.toArgb();
            middleColor = Color.HSVToColor(new float[]{hsvValues[0], hsvValues[1], middleBrightness});
            lowerColor = Color.HSVToColor(new float[]{hsvValues[0], hsvValues[1], lowerBrightness});

        } else if (hsvValues[2] < 0.25f){
            // original color goes on bottom
            float upperBrightness = hsvValues[2] + (brightnessScale * 2);
            float middleBrightness = hsvValues[2] + brightnessScale;

            upperColor = Color.HSVToColor(new float[]{hsvValues[0], hsvValues[1], upperBrightness});
            middleColor = Color.HSVToColor(new float[]{hsvValues[0], hsvValues[1], middleBrightness});
            lowerColor = color.toArgb();
        } else {
            // original color goes in middle
            float upperBrightness = hsvValues[2] + brightnessScale;
            float lowerBrightness = hsvValues[2] - brightnessScale;

            upperColor = Color.HSVToColor(new float[]{hsvValues[0], hsvValues[1], upperBrightness});
            middleColor = color.toArgb();
            lowerColor = Color.HSVToColor(new float[]{hsvValues[0], hsvValues[1], lowerBrightness});
        }


        return new int[] {upperColor, middleColor, lowerColor};

    }

    public int[] getAnalogousColor(){
        int upperColor;
        int middleColor;
        int lowerColor;

        upperColor = Color.HSVToColor(new float[]{getSideHue(hsvValues[0], 30), hsvValues[1], hsvValues[2]});
        middleColor = color.toArgb();
        lowerColor = Color.HSVToColor(new float[]{getSideHue(hsvValues[0], -30), hsvValues[1], hsvValues[2]});

        return new int[] {upperColor, middleColor, lowerColor};

    }

    public int[] getSplitComplementary(){
        int upperColor;
        int middleColor;
        int lowerColor;

        upperColor = Color.HSVToColor(new float[]{getSideHue(hsvValues[0], 150), hsvValues[1], hsvValues[2]});
        middleColor = color.toArgb();
        lowerColor = Color.HSVToColor(new float[]{getSideHue(hsvValues[0], -150), hsvValues[1], hsvValues[2]});

        return new int[] {upperColor, middleColor, lowerColor};

    }

    private static int getMinValue(int[] numbers){
        int minValue = numbers[0];
        for(int i=1;i<numbers.length;i++){
            if(numbers[i] < minValue){
                minValue = numbers[i];
            }
        }
        return minValue;
    }

    public static int rgbwToRgbColor(int red, int green, int blue, int white){
        Log.i("convertRGBW", "R"+red+" "+"G"+green+" "+"B"+blue+" "+"W"+white);
        float Ro = (red + (white / 3f)) / 255f;
        float Go = (green + (white / 3f)) / 255f;
        float Bo = (blue + (white / 3f)) / 255f;

        if (Ro > 1) Ro = 1;
        if (Go > 1) Go = 1;
        if (Bo > 1) Bo = 1;

        int result = Color.rgb(Ro, Go, Bo);

        Log.i("resultRGB", "R"+Color.red(result)+" "+"G"+Color.green(result)+" "+"B"+Color.blue(result));


        return result;
    }

    private static float getSideHue(float hue, int offsetDegree){

        float offsetHue = hue + offsetDegree;

        // if out of 360° start from 0°
        if (offsetHue > 360) offsetHue -= 360f;

        //if out of 0° start from 360°
        if (offsetHue < 0) offsetHue += 360f;

        return offsetHue;
    }

    public static int[] rgbwToRgb(int[] rgbw){
        int Ri = rgbw[0];
        int Gi = rgbw[1];
        int Bi = rgbw[2];
        int Wi = rgbw[3];

        int Ro = Ri + (Wi / 3) ;
        int Go = Gi + (Wi / 3);
        int Bo = Bi + (Wi / 3);

        if (Ro > 255) Ro = 255;
        if (Go > 255) Go = 255;
        if (Bo > 255) Bo = 255;


        Log.i("resultRGB", "R"+Ro+" "+"G"+Go+" "+"B"+Bo);

        return new int[] {Ro, Go, Bo};
    }

    public static int[] rgbToRgbw(int[] rgb){
        int Ri = rgb[0];
        int Gi = rgb[1];
        int Bi = rgb[2];

        //Get the maximum between R, G, and B
        float tM = Math.max(Ri, Math.max(Gi, Bi));

        //If the maximum value is 0, immediately return pure black.
        if(tM == 0) return new int[] { 0, 0, 0, 0 };

        //This section serves to figure out what the color with 100% hue is
        float multiplier = 255.0f / tM;
        float hR = Ri * multiplier;
        float hG = Gi * multiplier;
        float hB = Bi * multiplier;

        //This calculates the Whiteness (not strictly speaking Luminance) of the color
        float M = Math.max(hR, Math.max(hG, hB));
        float m = Math.min(hR, Math.min(hG, hB));
        float Luminance = ((M + m) / 2.0f - 127.5f) * (255.0f/127.5f) / multiplier;

        //Calculate the output values
        int Wo = (int)Luminance;
        int Bo = (int)(Bi - Luminance);
        int Ro = (int)(Ri - Luminance);
        int Go = (int)(Gi - Luminance);

        //Trim them so that they are all between 0 and 255
        if (Wo < 0) Wo = 0;
        if (Bo < 0) Bo = 0;
        if (Ro < 0) Ro = 0;
        if (Go < 0) Go = 0;
        if (Wo > 255) Wo = 255;
        if (Bo > 255) Bo = 255;
        if (Ro > 255) Ro = 255;
        if (Go > 255) Go = 255;


        Log.i("resultRGB", "R"+Ro+" "+"G"+Go+" "+"B"+Bo + " " + "W" + Wo);

        return new int[] {Ro, Go, Bo, Wo};
    }



}
