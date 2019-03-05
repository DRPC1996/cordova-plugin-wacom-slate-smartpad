package com.wacom.samples.cdlsampleapp;

import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.wacom.cdl.callbacks.FileTransferCallback;
import com.wacom.cdl.callbacks.OnCompleteCallback;
import com.wacom.cdl.exceptions.InvalidOperationException;
import com.wacom.cdl.InkDevice;
import com.wacom.cdl.deviceservices.DeviceServiceType;
import com.wacom.cdl.deviceservices.FileTransferDeviceService;
import com.wacom.cdlcore.InkDocument;
import com.wacom.ink.path.PathBuilder;
import com.wacom.inkcanvas.control.InkController;
import com.wacom.inkcanvas.control.InkPathBuilder;
import com.wacom.inkcanvas.views.InkView;

import java.util.ArrayList;

public class FileTransferActivity extends AppCompatActivity {
    //region Fields
    private InkDevice inkDevice;

    private InkView inkView;
    private InkController inkController;

    private ArrayList<String> filesTitles = new ArrayList<>();
    private ArrayAdapter<String> filesAdapter;
    private int filesCount = 0;

    private boolean continuousTransfer = true;
    private ArrayList<InkDocument> documents = new ArrayList<>();

    private ListView filesListView;

    private FileTransferDeviceService fileTransferDeviceService;
    //endregion Fields


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_transfer);

        final MyApplication app = (MyApplication) getApplication();
        app.subscribeForEvents(FileTransferActivity.this);
        inkDevice = app.getInkDevice();

        fileTransferDeviceService = (FileTransferDeviceService) inkDevice.getDeviceService(DeviceServiceType.FILE_TRANSFER_DEVICE_SERVICE);

        //region Setup InkCanvas
        inkView = (InkView) findViewById(R.id.mInkView);
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

        //region ListView Logic
        filesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                filesTitles);

        filesListView = (ListView) findViewById(R.id.filesList);
        filesListView.setAdapter(filesAdapter);

        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                inkController.getInkContext().setInkDocument(documents.get(position));
                inkView.invalidate();
            }
        });
        //endregion ListView Logic

        //region FileTransfer Logic
        try {
            fileTransferDeviceService.enable(continuousTransfer, fileTransferCallback, null);
        } catch (InvalidOperationException e) {
            e.printStackTrace();
        }

        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_file_transfer);
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

                        fileTransferDeviceService.setTransformationMatrix(matrix);
                    }
                });
        //region FileTransfer Logic
    }

    private FileTransferCallback fileTransferCallback = new FileTransferCallback() {
        @Override
        public void onFileTransferStarted(int filesCount) {

        }

        @Override
        public void onFileTransferComplete() {

        }

        @Override
        public void onFileTransferFailed() {

        }

        @Override
        public boolean onFileTransferred(InkDocument inkDocument, int fileIndex, int remainingFilesCount) {
            filesCount++;

            int layersCount = inkDocument.getRoot().size();
            int totalStrokes = inkDocument.getStrokesCount();

            String message = "File " + filesCount + " (" + totalStrokes + " strokes in " + layersCount + " layers)";

            filesTitles.add(message);
            filesAdapter.notifyDataSetChanged();

            documents.add(inkDocument);

            int position = documents.size() - 1;

            filesListView.requestFocusFromTouch();
            filesListView.setSelection(position);


            inkController.getInkContext().setInkDocument(inkDocument);
            inkView.invalidate();

            return true;
        }
    };


    @Override
    public void onBackPressed()
    {
        if(continuousTransfer){
            try {
                fileTransferDeviceService.disable(new OnCompleteCallback() {
                    @Override
                    public void onComplete() {
                        FileTransferActivity.this.finish();
                    }
                });
            } catch (InvalidOperationException e) {
                e.printStackTrace();
            }
        } else {
            FileTransferActivity.this.finish();
        }
        super.onBackPressed();
    }
}
