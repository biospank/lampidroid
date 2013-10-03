package pi.lamp;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class HttpNotifyTask extends AsyncTask<String, Void, Void> {

	public static final int LAMPI_NOTIFY_ACTION = 1;
	public static final int LAMPI_RESET_ACTION = 2;
	private int lampiAction;
	private final int LAMPI_HTTP_PORT = 4567;
	private final String LAMPI_CLIENT_NAME = "droid";
	
	
	
	public HttpNotifyTask(int type) {
		this.lampiAction = type;
	}



	@Override
	protected Void doInBackground(String... ips) {
		String ip = ips[0];
		
		if(ip != null) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet get;
			
			switch (this.lampiAction) {
			case LAMPI_NOTIFY_ACTION:
				get = new HttpGet("http://" + ip + ":" + LAMPI_HTTP_PORT + "/lamp/" + LAMPI_CLIENT_NAME);
				break;
			case LAMPI_RESET_ACTION:
				get = new HttpGet("http://" + ip + ":" + LAMPI_HTTP_PORT + "/lamp/led/reset");
				break;
			default:
				get = new HttpGet("http://" + ip + ":" + LAMPI_HTTP_PORT + "/lamp/" + LAMPI_CLIENT_NAME);
				break;
			}
	
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
