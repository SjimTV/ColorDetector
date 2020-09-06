package com.sjimtv.colordetector;

import java.util.ArrayList;

public class JsonColorWrapper {
    private ArrayList<JsonColor> colorLibrary;

    public JsonColorWrapper() {

    }

    public void setColorLibrary(ArrayList<JsonColor> colorLibrary) {
        this.colorLibrary = colorLibrary;
    }

    public ArrayList<JsonColor> getColorLibrary() {
        return colorLibrary;
    }
}
