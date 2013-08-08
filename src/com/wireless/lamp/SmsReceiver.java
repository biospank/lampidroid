package com.wireless.lamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String sms = "Message received!!";
		Toast.makeText(context, sms, Toast.LENGTH_SHORT).show();
		
		Intent lampActivityIntent = new Intent(context, LampActivity.class);
		lampActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(lampActivityIntent);
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(LampActivity.SMS_LAMP_ACTION);
		broadcastIntent.putExtra("sms", sms);
		context.sendBroadcast(broadcastIntent);
		
	}
}