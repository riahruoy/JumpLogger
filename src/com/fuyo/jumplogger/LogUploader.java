package com.fuyo.jumplogger;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;


public class LogUploader extends IntentService {
    private static final int NOTIFICATION_ID = 2;
    private Notification notification;
    final String url;
    private String email;
    private String password;
    public LogUploader(String name) {
		super(name);
		url = "https://www.iijuf.net/jumplogger/api/uploadLogFile.php";
	}

	public LogUploader() {
		this("LogUploadJob");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			if (!isConnected()) return;
			
			
		    email = intent.getStringExtra("email");
		    password = intent.getStringExtra("password");
			final String uploadDir = intent.getStringExtra("uploadDir");
			String logDir = Environment.getExternalStorageDirectory().getPath() + "/" + LogDataBase.LOGDIR_NAME + "/" + LogDataBase.UPLOAD_LOGDIR_NAME;
			File baseDir = new File(logDir);
			if (!baseDir.isDirectory()) throw new Exception("directory not found : " + uploadDir);
			
			List<File> originalLogFiles = new ArrayList<File>();
			List<String> labels = new ArrayList<String>();

			//calcurate total size to be uploaded (uncompressed)
			long totalSizeOfLogFile = 0;
			File[] baseDirFiles = baseDir.listFiles();
			for (int i = 0; i < baseDirFiles.length; i++) {
				File dayDir = baseDirFiles[i];
				File[] files = dayDir.listFiles();
				for (int j = 0; j < files.length; j++) {
					if (files[j].length() > 0) {
						totalSizeOfLogFile += files[j].length();
						originalLogFiles.add(files[j]);
						labels.add(dayDir.getName());
					} else {
						files[j].delete();
					}
				}
				if (dayDir.listFiles().length == 0) {
					dayDir.delete();
				}
			}

			updateNotification("compressing...");
			long totalSizeToBeUploaded = 0;
			List<File> compressedLogFiles = new ArrayList<File>();
			for (File file : originalLogFiles) {
				if (file.getName().contains(".gz")) {
					totalSizeToBeUploaded += file.length();
					compressedLogFiles.add(file);
				} else {
					File compressedFile = new File(file.getAbsolutePath() + ".gz");
					compress(file, compressedFile);
					
					totalSizeToBeUploaded += compressedFile.length();
					
					compressedLogFiles.add(compressedFile);
					file.delete();
				}
			}
			
			
			Log.d("upload", "original : " + totalSizeOfLogFile + " byte , compressed : " + totalSizeToBeUploaded + " byte (" + Math.round(totalSizeToBeUploaded * 100/ totalSizeOfLogFile) + " %)");
			final String accessId = intent.getStringExtra("accessId");

			Log.d("upload", "label:" + labels.size() + ", log:" + compressedLogFiles.size());
			long totalByteSent = 0;
			for (int i = 0; i < compressedLogFiles.size(); i++) {
				File file = compressedLogFiles.get(i);
				String label = labels.get(i);
				updateNotification("uploading...(" + Math.round(totalByteSent /1000) + "/" + Math.round(totalSizeToBeUploaded / 1000)
						+ "KB, " +Math.round(totalByteSent * 100 / totalSizeToBeUploaded) + "%)");
				long fileSize = file.length();
				uploadFile(file, accessId, label);
				totalByteSent +=  fileSize;
			}
			
			//delete empty directory
			baseDirFiles = baseDir.listFiles();
			for (int i = 0; i < baseDirFiles.length; i++) {
				if (baseDirFiles[i].listFiles().length == 0) {
					baseDirFiles[i].delete();
				}
			}

			Log.d("upload", "uploadFinished");
//	        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
//                    Uri.parse("file://" +  Environment.getExternalStorageDirectory()))); 
            String str = Downloader.downloadString("upload", url, email, password, labels.get(0));
            InputStream is = new ByteArrayInputStream(str.getBytes());
            ArrayList<JumpRecord> records = JumpRecord.readAll(is);
            is.close();
            if (records.size() > 0) {
                FileOutputStream os = openFileOutput(JumpRecord.FILENAME, MODE_PRIVATE);
                JumpRecord.writeAll(records, os);
            } else {

            }
            calc(labels);
        } catch (NullPointerException e) {
			// error;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    private void calc(List<String> labels) {
        for (String label : labels) {
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(10000));
            httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(30000));
            HttpClient httpClient = new DefaultHttpClient(httpParams);

            try {
                HttpPost httpPost = new HttpPost("https://www.iijuf.net/jumplogger/api/api.jump.php" + "?type=calc&user=" + URLEncoder.encode(email, "UTF-8") + "&pass=" + URLEncoder.encode(password, "UTF-8") + "&label="+URLEncoder.encode(label, "UTF-8"));

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                    @Override
                    public String handleResponse(HttpResponse response)
                            throws ClientProtocolException, IOException {
                        switch (response.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                                String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                                return body;
                            default:
                                return "NG";
                        }
                    }

                };
                httpClient.execute(httpPost, responseHandler);

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient.getConnectionManager().shutdown();
            httpClient = null;

        }
    }
	
	private void uploadFile(final File file, final String accessId, final String label) {
		HttpParams httpParams = new BasicHttpParams();
		httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(10000));
		httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(30000));
		HttpClient httpClient = new DefaultHttpClient(httpParams);

		try {
			HttpPost httpPost = new HttpPost(url + "?user=" + URLEncoder.encode(email, "UTF-8") + "&pass=" + URLEncoder.encode(password, "UTF-8") + "&label="+URLEncoder.encode(label, "UTF-8"));
			final MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			FileBody fileBody = new FileBody(file, "text/plain");
			reqEntity.addPart("upfile", fileBody);
			httpPost.setEntity(reqEntity);
	
			
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
	
				@Override
				public String handleResponse(HttpResponse response)
						throws ClientProtocolException, IOException {
					switch (response.getStatusLine().getStatusCode()) {
					case HttpStatus.SC_OK:
						String body = EntityUtils.toString(response.getEntity(), "UTF-8");
						Log.d("upload", body);
						file.delete();
						return body;
					default:
						return "NG";
					}
				}
				    	  
			};
			httpClient.execute(httpPost, responseHandler);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		httpClient.getConnectionManager().shutdown();
		httpClient = null;
	}
	private void updateNotification(final String text) {
    	notification = new Notification.Builder(this)
    	.setContentTitle("LogUploader")
    	.setContentText(text)
    	.setSmallIcon(R.drawable.ic_upload)
    	.getNotification();
            

        startForeground(NOTIFICATION_ID, notification);

	}
	private boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if( ni != null ){
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;

	}
	
	private static void compress(final File inputFile, final File outputFile) {
		InputStream is = null;
		
		GZIPOutputStream gzos = null;
		byte[] buf = new byte[1024];
		
		try {
			is = new FileInputStream(inputFile);
			gzos = new GZIPOutputStream(new FileOutputStream(outputFile));
			int len = 0;
			while ((len = is.read(buf)) != -1) {
				gzos.write(buf, 0, len);
			}
			is.close();
			gzos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
