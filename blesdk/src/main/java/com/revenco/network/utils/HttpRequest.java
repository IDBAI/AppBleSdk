package com.revenco.network.utils;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.revenco.network.utils.HttpRequest.httpMethod.GET;
import static com.revenco.network.utils.HttpRequest.httpMethod.POST;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-28 15:09.</p>
 * <p>CLASS DESCRIBE :</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class HttpRequest implements Runnable {
    private static final int CONNECT_TIMEOUT = 6_000;
    private static final int READ_TIMEOUT = 10_000;
    private static final int MSG_SUCCEED = 100;
    private static final int MSG_CANCEL = 101;
    private static final int MSG_FAILED = 102;
    /**
     * 最大重试次数2次
     */
    private static final int HTTP_RETRY_MAX = 2;
    private Thread thread;
    private HttpURLConnection connection;
    private HashMap<String, String> params;
    private RequestListener listener;
    private boolean isCancel;
    private httpMethod Method = httpMethod.GET;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            System.out.println("handleMessage() called with: msg.obj = [" + (String) msg.obj + "]");
            switch (msg.what) {
                case MSG_SUCCEED:
                    if (listener != null)
                        listener.onSucceed((String) msg.obj);
                    break;
                case MSG_FAILED:
                    if (listener != null)
                        listener.onFailed((String) msg.obj);
                    break;
                case MSG_CANCEL:
                    System.out.println("请求被取消！");
                    break;
            }
            return false;
        }
    });
    private String urlString;

    public HttpRequest(String urlString) {
        this.urlString = urlString;
        thread = new Thread(this);
    }

    /**
     * 添加请求参数
     *
     * @param requeJson
     */
    public void addParams(HashMap requeJson) {
        this.params = requeJson;
    }

    /**
     * 执行http请求，根据添加的请求参数格式，自动判断执行 GET 或者 POST 请求，如果未添加请求参数，默认是 GET 请求
     *
     * @param listener
     */
    public void executPost(RequestListener listener) {
        Method = POST;
        this.listener = listener;
        this.thread.start();
    }

    public void executGet(RequestListener listener) {
        Method = GET;
        this.listener = listener;
        this.thread.start();
    }

    public void cancel() {
        isCancel = true;
        if (connection != null)
            connection.disconnect();
        if (thread != null)
            thread.interrupt();
    }

    @Override
    public void run() {
        try {
            if (Method == POST) {
                sendPost();
            } else {
                sendGet();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Message message = mHandler.obtainMessage();
            message.what = MSG_FAILED;
            message.obj = "URL格式错误！";
            mHandler.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            Message message = mHandler.obtainMessage();
            message.what = MSG_FAILED;
            message.obj = "网络连接异常！";
            mHandler.sendMessage(message);
        } finally {
            if (connection != null)
                connection.disconnect();
            connection = null;
        }
    }

    private void sendGet() throws IOException {
        if (params != null) {
            StringBuilder sb = new StringBuilder();
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                sb.append(key).append("=").append(value).append("&");
            }
            urlString = urlString + "?" + sb.deleteCharAt(sb.length() - 1).toString();
        }
        URL url = new URL(urlString);
        int send = 0;
        int responseCode;
        do {
            if (connection != null)
                connection.disconnect();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(true);//post 方式不能使用缓存
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();
            if (isCancel) {
                connection.disconnect();
                thread.interrupt();
                Message message = mHandler.obtainMessage();
                message.what = MSG_CANCEL;
                mHandler.sendMessage(message);
                return;
            }
            send++;
            System.out.println(send + "、" + Method.toString() + " 请求 url -> " + url.toString());
            System.out.println(send + "、" + Method.toString() + " 请求 params -> " + params);
            //发送请求
            responseCode = connection.getResponseCode();
            System.out.println(send + "、请求 响应码 -> " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                parseSuccess();
                break;
            } else {
                if (send == HTTP_RETRY_MAX)
                    parseFailed(responseCode, url);
            }
        }
        while (send < HTTP_RETRY_MAX);
    }

    private void sendPost() throws IOException {
        byte[] body = null;
        if (params != null) {
            StringBuilder sb = new StringBuilder();
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                sb.append(key).append("=").append(value).append("&");
            }
            String params = sb.deleteCharAt(sb.length() - 1).toString();
            body = params.getBytes("utf-8");
        }
        URL url = new URL(urlString);
        int send = 0;
        int responseCode;
        do {
            if (connection != null)
                connection.disconnect();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);//post 方式不能使用缓存
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            if (body != null)
                connection.setRequestProperty("Content-Length", String.valueOf(body.length));
//            connection.connect();//post此处不需要connect
            if (body != null) {
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.write(body);
                out.close();
            }
            if (isCancel) {
                connection.disconnect();
                thread.interrupt();
                Message message = mHandler.obtainMessage();
                message.what = MSG_CANCEL;
                mHandler.sendMessage(message);
                return;
            }
            send++;
            System.out.println(send + "、" + Method.toString() + " 请求 url -> " + url.toString());
            System.out.println(send + "、" + Method.toString() + " 请求 params -> " + params);
            //发送请求
            responseCode = connection.getResponseCode();
            System.out.println(send + "、请求 响应码 -> " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                parseSuccess();
                break;
            } else {
                if (send == HTTP_RETRY_MAX)
                    parseFailed(responseCode, url);
            }
        }
        while (send < HTTP_RETRY_MAX);
    }

    private void parseFailed(int responseCode, URL url) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_FAILED;
        message.obj = getErrorMsg(responseCode);
        mHandler.sendMessage(message);
    }

    private Object getErrorMsg(int responseCode) {
        String error = "其他错误";
        switch (responseCode) {
            case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                error = "客户端连接超时";
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
                error = "找不到服务";
                break;
            case HttpURLConnection.HTTP_BAD_METHOD:
                error = "方法错误";
                break;
            case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
                error = "请求参数太大";
                break;
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                error = "服务器错误";
                break;
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                error = "网关超时";
                break;
        }
        return error + ", 错误代码：" + responseCode;
    }

    private void parseSuccess() {
        try {
            InputStreamReader streamReader = new InputStreamReader(connection.getInputStream(), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            StringBuilder sb = new StringBuilder();
            String line = "";
            for (line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
                sb.append(line);
            bufferedReader.close();
            Message message = mHandler.obtainMessage();
            message.what = MSG_SUCCEED;
            message.obj = sb.toString();
            mHandler.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            Message message = mHandler.obtainMessage();
            message.what = MSG_FAILED;
            message.obj = "解析数据产生IO异常！";
            mHandler.sendMessage(message);
        }
    }

    /**
     * 生成 HashMap 对象
     *
     * @param key
     * @param values
     * @return
     */
    public HashMap<String, String> geneHashMap(String[] key, String[] values) {
        if (key.length != values.length)
            return null;
        HashMap<String, String> hashMap = null;
        hashMap = new HashMap<>();
        for (int i = 0; i < key.length; i++) {
            hashMap.put(key[i], values[i]);
        }
        return hashMap;
    }

    public enum httpMethod {
        POST,
        GET
    }

    public interface RequestListener {
        void onSucceed(String json);

        void onFailed(String err);
    }
}
