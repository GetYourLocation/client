package com.getyourlocation.app.client.activity;

import android.content.Intent;
import android.graphics.BitmapRegionDecoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getyourlocation.app.client.Constant;
import com.getyourlocation.app.client.R;

import java.io.IOException;

import sysu.mobile.limk.library.MapView;
import sysu.mobile.limk.library.OnRealLocationMoveListener;
import sysu.mobile.limk.library.Position;


public class LocalizationActivity extends AppCompatActivity {
    private static final String TAG = "IndoorMapActivity";
    private TextView infoTxt;
    private final static int REQUEST_CODE=1;
    private MapView mapView;
    private float mapHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_map);
        infoTxt = (TextView) findViewById(R.id.indoorMap_infoTxt);
        mapView = (MapView) findViewById(R.id.indoorMap_mapView);
        showLocation(652, 684);
        Button locationBtn = (Button) findViewById(R.id.indoorMap_location_btn);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "test...");
                Intent intent=new Intent();
                intent.setClass(LocalizationActivity.this, PhotoActivity.class);
                //intent.putExtra("str", "Intent Demo");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode==REQUEST_CODE)
        {
            if (resultCode==testActivity.RESULT_CODE)
            {
                Bundle bundle=data.getExtras();
                float x = bundle.getFloat("x");
                float y = bundle.getFloat("y");
                showLocation(x, mapHeight - y);
            }
        }
    }

    private void showLocation(float x, float y)
    {
        try {
            mapView.initNewMap(getAssets().open(Constant.FILENAME_MAP), 1, 0, new Position(x, y));
            BitmapRegionDecoder mMapDecoder = BitmapRegionDecoder.newInstance(
                    getAssets().open(Constant.FILENAME_MAP), false);
            mapHeight = mMapDecoder.getHeight();
            System.out.println("mapHeight is:"+mMapDecoder.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapView.updateMyLocation(new Position(x, y));
        mapView.setOnRealLocationMoveListener(new OnRealLocationMoveListener() {
            @Override
            public void onMove(Position position) {
                infoTxt.setText(position.toString());
            }
        });
    }
}