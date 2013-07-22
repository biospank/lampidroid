package com.wireless.lamp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class UdpClient implements Runnable {

	private static final String TAG = "Discovery";
	private static final int SERVER_PORT = 1234;
	private static final int TIMEOUT_MS = 500;
	private WifiManager mWifi;
	private String lampiIp;

  interface UdpClientReceiver {
    void addAnnouncedServers(InetAddress[] host, int port[]);
  }

  UdpClient(WifiManager wifi) {
    mWifi = wifi;
  }

	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(SERVER_PORT);
			socket.setBroadcast(true);
			socket.setSoTimeout(TIMEOUT_MS);

			sendDiscoveryRequest(socket);
			listenForResponses(socket);
		} catch (IOException e) {
			Log.e(TAG, "Could not send discovery request", e);
		}
	}

	/**
	 * Send a broadcast UDP packet containing a request for boxee services to
	 * announce themselves.
	 * 
	 * @throws IOException
	 */
	private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
		// String data = String
		// .format(
		// "<bdp1 cmd=\"discover\" application=\"iphone_remote\" challenge=\"%s\" signature=\"%s\"/>",
		// mChallenge, getSignature(mChallenge));
		// Log.d(TAG, "Sending data " + data);

		String data = new String("\004\b\"\nHello");

		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
				getBroadcastAddress(), SERVER_PORT);
		socket.send(packet);
	}

	/**
	 * Calculate the broadcast IP we need to send the packet along. If we send it
	 * to 255.255.255.255, it never gets sent. I guess this has something to do
	 * with the mobile network not wanting to do broadcast.
	 */
	private InetAddress getBroadcastAddress() throws IOException {
		DhcpInfo dhcp = mWifi.getDhcpInfo();
		if (dhcp == null) {
			Log.d(TAG, "Could not get dhcp info");
			return null;
		}

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
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
				String s = new String(packet.getData(), 0, packet.getLength());
				Log.d(TAG, "Received response " + s);
			}
		} catch (SocketTimeoutException e) {
			Log.d(TAG, "Receive timed out");
		}
	}

	public void setLampiIp(String lampiIp) {
		this.lampiIp = lampiIp;
	}

	public String getLampiIp() {
		return this.lampiIp;
		
	}
}
