package com.getyourlocation.app.client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.getyourlocation.app.client.Constant;
import com.getyourlocation.app.client.R;

import java.io.IOException;

import sysu.mobile.limk.library.MapView;
import sysu.mobile.limk.library.OnRealLocationMoveListener;
import sysu.mobile.limk.library.Position;


public class IndoorMapActivity extends AppCompatActivity {
    private static final String TAG = "IndoorMapActivity";
    private TextView infoTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_map);
        infoTxt = (TextView) findViewById(R.id.indoorMap_infoTxt);
        MapView mapView = (MapView) findViewById(R.id.indoorMap_mapView);
        try {
            mapView.initNewMap(getAssets().open(Constant.FILENAME_MAP), 1, 0, new Position(652, 684));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapView.updateMyLocation(new Position(652, 684));
        mapView.setOnRealLocationMoveListener(new OnRealLocationMoveListener() {
            @Override
            public void onMove(Position position) {
                infoTxt.setText(position.toString());
            }
        });
    }
}
