package com.getyourlocation.app.client.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.getyourlocation.app.client.R;

public class testActivity extends AppCompatActivity {
    public final static int RESULT_CODE=1;
    private float x = 100;
    private float y = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Button Btn = (Button) findViewById(R.id.button1);
        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("x", x);
                intent.putExtra("y", y);
                setResult(RESULT_CODE, intent);
                finish();
            }
        });
    }
}
