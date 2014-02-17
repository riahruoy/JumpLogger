package com.fuyo.jumplogger;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class LoginAsyncTask extends AsyncTask<String, Integer, String> {
	int resultStatus = 0;
	String result = "";
	Context context;
	final OnCompleteListener onCompleteListener;
	final String email;
	final String password;
	private ProgressDialog dialog = null;
	final String url;
	LoginAsyncTask(Context context, final String email, final String password, final OnCompleteListener listener) {
		this.context = context;
		this.email = email;
		this.password = password;
		onCompleteListener = listener;
		url = context.getResources().getString(R.string.url_passcheck);
	}
	@Override
	protected String doInBackground(String... params) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		 
		ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
		postParam.add(new BasicNameValuePair("user", email));
		postParam.add(new BasicNameValuePair("pass", password));
		
		try {
		    httpPost.setEntity(new UrlEncodedFormEntity(postParam, "utf-8"));
		    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				
				@Override
				public String handleResponse(HttpResponse response)
						throws ClientProtocolException, IOException {
					switch (response.getStatusLine().getStatusCode()) {
					case HttpStatus.SC_OK:
						String body = EntityUtils.toString(response.getEntity(), "UTF-8");
	
						resultStatus = 1;
						result = body;
						return body;
					default:
						resultStatus = -1;
						return null;
					}
				}
				    	  
			};
			httpClient.execute(httpPost, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		httpClient.getConnectionManager().shutdown();

		
		return null;
	}
    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Logging in...");
        dialog.show();
    }
	@Override
	protected void onPostExecute(String _result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		if (result != null) {
			String[] lines = result.split(",");
			if (lines[0].equals("1")) {
				onCompleteListener.onSuccess();
			} else if (lines[0].equals("0")) {
				onCompleteListener.onFailure();
			}
		}
	}
	
	public static abstract class OnCompleteListener {
		abstract void onSuccess();
		abstract void onFailure();
	}
}
