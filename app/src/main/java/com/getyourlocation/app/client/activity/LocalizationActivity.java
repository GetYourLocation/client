package com.getyourlocation.app.client.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.getyourlocation.app.client.R;

import com.getyourlocation.app.client.util.CommonUtil;
import com.getyourlocation.app.client.util.NetworkUtil;

/**
 * TODO 实现定位功能
 */
public class LocalizationActivity extends AppCompatActivity {
    private static final String TAG = "LocalizationActivity";

    private NetworkUtil networkUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        Button testBtn = (Button) findViewById(R.id.loc_test_btn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.showToast(LocalizationActivity.this, "Hello!");
                startActivity(new Intent(LocalizationActivity.this, PhotoActivity.class));
            }
        });
    }
}
