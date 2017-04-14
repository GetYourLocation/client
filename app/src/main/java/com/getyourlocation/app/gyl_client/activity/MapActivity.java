package com.getyourlocation.app.gyl_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.getyourlocation.app.gyl_client.Constant;
import com.getyourlocation.app.gyl_client.R;
import com.getyourlocation.app.gyl_client.util.FileUtil;
import com.palmaplus.nagrand.data.DataList;
import com.palmaplus.nagrand.data.DataSource;
import com.palmaplus.nagrand.data.LocationList;
import com.palmaplus.nagrand.data.LocationModel;
import com.palmaplus.nagrand.data.MapModel;
import com.palmaplus.nagrand.data.PlanarGraph;
import com.palmaplus.nagrand.view.MapView;


public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    private MapView mapView;
    private DataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initDataSource();
        initMap();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.drop();
    }

    private void initDataSource() {
        dataSource = new DataSource(Constant.URL_SERVER);
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

    private void showMap(long mapPOI) {
        dataSource.requestPOIChildren(mapPOI, new DataSource.OnRequestDataEventListener<LocationList>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState state, LocationList data) {
                if (state == DataSource.ResourceState.ok) {
                    Log.d(TAG, "Floor amount: " + data.getSize());
                    if (data.getSize() != 0) {
                        long floorID = LocationModel.id.get(data.getPOI(0));  // 得到默认楼层(1楼)ID
                        showFloor(floorID);
                    }
                } else {
                    Log.e(TAG, "requestPOIChildren() failed");
                }
            }
        });
    }

    private void showFloor(long floorID) {
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
}
