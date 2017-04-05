package com.getyourlocation.app.gyl_client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.getyourlocation.app.gyl_client.util.CommonUtil;
import com.getyourlocation.app.gyl_client.util.SensorUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";

    private static final String SERVER_IP = "http://192.168.199.105";
    private static final String URL_SUM = SERVER_IP + "/gyl/api/sum";
    private static final String URL_PRODUCT = SERVER_IP + "/gyl/api/product";

    private SensorUtil sensorUtil;
    private RequestQueue requestQueue;

    private TextView sensorInfoTxt;
    private EditText xValTxt;
    private EditText yValTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initReqQueue();
        initSensor();
        initSumProduct();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorUtil.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorUtil.unregister();
    }

    private void initReqQueue() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    private void initSensor() {
        sensorInfoTxt = (TextView) findViewById(R.id.test_sensorInfoTxt);
        Button resetBtn = (Button) findViewById(R.id.test_resetBtn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorUtil.reset();
            }
        });
        sensorUtil = SensorUtil.getInstance(this);
        sensorUtil.setOnSensorUpdatedListener(new SensorUtil.OnSensorUpdatedListener() {
            @Override
            public void onUpdated() {
                float[] acceleration = sensorUtil.getAcceleration();
                float[] gyroRotate = sensorUtil.getGyroRotate();
                float compassRotate = sensorUtil.getCompassRotate();
                sensorInfoTxt.setText(getString(R.string.sensor_data,
                        acceleration[0],
                        acceleration[1],
                        acceleration[2],
                        gyroRotate[0],
                        gyroRotate[1],
                        gyroRotate[2],
                        compassRotate));
            }
        });
    }

    private void initSumProduct() {
        xValTxt = (EditText) findViewById(R.id.test_x_editTxt);
        yValTxt = (EditText) findViewById(R.id.test_y_editTxt);
        Button sumBtn = (Button) findViewById(R.id.test_sumBtn);
        Button productBtn = (Button) findViewById(R.id.test_productBtn);
        sumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = Integer.parseInt(xValTxt.getText().toString());
                int y = Integer.parseInt(yValTxt.getText().toString());
                computeSum(x, y);
            }
        });
        productBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = Integer.parseInt(xValTxt.getText().toString());
                int y = Integer.parseInt(yValTxt.getText().toString());
                computeProduct(x, y);
            }
        });
    }

    private void computeSum(final int x, final int y) {
        requestQueue.add(new StringRequest(Request.Method.GET, URL_SUM, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    int ans = (int)jsonObj.get("ans");
                    CommonUtil.showToast(TestActivity.this, String.valueOf(ans));
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "", error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x", String.valueOf(x));
                params.put("y", String.valueOf(y));
                return params;
            }
        });
    }

    private void computeProduct(final int x, final int y) {
        requestQueue.add(new StringRequest(Request.Method.POST, URL_PRODUCT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    int ans = (int)jsonObj.get("ans");
                    CommonUtil.showToast(TestActivity.this, String.valueOf(ans));
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "", error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x", String.valueOf(x));
                params.put("y", String.valueOf(y));
                return params;
            }
        });
    }
}