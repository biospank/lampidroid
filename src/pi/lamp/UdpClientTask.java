package pi.lamp;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class UdpClientTask extends AsyncTask<Void, Void, HashMap<String, String>> {

	private static final String TAG = "Discovery";
	private static final String UDP_STRING_REQUEST = "reply_port=12345";
	private static final int SERVER_PORT = 1234;
	private static final int TIMEOUT_REQUEST = 1000;
	private static final int TIMEOUT_RESPONSE = 3000;
	private WifiManager mWifi;
	private String lampiIp;
	private boolean wifiEnabled;
	private OnTaskListener listener;
	private DatagramSocket socket;
	private DatagramSocket rcvsocket;
	private HashMap<String, String> bcInterfaces = new HashMap<String, String>();

	UdpClientTask(WifiManager wifi, OnTaskListener listener, boolean wifiEnabled) {
	  mWifi = wifi;
	  this.listener = listener;
	  this.wifiEnabled = wifiEnabled;
	}

	@Override
	protected HashMap<String, String> doInBackground(Void... arg0) {
		try {
			socket = new DatagramSocket(SERVER_PORT);
			rcvsocket = new DatagramSocket(12345);
			rcvsocket.setSoTimeout(TIMEOUT_RESPONSE);
			socket.setBroadcast(true);
			socket.setSoTimeout(TIMEOUT_REQUEST);

			sendDiscoveryRequest(socket);
			listenForResponses(rcvsocket);
		} catch (BindException be) {
			//Log.e(TAG, "Could not send discovery request", be);
		} catch (IOException e) {
			//Log.e(TAG, "Could not send discovery request", e);
		} catch (RuntimeException re) {
			//Log.e(TAG, "Could not send discovery request", re);
		} finally {
			if(socket != null)
				socket.close();
			if(rcvsocket != null)
				rcvsocket.close();
		}
		return bcInterfaces;
	}
	
	protected void onPreExecute() {
		this.listener.onTaskBegin();
	}

	protected void onPostExecute(HashMap<String, String> result) {
//		TextView tvFooter = (TextView) ((LampActivity)this.listener).findViewById(R.id.tvFooter);
//		tvFooter.setText("Broadcast addresses\n");
//		for (String key : result.keySet()) {
//			tvFooter.append(key + ": " + result.get(key) + "\n");
//		}

		this.listener.onTaskCompleted();
	}

	/**
	 * Send a broadcast UDP packet containing a request for boxee services to
	 * announce themselves.
	 * 
	 * @throws IOException
	 */
	private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
		String data = new String(UDP_STRING_REQUEST);
		
		ArrayList<InetAddress> foundBcastAddresses = null;
		
		foundBcastAddresses = getBroadcastAddresses();
		
		for (InetAddress bcAddress : foundBcastAddresses) {
			DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
					bcAddress, SERVER_PORT);
			socket.send(packet);
		}

	}

	/**
	 * Calculate the broadcast IP we need to send the packet along. If we send it
	 * to 255.255.255.255, it never gets sent. I guess this has something to do
	 * with the mobile network not wanting to do broadcast.
	 */
	private ArrayList<InetAddress> getBroadcastAddresses() throws SocketException {
		ArrayList<InetAddress> foundBcastAddresses = new ArrayList<InetAddress>();
		System.setProperty("java.net.preferIPv4Stack", "true");
		Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces();
		while (niEnum.hasMoreElements()) {
			NetworkInterface ni = niEnum.nextElement();
			if (!ni.isLoopback()) {
				for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {

					InetAddress ia = interfaceAddress.getBroadcast();
					
					if(ia != null) {
						foundBcastAddresses.add(interfaceAddress.getBroadcast());
						bcInterfaces.put(ni.getDisplayName() + " - " + ni.getName(), 
								interfaceAddress.getBroadcast().toString());
						
					}

				}
			}
		}

		return foundBcastAddresses;
	}
	
	/**
	 * Listen on socket for responses, timing out after TIMEOUT_MS
	 * 
	 * @param socket
	 *          socket on which the announcement request was sent
	 * @throws IOException
	 */
	private void listenForResponses(DatagramSocket socket) throws IOException {
		byte[] buf = new byte[1024];
		try {
			while (true) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String s = packet.getAddress().getHostAddress(); //new String(packet.getData(), 0, packet.getLength());
				//Log.d(TAG, "Received response " + s);
				setLampiIp(s);
				
			}
		} catch (SocketTimeoutException e) {
			//Log.d(TAG, "Receive timed out");
		}
	}

	public void setLampiIp(String lampiIp) {
		this.lampiIp = lampiIp;
	}

	public String getLampiIp() {
		return this.lampiIp;
		
	}

	
}
