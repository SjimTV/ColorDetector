package com.sjimtv.colordetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.google.gson.Gson;

public class DetectActivity extends AppCompatActivity {

    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private final int PERMISSION_REQUEST_CODE = 10;

    private boolean torchOn = false;
    private boolean sliderToggle = false;
    private int resultColor;

    private boolean colorOverViewVisible = true;

    private CameraManager cameraManager;

    ConstraintLayout resultColorView;
    ImageView resultBitmap;
    ImageView targetView;
    ConstraintLayout targetLayout;
    SeekBar zoomSlider;
    PreviewView viewFinder;
    Button torchButton;
    Button captureColorButton;

    ImageView colorOverView;

    ProgressBar redProgressBar;
    ProgressBar greenProgressBar;
    ProgressBar blueProgressBar;

    private Drawable targetDrawable;
    private Drawable sliderDrawable;
    private Drawable thumbDrawbable;

    private AnimatedVectorDrawable animatedCapture;
    private AnimatedVectorDrawable animatedSliderOn;
    private AnimatedVectorDrawable animatedSliderOff;
    private AnimatedVectorDrawable animatedBulbOn;
    private AnimatedVectorDrawable animatedBulbOff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        fullScreen();
        loadViews();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }

        loadDrawables();
        setupZoomSlider();

    }

    private void loadViews(){
        viewFinder = findViewById(R.id.viewFinder);
        resultColorView = findViewById(R.id.backgroundLayout);
        resultBitmap = findViewById(R.id.resultBitmap);
        targetView = findViewById(R.id.targetView);
        targetLayout = findViewById(R.id.targetLayout);
        captureColorButton = findViewById(R.id.captureColorButton);

        colorOverView = findViewById(R.id.colorOverView);
        colorOverView.setBackgroundColor(getIntent().getIntExtra("color", Color.rgb(0.8f, 0.8f, 0.8f)));
        colorOverView.setVisibility(View.VISIBLE);

        redProgressBar = findViewById(R.id.redProgressBar);
        greenProgressBar = findViewById(R.id.greenProgressBar);
        blueProgressBar = findViewById(R.id.blueProgressBar);
    }

    private void loadDrawables(){
        targetDrawable = getDrawable(R.drawable.ic_target_overlay);

        sliderDrawable = getDrawable(R.drawable.zoom_slider);
        thumbDrawbable = getDrawable(R.drawable.zoom_thumb);

        animatedCapture = (AnimatedVectorDrawable) getDrawable(R.drawable.animated_eye_closing);
        animatedSliderOn = (AnimatedVectorDrawable) getDrawable(R.drawable.animated_slider_toggle_on);
        animatedSliderOff = (AnimatedVectorDrawable) getDrawable(R.drawable.animated_slider_toggle_off);
        animatedBulbOff = (AnimatedVectorDrawable) getDrawable(R.drawable.animated_bulb_off);
        animatedBulbOn = (AnimatedVectorDrawable) getDrawable(R.drawable.animated_bulb_on);

        torchButton = findViewById(R.id.torchButton);
        torchButton.setBackground(animatedBulbOff);
        animatedBulbOff.start();
    }

    private void setupZoomSlider(){
        zoomSlider = findViewById(R.id.zoomSlider);
        zoomSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 100) {
                    seekBar.setProgress(100);
                    return;
                }
                if (progress <= 0){
                    seekBar.setProgress(0);
                    return;
                }

                float zoom = (float) progress / 100;
                cameraManager.getCamera().getCameraControl().setLinearZoom(zoom);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void showColorOverView(){
        colorOverView.setVisibility(View.VISIBLE);
        colorOverView.setBackgroundColor(resultColor);
        AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(500);
        colorOverView.setAnimation(inAnimation);
        colorOverViewVisible = true;

    }

    private void hideColorOverView(){
        AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(1000);
        colorOverView.setAnimation(outAnimation);
        colorOverView.setVisibility(View.GONE);
        colorOverViewVisible = false;
    }

    public void captureClicked(View view) {
        cameraManager.stopAnalyzing();
        // Animate Icon
        view.setBackground(animatedCapture);
        animatedCapture.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                Intent intent = new Intent();
                intent.putExtra("resultColor", resultColor);
                setResult(Activity.RESULT_OK, intent);
                DetectActivity.this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        animatedCapture.start();

    }

    public void closeClicked(View view) {
        finish();
    }

    public void torchClicked(View view){
        if (torchOn){
            view.setBackground(animatedBulbOff);
            animatedBulbOff.start();
            cameraManager.getCamera().getCameraControl().enableTorch(false);
            torchOn = false;
        } else {
            view.setBackground(animatedBulbOn);
            animatedBulbOn.start();
            cameraManager.getCamera().getCameraControl().enableTorch(true);
            torchOn = true;
        }

    }

    public void sliderToggleClicked(View view) {
        ConstraintLayout sliderLayout = findViewById(R.id.sliderLayout);

        if (sliderToggle){
            view.setBackground(animatedSliderOff);
            animatedSliderOff.start();

            AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(1000);
            sliderLayout.setAnimation(outAnimation);
            sliderLayout.setVisibility(View.INVISIBLE);
            sliderToggle = false;
        } else {
            view.setBackground(animatedSliderOn);
            animatedSliderOn.start();

            AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(1000);
            sliderLayout.setAnimation(inAnimation);
            sliderLayout.setVisibility(View.VISIBLE);
            sliderToggle = true;
        }

    }

    private void startCamera() {
        cameraManager = new CameraManager(this, CameraSelector.LENS_FACING_BACK);
        cameraManager.setPreviewView(viewFinder);
        cameraManager.setColorAnalyzer(new ColorAnalyzer(this));
        cameraManager.initialize();

    }

    public void updateResultColor(int resultColor, int inverseResultColor){
        setOverlayAndBackgroundColor(resultColor);
        setUIColor(inverseResultColor);
        setResultProgress(resultColor);
    }

    public void setResultColor(int resultColor, int inverseResultColor, Bitmap bitmap){
        this.resultColor = resultColor;
        updateResultColor(resultColor, inverseResultColor);
        resultBitmap.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        viewFinder.setVisibility(View.INVISIBLE);
        showColorOverView();
    }


    private void setOverlayAndBackgroundColor(int resultColor){
        // set result color background
        resultColorView.setBackgroundColor(resultColor);

        // set camera overlay color
        targetDrawable.setTint(resultColor);
        targetView.setImageDrawable(targetDrawable);
    }

    private void setUIColor(int inverseResultColor) {
        // set slider inverse color
        sliderDrawable.setTint(inverseResultColor);
        thumbDrawbable.setTint(inverseResultColor);
        zoomSlider.setProgressDrawable(sliderDrawable);
        zoomSlider.setThumb(thumbDrawbable);

    }

    private void setResultProgress(int resultColor){
        if (colorOverViewVisible){
            hideColorOverView();
            targetLayout.setVisibility(View.VISIBLE);
            viewFinder.setVisibility(View.VISIBLE);
        }

        Color color = Color.valueOf(resultColor);

        int RED = (int) (color.red() * 255) - 1;
        int GREEN = (int) (color.green() * 255) - 1;
        int BLUE = (int) (color.blue() * 255) - 1;

        redProgressBar.setProgress(RED);
        greenProgressBar.setProgress(GREEN);
        blueProgressBar.setProgress(BLUE);
    }



    private boolean allPermissionsGranted(){
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            if (allPermissionsGranted()){
                startCamera();
            }
        }
    }

    public void fullScreen(){
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