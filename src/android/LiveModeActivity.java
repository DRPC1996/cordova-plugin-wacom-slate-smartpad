package com.wacom.samples.cdlsampleapp;

import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;
import com.wacom.cdl.callbacks.LiveModeCallback;
import com.wacom.cdl.callbacks.OnCompleteCallback;
import com.wacom.cdl.exceptions.InvalidOperationException;
import com.wacom.cdl.InkDevice;
import com.wacom.cdl.deviceservices.DeviceServiceType;
import com.wacom.cdl.deviceservices.LiveModeDeviceService;
import com.wacom.cdlcore.InkStroke;
import com.wacom.ink.path.PathBuilder;
import com.wacom.ink.path.PathChunk;
import com.wacom.ink.path.PathUtils;
import com.wacom.inkcanvas.control.InkController;
import com.wacom.inkcanvas.control.InkPathBuilder;
import com.wacom.inkcanvas.views.InkView;

import java.util.Timer;
import java.util.TimerTask;

public class LiveModeActivity extends AppCompatActivity {
    //region Fields
    private InkDevice inkDevice;

    private Timer timer = new Timer();
    private TimerTask hideHoverPointTask;
    private InkView inkView;
    private ImageView hoverPoint;
    private InkController inkController;
    private LiveModeDeviceService liveModeDeviceService;
    //endregion Fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time);

        final MyApplication app = (MyApplication) getApplication();
        app.subscribeForEvents(LiveModeActivity.this);
        inkDevice = app.getInkDevice();

        liveModeDeviceService = (LiveModeDeviceService) inkDevice.getDeviceService(DeviceServiceType.LIVE_MODE_DEVICE_SERVICE);

        //region Setup InkCanvas
        inkView = (InkView) findViewById(R.id.mInkView);
        hoverPoint = (ImageView) findViewById(R.id.hoverPoint);

        inkController = new InkController();
        inkController.bind(inkView);
        inkController.initialize(new InkView.OnViewLifecycleListener() {
            @Override
            public void onInitialized(InkView view) {
                inkView.setBackgroundColorX(Color.LTGRAY);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inkView.invalidate();
                    }
                });
            }
        });

        InkPathBuilder inkPathBuilder = inkController.getInkPathBilder();
        inkPathBuilder.setActivePathBuilder(InkPathBuilder.PathBuilderType.SpeedPathBuilder);
        inkPathBuilder.setPathPropertyConfig(InkPathBuilder.PathBuilderType.SpeedPathBuilder, PathBuilder.PropertyName.Width, 1f, 2f, Float.NaN, Float.NaN, PathBuilder.PropertyFunction.Power, 1.0f, false);
        inkPathBuilder.enableSmoothener(true);
        //endregion Setup InkCanvas

        //region LiveMode Logic
        try {
            liveModeDeviceService.enable(liveModeCallback, null);
        } catch (InvalidOperationException e) {
            e.printStackTrace();
        }

        final View layout = findViewById(R.id.activity_real_time);
        final ViewTreeObserver observer= layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        float wScale = inkView.getWidth() / (float) app.noteWidth;
                        float hScale = inkView.getHeight() / (float) app.noteHeight;

                        float sf = wScale < hScale ? wScale : hScale;

                        Matrix matrix = new Matrix();
                        matrix.postScale(sf, sf);

                        liveModeDeviceService.setTransformationMatrix(matrix);
                    }
                });
        //endregion LiveMode Logic
    }



    private void resetHoverPointTimer(){
        if (hideHoverPointTask != null){
            hideHoverPointTask.cancel();
        }

        hideHoverPointTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hoverPoint.setVisibility(View.GONE);
                    }
                });
            }
        };

        timer.schedule(hideHoverPointTask, 50);
    }

    private LiveModeCallback liveModeCallback =  new LiveModeCallback() {
        @Override
        public void onStrokeStart(PathChunk pathChunk) {
            hoverPoint.setVisibility(View.GONE);
            inkController.drawPathPart(PathUtils.Phase.BEGIN, pathChunk);
        }

        @Override
        public void onStrokeMove(PathChunk pathChunk) {
            inkController.drawPathPart(PathUtils.Phase.MOVE, pathChunk);
        }

        @Override
        public void onStrokeEnd(PathChunk pathChunk, InkStroke inkStroke) {
            inkController.drawPathPart(PathUtils.Phase.END, pathChunk);
        }

        @Override
        public void onHover(int x, int y) {
            hoverPoint.setVisibility(View.VISIBLE);
            hoverPoint.setX(x + hoverPoint.getWidth() / 2);
            hoverPoint.setY(y + hoverPoint.getHeight() / 2);
            resetHoverPointTimer();
        }

        @Override
        public void onNewLayerCreated() {
            Toast.makeText(LiveModeActivity.this, "New layer created", Toast.LENGTH_SHORT).show();
        }
    };



    @Override
    public void onBackPressed()
    {
        try {
            liveModeDeviceService.disable(new OnCompleteCallback() {
                @Override
                public void onComplete() {
                    LiveModeActivity.this.finish();
                }
            });
        } catch (InvalidOperationException e) {
            e.printStackTrace();
        }
    }
}
