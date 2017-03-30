package com.revenco.appblesdk.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.revenco.appblesdk.R;
import com.revenco.blesdk.utils.XLog;
import com.revenco.database.bean.BleOpenRecordBean;
import com.revenco.database.bean.CertificateBean;
import com.revenco.database.bean.StatisticalBean;
import com.revenco.database.bean.UserBean;
import com.revenco.database.buss.BleOpenRecordBuss;
import com.revenco.database.helper.BussHelper;
import com.revenco.database.buss.CertificateBuss;
import com.revenco.database.buss.StatisticalBuss;
import com.revenco.database.buss.UserBuss;
import com.revenco.network.utils.HttpRequest;

import java.util.HashMap;
import java.util.List;

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
        findViewById(R.id.dataBaseTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleOpenRecordTest();
                certificateTest();
                statisticalTest();
                userTest();
            }
        });
        findViewById(R.id.fanxingTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<UserBean> userBeen = BussHelper.queryAll(getApplicationContext(), UserBean.class, UserBuss.tableName);
                for (UserBean userBean : userBeen) {
                    System.out.println(userBean.toString());
                }
                List<StatisticalBean> statisticalBeen = BussHelper.queryAll(getApplicationContext(), StatisticalBean.class, StatisticalBuss.tableName);
                for (StatisticalBean statisticalBean : statisticalBeen) {
                    System.out.println(statisticalBean.toString());
                }
                List<CertificateBean> certificateBeen = BussHelper.queryAll(getApplicationContext(), CertificateBean.class, CertificateBuss.tableName);
                for (CertificateBean certificateBean : certificateBeen) {
                    System.out.println(certificateBean.toString());
                }
                List<BleOpenRecordBean> bleOpenRecordBeen = BussHelper.queryAll(getApplicationContext(), BleOpenRecordBean.class, BleOpenRecordBuss.tableName);
                for (BleOpenRecordBean bleOpenRecordBean : bleOpenRecordBeen) {
                    System.out.println(bleOpenRecordBean.toString());
                }
            }
        });
    }

    private void bleOpenRecordTest() {
        BleOpenRecordBean bean = new BleOpenRecordBean();
        bean.certificateIndex = 101;
        bean.deviceAddress = "11223344556677889900";
        bean.deviceId = "测试设备id";
        bean.openConsumeTime = 2.1021f;
        bean.openResult = "sucess";
        bean.RSSI = -55;
        bean.scanTime = 1.021f;
        bean.reason = "成功没原因";
        bean.userId = "godfather";
        int id = BleOpenRecordBuss.insertRow(getApplicationContext(), bean);
        XLog.d(TAG, "开门记录id：" + id);
    }

    private void certificateTest() {
        CertificateBean bean = new CertificateBean();
        bean.appBleMac = "66:66:66:66:66";
        bean.certificateIndex = 1001;
        bean.content = "jdlgjrionkdfjdjf-4-5=1243l5-45645ip6k;23-41=3i354'0954065-=7967901JEGR44T58R5+52";
        bean.deviceAddress = "11223344556677889900";
        bean.deviceId = "测试设备id";
        bean.tag = "保留字段";
        int id = CertificateBuss.insertRow(getApplicationContext(), bean);
        XLog.d(TAG, "证书id :" + id);
    }

    private void statisticalTest() {
        StatisticalBean bean = new StatisticalBean();
        bean.averageOpenTime = 2.101f;
        bean.averageRSSI = -55;
        bean.deviceAddress = "11223344556677889900";
        bean.deviceId = "测试设备id";
        bean.failedCount = 50;
        bean.successCount = 100;
        bean.timeoutCount = 0;
        bean.totalCount = 150;
        bean.successRate = 0.510f;
        int id = StatisticalBuss.insertRow(getApplicationContext(), bean);
        XLog.d(TAG, "统计id：" + id);
    }

    private void userTest() {
        UserBean bean = new UserBean();
        bean.communityId = "测试小区";
        bean.mobileNum = "13800138001";
        bean.userId = "godfather";
        int id = UserBuss.insertRow(getApplicationContext(), bean);
        XLog.d(TAG, "用户id: " + id);
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
