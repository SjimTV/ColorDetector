package com.sjimtv.colordetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;

public class SchemeActivity extends AppCompatActivity {

    private final int RANGE_FULL = 0;
    private final int RANGE_COMPLEMENTARY = 1;
    private final int RANGE_TRIADIC = 2;
    private final int RANGE_MONOCHROMATIC = 3;
    private final int RANGE_ANALOGOUS = 5;
    private final int RANGE_SPLIT_COMPLEMENTARY = 6;


    private MyColor baseColor;
    private int returnColor;

    private int heightScreen;
    private int rangeTypeActive;

    ImageView baseColorView;
    ImageView secondColorView;
    ImageView thirdColorView;
    ImageView bladesBackground;
    ConstraintLayout backgroundLayout;
    TextView rangeTypeButton;
    Button detectColorButton;

    private AnimatedVectorDrawable animatedCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme);

        baseColor = new MyColor(getIntent().getIntExtra("color", Color.rgb(0.5f, 0.5f, 0.5f)));
        returnColor = baseColor.getColor();

        loadViews();
        loadDrawables();
        fullScreen();
        heightScreen = getHeightScreen() + (getHeightScreen() / 10);
        complementaryRangeColor();

    }

    private void loadViews() {
        backgroundLayout = findViewById(R.id.backgroundLayout);
        backgroundLayout.setBackgroundColor(Color.rgb(1f, 1f, 1f));
        detectColorButton = findViewById(R.id.detectColorButton);

        baseColorView = findViewById(R.id.baseColorView);
        secondColorView = findViewById(R.id.secondColorView);
        thirdColorView = findViewById(R.id.thirdColorView);

        bladesBackground = findViewById(R.id.bladesBackground);
        bladesBackground.getDrawable().setTint(returnColor);

        rangeTypeButton = findViewById(R.id.rangeTypeButton);
    }

    private void loadDrawables() {
        animatedCapture = (AnimatedVectorDrawable) getDrawable(R.drawable.animated_eye_closing);
    }

    public void backgroundClicked(View view) {
        setReturnColor(view.getLabelFor());
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
                intent.putExtra("jsonColor", gson.toJson(new JsonColor("unnamed", returnColor)));
                setResult(Activity.RESULT_OK, intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        animatedCapture.start();

    }

    public void rangeClicked(View view) {
        if (view.getId() == R.id.nextButton) {
            if (rangeTypeActive == RANGE_SPLIT_COMPLEMENTARY) rangeTypeActive = RANGE_FULL;
            else rangeTypeActive += 1;
        } else {
            if (rangeTypeActive == RANGE_FULL) rangeTypeActive = RANGE_SPLIT_COMPLEMENTARY;
            else rangeTypeActive -= 1;
        }
        updateRange();

    }

    private void updateRange() {
        switch (rangeTypeActive) {

            case RANGE_FULL:
                fullRangeColor();
                break;
            case RANGE_COMPLEMENTARY:
                complementaryRangeColor();
                break;
            case RANGE_TRIADIC:
                triadicRangeColor();
                break;
            case RANGE_MONOCHROMATIC:
                monochromaticRangeColor();
                break;
            case RANGE_ANALOGOUS:
                analogousRangeColor();
                break;
            case RANGE_SPLIT_COMPLEMENTARY:
                splitCompRangeColor();
                break;
        }
    }

    public void closeClicked(View view) {
        finish();
    }

    private void fullRangeColor() {
        setColorView(baseColorView, baseColor.getColor());

        scaleView(baseColorView, heightScreen);
        scaleView(secondColorView, 0);
        scaleView(thirdColorView, 0);

        rangeTypeButton.setText("Full");
        rangeTypeActive = RANGE_FULL;
    }

    private void complementaryRangeColor() {
        setColorView(baseColorView, baseColor.getColor());
        setColorView(secondColorView, baseColor.getComplementaryColor());

        scaleView(baseColorView, heightScreen / 2);
        scaleView(secondColorView, heightScreen / 2);
        scaleView(thirdColorView, 0);

        rangeTypeButton.setText("Complementary");
        rangeTypeActive = RANGE_COMPLEMENTARY;
    }

    private void triadicRangeColor() {
        setColorView(baseColorView, baseColor.getColor());
        setColorView(secondColorView, baseColor.getTriadicColor()[0]);
        setColorView(thirdColorView, baseColor.getTriadicColor()[1]);

        scaleView(baseColorView, heightScreen / 3);
        scaleView(secondColorView, heightScreen / 3);
        scaleView(thirdColorView, heightScreen / 3);

        rangeTypeButton.setText("Triadic");
        rangeTypeActive = RANGE_TRIADIC;
    }

    private void monochromaticRangeColor() {
        int[] monochromaticColors = baseColor.getMonochromaticColor();

        setColorView(baseColorView, monochromaticColors[0]);
        setColorView(secondColorView, monochromaticColors[1]);
        setColorView(thirdColorView, monochromaticColors[2]);

        scaleView(baseColorView, heightScreen / 3);
        scaleView(secondColorView, heightScreen / 3);
        scaleView(thirdColorView, heightScreen / 3);

        rangeTypeButton.setText("Monochromatic");
        rangeTypeActive = RANGE_MONOCHROMATIC;
    }

    private void analogousRangeColor() {
        int[] analogousColors = baseColor.getAnalogousColor();

        setColorView(baseColorView, analogousColors[0]);
        setColorView(secondColorView, analogousColors[1]);
        setColorView(thirdColorView, analogousColors[2]);

        scaleView(baseColorView, heightScreen / 3);
        scaleView(secondColorView, heightScreen / 3);
        scaleView(thirdColorView, heightScreen / 3);

        rangeTypeButton.setText("Analogous");
        rangeTypeActive = RANGE_ANALOGOUS;
    }

    private void splitCompRangeColor() {
        int[] splitCompColors = baseColor.getSplitComplementary();

        setColorView(baseColorView, splitCompColors[0]);
        setColorView(secondColorView, splitCompColors[1]);
        setColorView(thirdColorView, splitCompColors[2]);

        scaleView(baseColorView, heightScreen / 3);
        scaleView(secondColorView, heightScreen / 3);
        scaleView(thirdColorView, heightScreen / 3);

        rangeTypeButton.setText("Split Complementary");
        rangeTypeActive = RANGE_SPLIT_COMPLEMENTARY;
    }

    private void scaleView(View view, int height) {
        view.getLayoutParams().height = height;
        view.requestLayout();
        Animation scaleAnimation = new ScaleAnimation(1f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setInterpolator(new BounceInterpolator());
        view.startAnimation(scaleAnimation);
    }

    private int getHeightScreen() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private void setColorView(ImageView view, int color) {
        view.setBackgroundColor(color);
        view.setLabelFor(color);
    }

    private void setReturnColor(int color) {
        returnColor = color;

        detectColorButton.setBackground(animatedCapture);
        animatedCapture.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                bladesBackground.getDrawable().setTint(color);
            }
        }, 250);

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
}