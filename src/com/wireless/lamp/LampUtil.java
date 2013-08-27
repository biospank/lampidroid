package com.wireless.lamp;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

public class LampUtil {
	public static long getTimeAlarmFor(long timeSet) {
    	long currentTime = System.currentTimeMillis();
		long timeAlarm;
		if(currentTime >= timeSet){
			Calendar c = Calendar.getInstance();
			if(timeSet > 0) {
    			c.setTimeInMillis(timeSet);
			} else {
    			c.setTimeInMillis(currentTime);
			}
			c.add(Calendar.DATE, 1);
			timeAlarm = c.getTimeInMillis();
		} else {
			timeAlarm = timeSet; 
		}

		return timeAlarm;
	}

	public static String getFormattedDateFor(long timeAlarm, Context ctx) {
        Date date = new Date(timeAlarm);
        DateFormat dateformatter = android.text.format.DateFormat.getDateFormat(ctx);
        DateFormat timeformatter = android.text.format.DateFormat.getTimeFormat(ctx);

        StringBuilder strDate = new StringBuilder();
        strDate.append(dateformatter.format(date));
        strDate.append(" at " + timeformatter.format(date));
        
        return strDate.toString();
        
	}

}
