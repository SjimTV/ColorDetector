package com.sjimtv.colordetector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.illposed.osc.OSCPortOut;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final int DETECT_ACTIVITY = 3;
    private final int COLORLIBRARY_ACTIVITY = 1;
    private final int SCHEME_ACTIVITY = 2;

    private final int TYPE_RGB = 0;
    private final int TYPE_RGBW = 1;
    private final int TYPE_HTML = 2;
    private final String[] TYPES = {"RGB", "RGBW", "HTML"};
    private final String[] COLORS = {"R = ", "G = ", "B = ", "W = "};

    private MyColor resultColor;

    private int typeState = TYPE_RGB;


    SharedPreferences sharedPreferences;

    ConstraintLayout backgroundLayout;

    ImageView menuBackground;
    ConstraintLayout menuLayout;

    TextView resultTextView;
    ConstraintLayout resultLayout;
    private boolean resultVisible = true;

    private AnimatedVectorDrawable animatedCapture;

    private OSCPortOut oscSender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("com.sjimtv.colordetector", 0);

        fullScreen();
        loadViews();
        loadDrawables();

        setUpActivity(getResultColor(getIntent()));

        try {
            oscSender = setupOscSender();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private MyColor getResultColor(Intent intent) {
        // Make the baseColor from intent, if there is a JSONCOLOR convert, otherwise take from int value.
        // If there is not a int value (startup) generate random color
        String jsonString = intent.getStringExtra("jsonColor");
        if (jsonString == null) {
            return new MyColor(intent.getIntExtra("resultColor", randomColor()));
        } else {
            Gson gson = new Gson();
            return new MyColor(gson.fromJson(jsonString, JsonColor.class));
        }
    }

    private void setUpActivity(MyColor resultColor) {
        this.resultColor = resultColor;
        backgroundLayout.setBackgroundColor(resultColor.getColor());

        setTypeResults();
        animOutResult(0);
    }

    private void storeColorUserLibrary(JsonColor jsonColor) {
        ArrayList<JsonColor> userLibrary;
        String jsonUserLibrary = sharedPreferences.getString("userLibrary", "empty");
        if (jsonUserLibrary.equals("empty")) {
            userLibrary = new ArrayList<>();
        } else {
            userLibrary = colorLibraryFromJson(jsonUserLibrary);
        }
        userLibrary.add(jsonColor);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userLibrary", colorWrapperToJson(userLibrary)).apply();
    }

    private void loadViews() {
        backgroundLayout = findViewById(R.id.backgroundLayout);

        menuLayout = findViewById(R.id.menuLayout);
        menuBackground = findViewById(R.id.backgroundMenu);

        resultLayout = findViewById(R.id.resultLayout);
        resultTextView = findViewById(R.id.resultTextView);
    }

    private void loadDrawables() {
        animatedCapture = (AnimatedVectorDrawable) getDrawable(R.drawable.animated_eye_closing);
    }

    private OSCPortOut setupOscSender() throws InterruptedException {
        OSCSendThread oscSendThread = new OSCSendThread(8000);
        oscSendThread.start();
        oscSendThread.join();
        return oscSendThread.getSender();
    }


    public void detectColorClicked(View view) {
        // Animate Icon
        view.setBackground(animatedCapture);
        animatedCapture.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, DetectActivity.class);
                intent.putExtra("color", resultColor.getColor());
                startActivityForResult(intent, DETECT_ACTIVITY);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 500);


    }

    public void popupMenuClicked(View view){
        showPopupMenu();
    }

    public void resultTypeClicked(View view) {
        hidePopupMenu();
        setTypeResults();
        animInResult(1000);
    }

    public void changeResultTypeClicked(View view) {
        TextView resultTypeMenuButton = findViewById(R.id.resultTypeMenuButton);
        resultTypeMenuButton.setText(changeResultType(view.getTag().toString()));
    }

    public void libraryClicked(View view) {
        hidePopupMenu();

        Intent intent = new Intent(MainActivity.this, ColorLibraryActivity.class);
        intent.putExtra("color", resultColor.getColor());
        startActivityForResult(intent, COLORLIBRARY_ACTIVITY);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void schemeClicked(View view) {
        hidePopupMenu();

        Intent intent = new Intent(MainActivity.this, SchemeActivity.class);
        intent.putExtra("color", resultColor.getColor());
        startActivityForResult(intent, SCHEME_ACTIVITY);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void sendColorOscClicked(View view) {
        hidePopupMenu();

        if (typeState == TYPE_RGB) new SendColorOSCTask(oscSender, true).execute(resultColor.getRgbValues());
        else if (typeState == TYPE_RGBW) new SendColorOSCTask(oscSender, true).execute(resultColor.getRgbwValues());
        else if (typeState == TYPE_HTML) new SendHTMLOSCTask(oscSender).execute(resultColor.getHtmlValue());
    }

    public void saveClicked(View view) {
        saveColor();
    }

    public void closeClicked(View view) {
        finish();
    }

    public void backgroundClicked(View view) {
        if (resultVisible) {
            animOutResult(1000);
        } else {
            animInResult(1000);
        }
    }

    public void tapOutMenuClicked(View view) {
        if (menuBackground.getAnimation() == null || menuBackground.getAnimation().hasEnded()){
            setTypeResults();
            hidePopupMenu();
        }
    }

    public void emptyClicked(View view){
        // DO NOTHING
    }


    private void animInResult(int duration) {
        if (resultVisible) return;

        // while in onCreate, height = 0...
        int topEscape = -resultLayout.getHeight();
        if (topEscape == 0) topEscape = -600;

        // Value toolbar
        Animation resultAnimation = new TranslateAnimation(0, 0, topEscape, 0);
        resultAnimation.setFillAfter(true);
        resultAnimation.setDuration(duration);
        resultAnimation.setInterpolator(new BounceInterpolator());
        resultLayout.startAnimation(resultAnimation);

        resultVisible = true;
    }

    private void animOutResult(int duration) {
        if (!resultVisible) return;

        // Value toolbar
        Animation resultSlide = new TranslateAnimation(0, 0, 0, -900);
        resultSlide.setFillAfter(true);
        resultSlide.setDuration(duration);
        resultLayout.startAnimation(resultSlide);

        resultVisible = false;
    }

    private void animInMenuItems(ConstraintLayout layout,int fromY, int duration){
        AnimatorSet animatorSet = new AnimatorSet();
        Interpolator interpolator = new OvershootInterpolator();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(layout, "scaleY", 0f, 1f);
        scaleY.setDuration(duration);
        scaleY.setInterpolator(interpolator);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(layout, "scaleX", 0f, 1f);
        scaleX.setDuration(duration);
        scaleX.setInterpolator(interpolator);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(layout, "translationY", fromY, 1f);
        translationY.setDuration(duration);
        translationY.setInterpolator(interpolator);

        ObjectAnimator translationX = ObjectAnimator.ofFloat(layout, "translationX", -500f, 1f);
        translationX.setDuration(duration);
        translationX.setInterpolator(interpolator);



        animatorSet.playTogether(scaleY, scaleX, translationY, translationX);
        animatorSet.start();


    }

    private void animOutMenuItems(ConstraintLayout layout,int toY, int duration){
        AnimatorSet animatorSet = new AnimatorSet();
        Interpolator interpolator = new AnticipateInterpolator();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(layout, "scaleY", 1f, 0f);
        scaleY.setDuration(duration);
        scaleY.setInterpolator(interpolator);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(layout, "scaleX", 1f, 0f);
        scaleX.setDuration(duration);
        scaleX.setInterpolator(interpolator);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(layout, "translationY", 1f, toY);
        translationY.setDuration(duration);
        translationY.setInterpolator(interpolator);

        ObjectAnimator translationX = ObjectAnimator.ofFloat(layout, "translationX", 1f, -500f);
        translationX.setDuration(duration);
        translationX.setInterpolator(interpolator);



        animatorSet.playTogether(scaleY, scaleX, translationY, translationX);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                layout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

    }

    private String changeResultType(String direction) {
        switch (direction) {
            case "next":
                if (typeState == TYPE_HTML) typeState = TYPE_RGB;
                else typeState += 1;
                return TYPES[typeState];

            case "prev":
                if (typeState == TYPE_RGB) typeState = TYPE_HTML;
                else typeState -= 1;
                return TYPES[typeState];
        }
        return "ERROR";
    }

    private void setTypeResults() {
        // Check what type is currently active

        String result = "";

        switch (typeState) {

            case TYPE_RGB:
                result = setColorResultText(resultColor.getRgbValues());
                break;

            case TYPE_RGBW:
                result = setColorResultText(resultColor.getRgbwValues());
                break;

            case TYPE_HTML:
                result = "HTML = " + resultColor.getHtmlValue();
                break;
        }

        resultTextView.setText(result);
    }


    private String setColorResultText(int[] colors) {
        String result = "";
        int i = 0;
        for (int value : colors){
            if (i > 0) result += "\n";
            result += COLORS[i] + value;
            i++;
        }
        return result;
    }

    private void showPopupMenu() {
        int duration = 400;
        int fromY = 1200;

        for (int i = 0; i < menuLayout.getChildCount(); i++){
            View child = menuLayout.getChildAt(i);
            if (child instanceof ConstraintLayout){
                child.setVisibility(View.VISIBLE);
                animInMenuItems((ConstraintLayout) child,fromY, duration);
                fromY += 100;
                duration += 150;
            }
        }

        AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(1000);
        menuBackground.setAnimation(inAnimation);
        menuLayout.setVisibility(View.VISIBLE);
        animOutResult(1000);
    }

    private void hidePopupMenu() {
        int duration = 600;
        int toY = 1400;

        for (int i = 0; i < menuLayout.getChildCount(); i++){
            View child = menuLayout.getChildAt(i);
            Log.i("ID", child.toString());
            if (child instanceof ConstraintLayout){
                animOutMenuItems((ConstraintLayout) child,toY, duration);
                toY -= 100;
                duration -= 150;
            }
        }

        AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(1000);
        outAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                menuLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        menuBackground.setAnimation(outAnimation);

    }

    private int randomColor() {
        Random random = new Random();
        return Color.rgb(
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat());
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

    private void saveColor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View saveColorDialogView = getLayoutInflater().inflate(R.layout.dialog_save_color, null);
        final EditText colorNameEditText = saveColorDialogView.findViewById(R.id.colorNameEditText);
        Button saveColorButton = saveColorDialogView.findViewById(R.id.saveColorButton);
        builder.setView(saveColorDialogView);

        final AlertDialog saveColorDialog = builder.create();
        saveColorDialog.show();

        saveColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(colorNameEditText.getText())) {
                    colorNameEditText.setHint("Enter a name please.");
                } else {
                    storeColorUserLibrary(new JsonColor(colorNameEditText.getText().toString(), resultColor.getColor()));
                    saveColorDialog.dismiss();
                    fullScreen();
                }
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCHEME_ACTIVITY || requestCode == COLORLIBRARY_ACTIVITY || requestCode == DETECT_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                setUpActivity(getResultColor(data));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullScreen();
    }
}