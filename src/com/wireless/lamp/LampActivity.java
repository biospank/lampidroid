package com.wireless.lamp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class LampActivity extends Activity implements OnTaskCompleted {
	
	private UdpClientTask cUdp;
	private TextView lblAutoDisovery;
	private Button btnRefresh;
	private ToggleButton tglLamp;


	IntentFilter intentFilter;
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//---display the SMS received in the TextView---
			lblAutoDisovery.setText(intent.getExtras().getString("sms"));
			if(cUdp.getLampiIp() == null)
				new HttpNotifyTask().execute("192.168.1.2");
			else
				new HttpNotifyTask().execute(cUdp.getLampiIp());
		}

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lamp);

		initializeView();
		

		// Kickoff the Client
		Context ctx = this.getApplicationContext();
		WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		if(wifi.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
			startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
		} else {
			cUdp = new UdpClientTask(wifi);
			cUdp.execute();

//			cUdp = new UdpClient(wifi);
//			new Thread(cUdp).start();
			
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			TextView SMSes = (TextView) findViewById(R.id.etName);
//			SMSes.setText(cUdp.getLampiIp());
			
		}
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lamp, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		//registerReceiver(intentReceiver, intentFilter);
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(intentReceiver);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		//unregisterReceiver(intentReceiver);
		super.onPause();
	}

	protected void initializeView() {
		lblAutoDisovery = (TextView) findViewById(R.id.lblAutoDiscovery);
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		tglLamp = (ToggleButton) findViewById(R.id.tglLamp);

		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");
		
		tglLamp.setOnClickListener(new ToggleButton.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(((ToggleButton)v).isChecked()) {
					//Log.d("tglLamp", "Button enabled");
					registerReceiver(intentReceiver, intentFilter);
				} else {
					//Log.d("tglLamp", "Button disabled");
					unregisterReceiver(intentReceiver);
				}
				lblAutoDisovery.setText("");
			}
			
		});
		//this.btnRefresh.setText(getString(R.string.refresh));
	}

	@Override
	public void onTaskCompleted() {
		String ip = cUdp.getLampiIp();
		if(ip == null) {
			Log.d("task completed", "no device found!!");
			lblAutoDisovery.setText("Lamp device not found!!");
		} else {
			Log.d("task completed", "device found!!");
			lblAutoDisovery.setText("raspi ip: " + ip);
		}
		
	}

}
