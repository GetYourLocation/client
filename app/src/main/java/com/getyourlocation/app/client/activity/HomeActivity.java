package com.getyourlocation.app.client.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.getyourlocation.app.client.R;
import com.getyourlocation.app.client.util.PermissionUtil;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtil.grantPermissions(this);
        Button testBtn = (Button) findViewById(R.id.main_test_btn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, SensorNetworkActivity.class));
            }
        });
        Button dataBtn = (Button) findViewById(R.id.main_data_btn);
        dataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, CollectDataActivity.class));
            }
        });
        Button locBtn = (Button) findViewById(R.id.main_localization_btn);
        locBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, LocalizationActivity.class));
            }
        });
    }
}
