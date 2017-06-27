package com.getyourlocation.app.client.activity;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.ImageLoader;
import com.getyourlocation.app.client.Constant;
import com.getyourlocation.app.client.R;
import com.getyourlocation.app.client.util.CommonUtil;
import com.getyourlocation.app.client.util.NetworkUtil;
import com.getyourlocation.app.client.util.SensorUtil;
import com.getyourlocation.app.client.widget.CameraPreview;


public class CameraActivity extends AppCompatActivity {
    private Camera camera;
    private CameraPreview cameraPreview;
    private Button shootBtn;
    private boolean isRecording = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

         shootBtn = (Button) findViewById(R.id.button_shoot);
        shootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView mipmap1 = (ImageView) findViewById(R.id.mipmap1);
                mipmap1.setImageResource(R.mipmap.ic_launcher_round);
                mipmap1.setVisibility(View.VISIBLE);

                ImageView mipmap2 = (ImageView) findViewById(R.id.mipmap2);
                mipmap2.setImageResource(R.mipmap.ic_launcher_round);
                mipmap2.setVisibility(View.VISIBLE);

                ImageView mipmap3 = (ImageView) findViewById(R.id.mipmap3);
                mipmap3.setImageResource(R.mipmap.ic_launcher_round);
                mipmap3.setVisibility(View.VISIBLE);
            }
        });

        Button uploadBtn = (Button) findViewById(R.id.button_upload);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CameraActivity.this, HomeActivity.class));
            }
        });

        ImageButton cancelBtn1 = (ImageButton) findViewById(R.id.cancel_1);
        cancelBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView mipmap1 = (ImageView) findViewById(R.id.mipmap1);
                mipmap1.setVisibility(View.INVISIBLE);
            }
        });

        ImageButton cancelBtn2 = (ImageButton) findViewById(R.id.cancel_2);
        cancelBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView mipmap1 = (ImageView) findViewById(R.id.mipmap2);
                mipmap1.setVisibility(View.INVISIBLE);
            }
        });

        ImageButton cancelBtn3 = (ImageButton) findViewById(R.id.cancel_3);
        cancelBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView mipmap1 = (ImageView) findViewById(R.id.mipmap3);
                mipmap1.setVisibility(View.INVISIBLE);
            }
        });

    }

}
