package com.getyourlocation.app.gyl_client.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.getyourlocation.app.gyl_client.R;

public class HomeActivity extends AppCompatActivity {

    @Override//
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button testBtn = (Button) findViewById(R.id.main_test_btn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, TestActivity.class));
            }
        });
        Button mapBtn = (Button) findViewById(R.id.main_map_btn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, MapActivity.class));
            }
        });
    }
}
