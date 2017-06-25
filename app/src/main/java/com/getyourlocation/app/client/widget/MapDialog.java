package com.getyourlocation.app.client.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.getyourlocation.app.client.Constant;
import com.getyourlocation.app.client.R;

import java.io.File;
import java.io.FileWriter;

import sysu.mobile.limk.library.MapView;
import sysu.mobile.limk.library.OnRealLocationMoveListener;
import sysu.mobile.limk.library.Position;


/**
 * Dialog that shows a map view.
 */
public class MapDialog extends Dialog {
    private static final String TAG = "MapDialog";
    private static final String POS_FILENAME = "pos.csv";

    private File framesDir;
    private Context context;
    private DialogInterface.OnCancelListener cancelListener;

    private TextView infoTxt;

    private Position curPos;

    /**
     * Initialize a dialog to show help information.
     *
     * @param context The context
     * @param cancelListener Called when the dialog is closed
     */
    public MapDialog(Context context, @Nullable DialogInterface.OnCancelListener cancelListener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;
        this.cancelListener = cancelListener;
        setOnCancelListener(this.cancelListener);
        init();
    }

    public void setFramesDir(File framesDir) {
        this.framesDir = framesDir;
    }

    private void init() {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_map, null);
        initInfoTxt(v);
        initMap(v);
        initSaveBtn(v);
        Window window = getWindow();
        if (window != null) {
            window.setContentView(v);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private void initInfoTxt(View v) {
        infoTxt = (TextView) v.findViewById(R.id.dialog_infoTxt);
    }

    private void initMap(View v) {
        curPos = new Position(0, 0);
        MapView mapView = (MapView) v.findViewById(R.id.dialog_mapView);
        try {
            mapView.initNewMap(context.getAssets().open(Constant.FILENAME_MAP), 1, 0, new Position(652, 684));
            mapView.updateMyLocation(new Position(652, 684));
            mapView.setOnRealLocationMoveListener(new OnRealLocationMoveListener() {
                @Override
                public void onMove(Position position) {
                    curPos = position;
                    infoTxt.setText(position.toString());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private void initSaveBtn(View v) {
        Button saveBtn = (Button) v.findViewById(R.id.dialog_saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePosToFile();
                MapDialog.this.dismiss();
                if (cancelListener != null) {
                    cancelListener.onCancel(MapDialog.this);
                }
            }
        });
    }

    private void savePosToFile() {
        String filename = framesDir.getParent() + File.separator + POS_FILENAME;
        File posFile = new File(filename);
        try {
            FileWriter fos = new FileWriter(posFile);
            fos.write(curPos.getX() + "," + curPos.getY());
            fos.close();
            Log.d(TAG, "Position (" + curPos.toString() + ") saved.");
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }
}
