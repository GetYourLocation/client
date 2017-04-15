package com.getyourlocation.app.gyl_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getyourlocation.app.gyl_client.Constant;
import com.getyourlocation.app.gyl_client.Mark;
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

    private int floorIndex = 0;

    // Mark container layer layout
    private RelativeLayout overlay_container;
    private ArrayList<Mark> mark_list;
    private int markNum = 0;
    private long currentFloorId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initDataSource();
        initMap();
        initOverlayContainer();
        initSingleTaptoMark();
        initFloorTxt();
        initFloorBtn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.drop();
    }
    private  void initSingleTaptoMark(){

        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(MapView mapView, float x, float y) {
                Types.Point point = mapView.converToWorldCoordinate(x, y);
                Mark mark = new Mark(mapView.getContext());
                mark.setMark(++markNum, x, y);
                mark.init(new double[]{point.x, point.y});
                mark.setFloorId(currentFloorId);
                Log.d("MapActivity","x:"+x+"y"+y);
                mapView.addOverlay(mark);
                mark_list.add(mark);
            }
        });
    }
    private void initDataSource() {
        dataSource = new DataSource(Constant.URL_SERVER);
    }
    private void initOverlayContainer(){
        overlay_container=(RelativeLayout)findViewById(R.id.map_view_container);
        setOverlay_container();
        mark_list = new ArrayList<Mark>();
    }
    private void setOverlay_container(){
        mapView.setOverlayContainer(overlay_container);
    }
    private void initMap() {
        mapView = (MapView) findViewById(R.id.map_mapView);
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

    private void initFloorTxt() {
        floorTxt = (TextView) findViewById(R.id.floor_text);
    }

    private void initFloorBtn() {
        Button upBtn = (Button) findViewById(R.id.up_botton);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floorList != null && floorIndex < floorList.getSize() - 1) {
                    floorIndex++;
                    Log.d(TAG, "Floor: " + String.valueOf(floorIndex));
                    showFloor(LocationModel.id.get(floorList.getPOI(floorIndex)));
                }
                floorTxt.setText(String.format(Locale.CHINA, "Floor: %d", floorIndex + 1));
            }
        });

        Button lowBtn = (Button) findViewById(R.id.low_botton);
        lowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floorList != null && floorIndex > 0) {
                    floorIndex--;
                    Log.d(TAG, "Floor: " + String.valueOf(floorIndex));
                    showFloor(LocationModel.id.get(floorList.getPOI(floorIndex)));
                }
                floorTxt.setText(String.format(Locale.CHINA, "Floor: %d", floorIndex + 1));
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

    private void showFloor(long floorID) {
        // get FloorId to Add Mark in correspond Floor
        currentFloorId = floorID;
        dataSource.requestPlanarGraph(floorID, new DataSource.OnRequestDataEventListener<PlanarGraph>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState state, PlanarGraph data) {
                if (state == DataSource.ResourceState.ok) {
                    mapView.drawPlanarGraph(data);
                    mapView.start();  // 开始绘制楼层平面图
                } else {
                    Log.e(TAG, "requestPlanarGraph() failed");
                }
            }
        });
    }
    public void AddMark(MapView mapView, float x, float y) {
        //将屏幕坐标转换为事件坐标
        Types.Point point = mapView.converToWorldCoordinate(x, y);
        //创建一个覆盖物
        Mark mark = new Mark(getApplicationContext());
        mark.setMark(++markNum, x, y);
        //把世界坐标传递给它
        mark.init(new double[]{point.x, point.y});
        //将这个覆盖物添加到MapView中
        mark.setFloorId(currentFloorId);
        mark_list.add(mark);
        mapView.addOverlay(mark);
    }
    public void RemoveMark(MapView mapView, long Id){
        for (Mark m:mark_list
                ) {
            if (m.getId() == Id) {
                mapView.removeOverlay(m);
                mark_list.remove(m);
                markNum--;
            }

        }
    }
    public void RemoveAllMark(MapView mapView){
        mapView.removeAllOverlay();
        mark_list.clear();
        markNum =0;
    }
}
