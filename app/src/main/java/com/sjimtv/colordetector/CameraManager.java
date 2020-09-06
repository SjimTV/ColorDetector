package com.sjimtv.colordetector;

import android.content.Context;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraManager {

    private boolean initializeAnalyzer = false;
    private boolean initializePreview = false;

    private Context context;
    private int lens;

    private PreviewView previewView;
    private ColorAnalyzer colorAnalyzer;

    private Camera camera;

    public CameraManager(Context context, int lens){
        this.context = context;
        this.lens = lens;
    }

    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
        initializePreview = true;
    }

    public void setColorAnalyzer(ColorAnalyzer colorAnalyzer) {
        this.colorAnalyzer = colorAnalyzer;
        initializeAnalyzer = true;
    }

    public Camera getCamera() {
        return camera;
    }

    public void initialize(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = null;
                    ImageAnalysis imageAnalyzer = null;

                    if (initializePreview){
                        preview = new Preview.Builder().build();
                    }

                    if (initializeAnalyzer){
                        imageAnalyzer = new ImageAnalysis.Builder().build();
                        imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(context), colorAnalyzer);
                    }

                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lens).build();

                    try {
                        cameraProvider.unbindAll();
                        if (initializePreview && initializeAnalyzer) {
                            camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageAnalyzer);
                        } else if (initializePreview)  {
                            camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview);
                        } else if (initializeAnalyzer) {
                            camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, imageAnalyzer);
                        }

                        if (initializePreview) {
                            assert preview != null;
                            preview.setSurfaceProvider(previewView.createSurfaceProvider());
                        }

                    } catch ( Exception e) {
                        e.printStackTrace();
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void stopAnalyzing(){
        colorAnalyzer.stop();
    }
}
