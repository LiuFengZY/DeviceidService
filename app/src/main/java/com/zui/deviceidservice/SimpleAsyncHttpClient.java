package com.zui.deviceidservice;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
                break;
            case HTTP_POST:
                //HttpThreadPoolUtils.execute(new HttpRequestPostRunnable(null, callback, url, false));
                HttpRequestPostRunnable httpRun = new HttpRequestPostRunnable(null,callback,url,data);
                httpRun.run();
                break;
            default:
                break;
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
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setReadTimeout(READ_TIME_OUT);
                urlConnection.setConnectTimeout(CONNECT_TIME_OUT);
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("connection", "close");
                urlConnection.setRequestProperty("Content-Type", "application/encrypted-json");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(mData.length()));
                OutputStream os = urlConnection.getOutputStream();
                os.write(mData.getBytes());
                int code = urlConnection.getResponseCode();
                if (code >= 200 && code < 400) {
                    is = urlConnection.getInputStream();
                    byte sRead[] = read(is);
                    String strRead = new String(sRead, ENCODE);
                    Log.d(TAG, "liufeng http post, read111 : " + "len..." + strRead.length() + strRead);
                    mCallback.onSuccess(strRead);
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
