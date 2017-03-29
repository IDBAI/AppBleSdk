package com.revenco.appblesdk.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.revenco.appblesdk.R;
import com.revenco.network.utils.HttpRequest;

import java.util.HashMap;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017/3/22 15:11.</p>
 * <p>CLASS DESCRIBE :</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.shake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShakeActivity.class);
                startActivity(intent);
//                test();
            }
        });
        findViewById(R.id.auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AutoTestActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.httpTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 http://app.utvgo.com:8099/utvgoClient/interfaces/content_listContent.action?channelId=6&pageNo=1&pageSize=30&typeId=0&keyWord=%E5%87%BA
                 **/
                HttpRequest httpRequest = new HttpRequest("http://app.utvgo.com:8099/utvgoClient/interfaces/content_listContent.action");
                String[] key = {"channelId", "pageNo", "pageSize", "typeId", "keyWord"};
                String[] values = {"6", "1", "30", "0", "%E5%87%BA"};
                HashMap<String, String> hashMap = httpRequest.geneHashMap(key, values);
                httpRequest.addParams(hashMap);
                //POST 或者 GET 方式均测试可以
                httpRequest.executGet(null);
//                httpRequest.executPost(null);
//
//                HttpRequest httpRequest = new HttpRequest("http://res.weicontrol.cn/api_V2/LoginIFace");
//                String[] key = {"12345678910"};
//                String[] values = {"1234656"};
//                httpRequest.addParams(httpRequest.geneHashMap(key, values));
//                httpRequest.executPost(null);
            }
        });
    }

    private void test() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("标题");
        builder.setMessage("Android 6.0需要动态请求权限，请允许.");
        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "test!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}
