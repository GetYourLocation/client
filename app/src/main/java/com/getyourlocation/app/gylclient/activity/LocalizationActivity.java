package com.getyourlocation.app.gylclient.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;

import com.getyourlocation.app.gylclient.R;

import com.getyourlocation.app.gylclient.Constant;
import com.getyourlocation.app.gylclient.util.CommonUtil;
import com.getyourlocation.app.gylclient.util.NetworkUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LocalizationActivity extends AppCompatActivity {
    private static final String TAG = "LocalizationActivity";

    private NetworkUtil networkUtil;

    private EditText x1Txt;
    private EditText y1Txt;
    private EditText x2Txt;
    private EditText y2Txt;
    private EditText x3Txt;
    private EditText y3Txt;
    private EditText alphaTxt;
    private EditText betaTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        initNetwork();
        initData();
    }

    private void initNetwork() {
        networkUtil = NetworkUtil.getInstance(this);
    }

    private void initData() {
        x1Txt = (EditText) findViewById(R.id.test_x1_editTxt);
        y1Txt = (EditText) findViewById(R.id.test_y1_editTxt);
        x2Txt = (EditText) findViewById(R.id.test_x2_editTxt);
        y2Txt = (EditText) findViewById(R.id.test_y2_editTxt);
        x3Txt = (EditText) findViewById(R.id.test_x3_editTxt);
        y3Txt = (EditText) findViewById(R.id.test_y3_editTxt);
        alphaTxt = (EditText) findViewById(R.id.test_alpha_editTxt);
        betaTxt = (EditText) findViewById(R.id.test_beta_editTxt);
        Button ResultBtn = (Button) findViewById(R.id.GetLocationBtn);
        ResultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x1 = Integer.parseInt(x1Txt.getText().toString());
                int y1 = Integer.parseInt(y1Txt.getText().toString());
                int x2 = Integer.parseInt(x2Txt.getText().toString());
                int y2 = Integer.parseInt(y2Txt.getText().toString());
                int x3 = Integer.parseInt(x3Txt.getText().toString());
                int y3 = Integer.parseInt(y3Txt.getText().toString());
                float alpha = Integer.parseInt(alphaTxt.getText().toString());
                float beta = Integer.parseInt(betaTxt.getText().toString());
                GetLocation(x1, y1, x2, y2, x3, y3, alpha, beta);
            }
        });
    }

    private void GetLocation(final int x1, final int y1, final int x2, final int y2,
                             final int x3, final int y3, final float alpha, final  float beta) {
        StringRequest req = new StringRequest(Request.Method.GET, Constant.URL_API_LOCALIZATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {  // Called when server respond
                        Log.d(TAG, response);
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            double x = (double)jsonObj.get("x");
                            double y = (double)jsonObj.get("y");
                            CommonUtil.showToast(LocalizationActivity.this, String.valueOf("x:" + x + " y:" + y ));
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
