package com.zui.deviceidservice;

import android.content.Context;
import android.util.Log;

import com.zui.deviceidservice.db.Encoder;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SimpleAsyncHttpClient {
    private static final String TAG = "HTTP-Client";
    private static int READ_TIME_OUT = 10 * 1000;
    private static int CONNECT_TIME_OUT = 10 * 1000;
    private static final String ENCODE = "UTF-8";
    private static final boolean DBG = true;

    public interface HttpCallback <T> {
        public void onSuccess(T response);
        public void onError(T error);
    }

    public enum HTTP_REQUEST_METHOD {
        HTTP_GET,
        HTTP_POST
    }

    public static final Map<HTTP_REQUEST_METHOD, String> methodMap;
    static {
        methodMap = new HashMap<HTTP_REQUEST_METHOD, String>();
        methodMap.put(HTTP_REQUEST_METHOD.HTTP_GET, "GET");
        methodMap.put(HTTP_REQUEST_METHOD.HTTP_POST, "POST");
    }

    public static void doHttpRequest(HTTP_REQUEST_METHOD method, String url, final HttpCallback<String> callback, String data) {
        switch(method) {
            case HTTP_GET:
                HttpThreadPoolUtils.execute(new HttpRequestGetRunnable(null, callback, url));
                break;
            case HTTP_POST:
                HttpThreadPoolUtils.execute(new HttpRequestPostRunnable(null, callback, url, data));
                break;
            default:
                break;
        }
    }

    public String makeHttpGetData(String url, final Map<String, String> params) {
        StringBuffer buffer = new StringBuffer(url);
        buffer.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            buffer.append(entry.getKey())
                    .append("=")
                    .append(Encoder.encode(entry.getValue()))
                    .append("&");
        }
        return buffer.toString();
    }

    /**
     *
     */
    static class HttpRequestGetRunnable implements Runnable {
        private Context mContext = null;
        private HttpCallback mCallback = null;
        private String mUrl = "";

        public HttpRequestGetRunnable(final Context context, final HttpCallback callBack, final String url) {
            this.mContext = context;
            this.mCallback = callBack;
            this.mUrl = url;
        }

        @Override
        public void run() {
            URL url = null;
            try {
                url = new URL(this.mUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                mCallback.onError(e.getMessage());
                return;
            }
            HttpURLConnection connection = null;
            InputStream is = null;
            BufferedReader br = null;
            Log.d(TAG, "liufeng http get111, url : " + url.toString());
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(READ_TIME_OUT);
                connection.setConnectTimeout(CONNECT_TIME_OUT);
                connection.setUseCaches(false);
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("Content-Type", "application/encrypted-json");
                connection.connect();
                Log.d(TAG, "liufeng http get, connection : " + connection.getResponseCode());
                if (connection.getResponseCode() == 200) {
                    is = connection.getInputStream();
                    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    StringBuffer sbf = new StringBuffer();
                    String temp = null;
                    while ((temp = br.readLine()) != null) {
                        sbf.append(temp);
                        sbf.append("\r\n");
                    }
                    if (DBG) Log.d(TAG, "liufeng http get, return : " + sbf.toString());

                    JSONTokener jsonParser = new JSONTokener(new String(sbf));
                    JSONObject object = (JSONObject)jsonParser.nextValue();
                    String code = object.getString("code");
                    String msg = object.getString("msg");
                    if (DBG) Log.d(TAG, "return code:" + code);
                    if (DBG) Log.d(TAG, "return msg:" + msg);
                    if ("0".equals(code) && "success".equals(msg)) {
                        JSONArray jsa = (JSONArray)object.getJSONArray("data");
                        JSONObject object2 = (JSONObject)jsa.get(0);
                        String developerId = object2.getString("developerId");
                        String applicationDate = object2.getString("applicationDate");
                        if (DBG) Log.d(TAG, "developerid:" + developerId);
                        if (DBG) Log.d(TAG, "applicationDate:" + applicationDate);
                        mCallback.onSuccess(developerId);
                    } else {
                        mCallback.onError("response code error:" + code);
                    }
                } else {
                    mCallback.onError("response code error: " + connection.getResponseCode());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "liufeng http get,IO Exception : ");
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (null != br) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != is) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                connection.disconnect();
            }

        }
    }

    /**
     * Http Post Runnable.
     */
    static class HttpRequestPostRunnable implements Runnable {

        private Context mContext = null;
        private HttpCallback mCallback = null;
        private String mUrl = "";
        private String mData = "";

        public HttpRequestPostRunnable(final Context context,
                                       final HttpCallback callBack, final String url, String data) {
            this.mContext = context;
            this.mCallback = callBack;
            this.mUrl = url;
            this.mData = data;
        }

        @Override
        public void run() {
            URL url = null;
            try {
                url = new URL(this.mUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                mCallback.onError(e.getMessage());
                return;
            }

            HttpURLConnection urlConnection = null;
            InputStream is = null;
            try {
                Log.d(TAG, "liufeng, http post, url " + mUrl);
                Log.d(TAG, "liufeng, http post, data:" + mData);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setReadTimeout(READ_TIME_OUT);
                urlConnection.setConnectTimeout(CONNECT_TIME_OUT);
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("Charset", "UTF-8");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(mData.length()));
                urlConnection.connect();
                OutputStream os = urlConnection.getOutputStream();
                os.write(mData.getBytes());
                os.flush();
                int code = urlConnection.getResponseCode();
                Log.d(TAG, "liufeng http code.response:" + code);
                if (code >= HttpURLConnection.HTTP_OK && code < HttpURLConnection.HTTP_BAD_REQUEST) {
                    is = urlConnection.getInputStream();
                    byte sRead[] = read(is);
                    String strRead = new String(sRead, ENCODE);
                    Log.d(TAG, "liufeng http post, read111 : " + "len..." + strRead.length() + strRead);
                    JSONTokener jsonParser = new JSONTokener((strRead));
                    JSONObject object = (JSONObject)jsonParser.nextValue();
                    String userid = object.getString("userid");
                    mCallback.onSuccess(userid);
                } else {

                    is = urlConnection.getErrorStream();
                    byte sRead[] = read(is);
                    String responseError = new String(sRead, ENCODE);
                    Log.d(TAG, "liufeng http post, ERROR : " + "responseError..." + responseError);
                    mCallback.onError(responseError);
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                mCallback.onError(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                mCallback.onError(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                        urlConnection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static byte[] read(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = inStream.read(buffer)) != -1)
        {
            outStream.write(buffer,0,len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

}
