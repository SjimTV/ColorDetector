package com.sjimtv.colordetector;

import android.graphics.Color;

public class JsonColor {

    private String name;

    private int red;
    private int green;
    private int blue;
    private int white = -1;

    public JsonColor(){

    }

    public JsonColor(String name, int color){
        this.name = name;
        red = Color.red(color);
        green = Color.green(color);
        blue = Color.blue(color);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getWhite(){
        return white;
    }

    public void setWhite(int white) {
        this.white = white;
    }

    public int getColor(){
        if (white != -1) {
            return MyColor.rgbwToRgbColor(red, green, blue, white);
        } else return Color.rgb(red, green, blue);
    }

    public boolean containsWhite() {
        return white != -1;
    }
}

