package com.getyourlocation.app.gyl_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getyourlocation.app.gyl_client.Constant;
import com.getyourlocation.app.gyl_client.util.CommonUtil;
import com.getyourlocation.app.gyl_client.widget.Mark;
import com.getyourlocation.app.gyl_client.R;
import com.palmaplus.nagrand.core.Types;
import com.palmaplus.nagrand.data.DataList;
import com.palmaplus.nagrand.data.DataSource;
import com.palmaplus.nagrand.data.LocationList;
import com.palmaplus.nagrand.data.LocationModel;
import com.palmaplus.nagrand.data.MapModel;
import com.palmaplus.nagrand.data.PlanarGraph;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.gestures.OnSingleTapListener;

import java.util.ArrayList;
import java.util.Locale;


public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    private DataSource dataSource;
    private LocationList floorList;

    private MapView mapView;
    private TextView floorTxt;

    private ArrayList<Mark> marks = new ArrayList<>();
    private int curFloorIdx = 0;
    private long curFloorID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initFloorCtrl();
        initMap();
        initClearBtn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.drop();
    }

    private void initFloorCtrl() {
        floorTxt = (TextView) findViewById(R.id.map_floorTxt);
        Button upBtn = (Button) findViewById(R.id.map_upBtn);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllMark();
                if (floorList != null && curFloorIdx < floorList.getSize() - 1) {
                    curFloorIdx++;
                    Log.d(TAG, "Floor: " + String.valueOf(curFloorIdx));
                    showFloor(LocationModel.id.get(floorList.getPOI(curFloorIdx)));
                }
                floorTxt.setText(String.format(Locale.CHINA, "Floor: %d", curFloorIdx + 1));
            }
        });
        Button lowBtn = (Button) findViewById(R.id.map_lowBtn);
        lowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllMark();
                if (floorList != null && curFloorIdx > 0) {
                    curFloorIdx--;
                    Log.d(TAG, "Floor: " + String.valueOf(curFloorIdx));
                    showFloor(LocationModel.id.get(floorList.getPOI(curFloorIdx)));
                }
                floorTxt.setText(String.format(Locale.CHINA, "Floor: %d", curFloorIdx + 1));
            }
        });
    }

    private void initMap() {
        dataSource = new DataSource(Constant.URL_MAP_SERVER);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.map_viewContainer);
        mapView = (MapView) findViewById(R.id.map_mapView);
        mapView.setOverlayContainer(container);
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(MapView mapView, float x, float y) {
                CommonUtil.showToast(MapActivity.this, "X: " + x + " Y: " + y);
                addMark(mapView, x, y);
            }
        });
        // 请求可用地图
        dataSource.requestMaps(new DataSource.OnRequestDataEventListener<DataList<MapModel>>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState state, DataList<MapModel> data) {
                if (state == DataSource.ResourceState.ok) {
                    Log.d(TAG, "Map amount: " + data.getSize());
                    if (data.getSize() != 0) {
                        long mapPOI = MapModel.POI.get(data.getPOI(0));  // 得到第一个可用地图
                        showMap(mapPOI);
                    }
                } else {
                    Log.e(TAG, "requestMaps() failed");
                }
            }
        });
    }

    private void initClearBtn() {
        Button clrBtn = (Button) findViewById(R.id.map_clearBtn);
        clrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllMark();
            }
        });
    }

    private void showMap(long mapPOI) {
        dataSource.requestPOIChildren(mapPOI, new DataSource.OnRequestDataEventListener<LocationList>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState state, LocationList data) {
                if (state == DataSource.ResourceState.ok) {
                    Log.d(TAG, "Floor amount: " + data.getSize());
                    floorList = data;
                    if (floorList.getSize() != 0) {
                        long floorID = LocationModel.id.get(floorList.getPOI(0));  // 得到默认楼层(1楼)ID
                        showFloor(floorID);
                    }
                } else {
                    Log.e(TAG, "requestPOIChildren() failed");
                }
            }
        });
    }

    private void showFloor(final long floorID) {
        dataSource.requestPlanarGraph(floorID, new DataSource.OnRequestDataEventListener<PlanarGraph>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState state, PlanarGraph data) {
                if (state == DataSource.ResourceState.ok) {
                    curFloorID = floorID;
                    mapView.drawPlanarGraph(data);
                    mapView.start();  // 开始绘制楼层平面图
                } else {
                    Log.e(TAG, "requestPlanarGraph() failed");
                }
            }
        });
    }

    private void addMark(MapView mapView, float x, float y) {
        Types.Point point = mapView.converToWorldCoordinate(x, y);
        Mark mark = new Mark(mapView.getContext());
        mark.setMark(marks.size(), x, y);
        mark.init(new double[]{point.x, point.y});
        mark.setFloorId(curFloorID);
        mapView.addOverlay(mark);
        marks.add(mark);
    }

    private void removeAllMark() {
        mapView.removeAllOverlay();
        marks.clear();
    }
}
