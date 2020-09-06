package com.sjimtv.colordetector;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ColorLibraryActivity extends AppCompatActivity {

    private final int LEE_LIBRARY = 0;
    private final int ROSCO_LIBRARY = 1;
    private final int SPIIDER_LIBRARY = 2;
    private final int USER_LIBRARY = 3;

    private MyColor baseColor;
    private JsonColor returnColor;

    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView colorLibraryView;

    private ArrayList<JsonColor> leeLibrary;
    private ArrayList<JsonColor> roscoLibrary;
    private ArrayList<JsonColor> spiiderLibrary;
    private ArrayList<JsonColor> userLibrary;

    private int libraryActive;

    SharedPreferences sharedPreferences;

    ConstraintLayout backgroundLayout;

    ImageView bladesBackground;
    Button returnWithColorButton;
    TextView changeLibraryButton;

    private AnimatedVectorDrawable animatedCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_library);

        sharedPreferences = getSharedPreferences("com.sjimtv.colordetector", 0);
        baseColor = new MyColor(getIntent().getIntExtra("color", Color.rgb(0.5f, 0.5f, 0.5f)));
        returnColor = new JsonColor("untitled", baseColor.getColor());

        loadViews();
        loadDrawables();
        loadLibrarys();
        fullScreen();

        setupRecyclerView();
    }

    private void loadViews() {
        backgroundLayout = findViewById(R.id.backgroundLayout);
        backgroundLayout.setBackgroundColor(baseColor.getColor());

        bladesBackground = findViewById(R.id.bladesBackground);
        bladesBackground.getDrawable().setTint(returnColor.getColor());
        returnWithColorButton = findViewById(R.id.detectColorButton);

        changeLibraryButton = findViewById(R.id.changeLibraryButton);
    }

    private void loadDrawables() {
        animatedCapture = (AnimatedVectorDrawable) getDrawable(R.drawable.animated_eye_closing);
    }

    private void loadLibrarys() {
        leeLibrary = colorLibraryFromAsset("leeLibrary.json");
        roscoLibrary = colorLibraryFromAsset("roscoLibrary.json");
        spiiderLibrary = colorLibraryFromAsset("spiiderLibrary.json");
        String jsonUserLibrary =sharedPreferences.getString("userLibrary", "empty");
        if (jsonUserLibrary.equals("empty")){
            userLibrary = new ArrayList<>();
        } else {
            userLibrary = colorLibraryFromJson(jsonUserLibrary);
        }
    }

    private void setupRecyclerView() {
        colorLibraryView = findViewById(R.id.colorLibraryView);
        colorLibraryView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        colorLibraryView.setLayoutManager(layoutManager);
        colorLibraryView.setItemAnimator(new DefaultItemAnimator());

        updateLibrary();

    }

    public void returnWithColorClicked(View view) {
        Gson gson = new Gson();
        // Animate Icon
        view.setBackground(animatedCapture);
        animatedCapture.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                Intent intent = new Intent();
                intent.putExtra("jsonColor", gson.toJson(returnColor));
                setResult(Activity.RESULT_OK, intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        animatedCapture.start();

    }

    public void changeLibraryClicked(View view) {
        if (userLibrary.isEmpty()){
            if (view.getId() == R.id.nextButton) {
                if (libraryActive == SPIIDER_LIBRARY) libraryActive = LEE_LIBRARY;
                else libraryActive += 1;
            } else {
                if (libraryActive == LEE_LIBRARY) libraryActive = SPIIDER_LIBRARY;
                else libraryActive -= 1;
            }
        }   else {
            if (view.getId() == R.id.nextButton) {
                if (libraryActive == USER_LIBRARY) libraryActive = LEE_LIBRARY;
                else libraryActive += 1;
            } else {
                if (libraryActive == LEE_LIBRARY) libraryActive = USER_LIBRARY;
                else libraryActive -= 1;
            }
        }
        updateLibrary();
    }

    private void updateLibrary() {
        switch (libraryActive) {
            case LEE_LIBRARY:
                adapter = new ColorLibraryAdapter(this, leeLibrary);
                colorLibraryView.setAdapter(adapter);
                libraryActive = LEE_LIBRARY;
                changeLibraryButton.setText("LEE Library");
                break;
            case ROSCO_LIBRARY:
                adapter = new ColorLibraryAdapter(this, roscoLibrary);
                colorLibraryView.setAdapter(adapter);
                libraryActive = ROSCO_LIBRARY;
                changeLibraryButton.setText("ROSCO Library");
                break;
            case SPIIDER_LIBRARY:
                adapter = new ColorLibraryAdapter(this, spiiderLibrary);
                colorLibraryView.setAdapter(adapter);
                libraryActive = SPIIDER_LIBRARY;
                changeLibraryButton.setText("Spiider Library");
                break;

            case USER_LIBRARY:
                adapter = new ColorLibraryAdapter(this, userLibrary);
                colorLibraryView.setAdapter(adapter);
                libraryActive = USER_LIBRARY;
                changeLibraryButton.setText("User Library");
                break;

        }
    }

    public void closeClicked(View view) {
        finish();
    }

    public void getItemOnClick(int position) {
        switch (libraryActive) {
            case LEE_LIBRARY:
                setReturnColor(leeLibrary.get(position));
                break;
            case ROSCO_LIBRARY:
                setReturnColor(roscoLibrary.get(position));
                break;
            case SPIIDER_LIBRARY:
                setReturnColor(spiiderLibrary.get(position));
                break;
            case USER_LIBRARY:
                setReturnColor(userLibrary.get(position));
                break;
        }
    }

    public void getItemOnLongClick(int position) {
        if (libraryActive == USER_LIBRARY){
            userLibrary.remove(position);
            adapter.notifyItemRemoved(position);
            if (userLibrary.isEmpty()){
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        libraryActive = LEE_LIBRARY;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateLibrary();
                            }
                        });

                    }
                }, 250);

            }
        }
    }

    private void setReturnColor(JsonColor color) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), returnColor.getColor(), color.getColor());
        colorAnimation.setDuration(1000); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                backgroundLayout.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });

        returnColor = color;

        returnWithColorButton.setBackground(animatedCapture);
        animatedCapture.start();

        colorAnimation.start();


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                bladesBackground.getDrawable().setTint(color.getColor());
            }
        }, 250);

    }

    private ArrayList<JsonColor> colorLibraryFromAsset(String fileName) {

        String json = null;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        Gson gson = new Gson();
        JsonColorWrapper colorWrapper = gson.fromJson(json, JsonColorWrapper.class);
        return colorWrapper.getColorLibrary();
    }

    private ArrayList<JsonColor> colorLibraryFromJson(String json) {

        Gson gson = new Gson();
        JsonColorWrapper colorWrapper = gson.fromJson(json, JsonColorWrapper.class);
        return colorWrapper.getColorLibrary();
    }

    static public String colorWrapperToJson(ArrayList<JsonColor> colors) {
        Gson gson = new Gson();
        JsonColorWrapper colorWrapper = new JsonColorWrapper();
        colorWrapper.setColorLibrary(colors);
        return gson.toJson(colorWrapper);
    }




    public void fullScreen() {
        // Makes the screen goes behind statusbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        // Flags for full screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String json;
        if (userLibrary.isEmpty()){
            json = "empty";
        } else {
            json = colorWrapperToJson(userLibrary);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userLibrary", json).apply();
    }
}