package com.fuyo.jumplogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;


import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.HttpConnectionMetricsImpl;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.renderscript.RenderScript.RSErrorHandler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fuyo.jumplogger.JumpRecord;


public class InternetConnection {
    public static String post (final String url, ArrayList<BasicNameValuePair> params) {
        try {
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(10000));
            httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(30000));
            httpParams.setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Linux; U; Android 4.0.1; ja-jp; Galaxy Nexus Build/ITL41D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
            HttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(HttpResponse response)
                        throws ClientProtocolException, IOException {
                    switch (response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_OK:
                            String body = "";
                            InputStream stream = null;
                            if (isGZipHttpResponse(response)) {
                                stream = new GZIPInputStream(response.getEntity().getContent());
                            } else {
                                stream = response.getEntity().getContent();
                            }
                            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                            try {
                                StringBuilder buf = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    buf.append(line + "\n");
                                }
                                body = buf.toString();
                            } finally {
                                stream.close();
                                reader.close();
                            }

                            //					Log.d("upload", body);
                            if (body.startsWith("error")) {
                                return "";
                            }
                            return body;
                        default:
                            return "";
                    }
                }
                private boolean isGZipHttpResponse(HttpResponse response) {
                    Header header = response.getEntity().getContentEncoding();
                    if (header == null) return false;

                    String value = header.getValue();
                    return (!TextUtils.isEmpty(value) && value.contains("gzip"));
                }

            };

            String result = "";
            result = httpClient.execute(httpPost, responseHandler);
            httpClient.getConnectionManager().shutdown();
            return result;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }
    public static String downloadString (final String type, final String url, final String email, final String password, final String label) {

        ArrayList<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("user", email));
        params.add(new BasicNameValuePair("pass", password));
        params.add(new BasicNameValuePair("label", label));
        return post(url, params);


    }
    public static String updateJumpRecord (final String url, final String email, final String password, final JumpRecord record) {
        ArrayList<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", "update"));
        params.add(new BasicNameValuePair("user", email));
        params.add(new BasicNameValuePair("pass", password));
        params.add(new BasicNameValuePair("record_id", record.id));
        params.add(new BasicNameValuePair("record_sportsType", record.sportsType));
        params.add(new BasicNameValuePair("record_location", record.location));
        params.add(new BasicNameValuePair("record_isSuccess", Integer.toString(record.isSuccess)));
        params.add(new BasicNameValuePair("record_trickName", record.trickName));
        return post(url, params);
    }
}
