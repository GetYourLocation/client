package com.getyourlocation.app.gylclient.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;

import com.getyourlocation.app.gylclient.R;

import com.android.volley.toolbox.ImageLoader;
import com.getyourlocation.app.gylclient.Constant;
import com.getyourlocation.app.gylclient.R;
import com.getyourlocation.app.gylclient.util.CommonUtil;
import com.getyourlocation.app.gylclient.util.NetworkUtil;
import com.getyourlocation.app.gylclient.util.SensorUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LocalizationActivity extends AppCompatActivity {
    private static final String TAG = "LocalizationActivity";
    private TextView sensorInfoTxt;
    private EditText x1ValTxt;
    private EditText y1ValTxt;
    private EditText x2ValTxt;
    private EditText y2ValTxt;
    private EditText x3ValTxt;
    private EditText y3ValTxt;
    private EditText AlphaValTxt;
    private EditText BetaValTxt;
    private ImageButton imgBtn;
    private ImageView resultImgView;
    private NetworkUtil networkUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        // TODO 定位
        initData();
        //initBtn();
        //initResultImgView();
        initNetwork();
    }

    private void initNetwork() {
        networkUtil = NetworkUtil.getInstance(this);
    }

    private void initData() {
        x1ValTxt = (EditText) findViewById(R.id.test_x1_editTxt);
        y1ValTxt = (EditText) findViewById(R.id.test_y1_editTxt);
        x2ValTxt = (EditText) findViewById(R.id.test_x2_editTxt);
        y2ValTxt = (EditText) findViewById(R.id.test_y2_editTxt);
        x3ValTxt = (EditText) findViewById(R.id.test_x3_editTxt);
        y3ValTxt = (EditText) findViewById(R.id.test_y3_editTxt);
        AlphaValTxt = (EditText) findViewById(R.id.test_alpha_editTxt);
        BetaValTxt = (EditText) findViewById(R.id.test_beta_editTxt);
        Button ResultBtn = (Button) findViewById(R.id.GetLocationBtn);
        ResultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x1 = Integer.parseInt(x1ValTxt.getText().toString());
                int y1 = Integer.parseInt(y1ValTxt.getText().toString());
                int x2 = Integer.parseInt(x2ValTxt.getText().toString());
                int y2 = Integer.parseInt(y2ValTxt.getText().toString());
                int x3 = Integer.parseInt(x3ValTxt.getText().toString());
                int y3 = Integer.parseInt(y3ValTxt.getText().toString());
                float alpha = Integer.parseInt(AlphaValTxt.getText().toString());
                float beta = Integer.parseInt(BetaValTxt.getText().toString());
                GetLocation(x1, y1, x2, y2, x3, y3, alpha, beta);
            }
        });
    }
    private void GetLocation(final int x1, final int y1, final int x2, final int y2,
                             final int x3, final int y3, final float alpha, final  float beta) {
        StringRequest req = new StringRequest(Request.Method.GET, Constant.URL_API_GETLOCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {  // Called when server respond
                        Log.d(TAG, response);
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            double x = (double)jsonObj.get("x");
                            double y = (double)jsonObj.get("y");
                            CommonUtil.showToast(LocalizationActivity.this, String.valueOf("x:" + x + "y:" + y ));
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
                params.put("x1", String.valueOf(x1));
                params.put("y1", String.valueOf(y1));
                params.put("x2", String.valueOf(x2));
                params.put("y2", String.valueOf(y2));
                params.put("x3", String.valueOf(x3));
                params.put("y3", String.valueOf(y3));
                params.put("alpha", String.valueOf(alpha));
                params.put("beta", String.valueOf(beta));
                return params;
            }
        };
        networkUtil.addReq(req);
    }
}
