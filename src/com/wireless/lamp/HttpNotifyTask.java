package com.wireless.lamp;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class HttpNotifyTask extends AsyncTask<String, Void, Void> {

	private final int LAMPI_HTTP_PORT = 4567;
	private final String LAMPI_CLIENT_NAME = "lampidroid";
	
	
	
	@Override
	protected Void doInBackground(String... ips) {
		String ip = ips[0];
		
		if(ip != null) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://" + ip + ":" + LAMPI_HTTP_PORT + "/" + LAMPI_CLIENT_NAME);
	
			try {
				httpclient.execute(get);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return null;
	}
}
