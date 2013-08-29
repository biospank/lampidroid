package com.wireless.lamp;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

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
        
        return dateformatter.format(date);
        
	}

	public static String getFullFormattedDateFor(long timeAlarm, Context ctx) {
        Date date = new Date(timeAlarm);
        DateFormat dateformatter = android.text.format.DateFormat.getDateFormat(ctx);
        DateFormat timeformatter = android.text.format.DateFormat.getTimeFormat(ctx);

        String strDate = "<small>" + dateformatter.format(date) + "</small> at <br />" + timeformatter.format(date) + "";
        
//        Spannable sText = new SpannableString(strDate);
//        sText.setSpan(new ForegroundColorSpan(Color.BLUE), 5, 9, 0);

        Spanned sText = Html.fromHtml(strDate);
        return sText.toString();
        
	}

	public static String getFormattedTimeFor(long timeAlarm, Context ctx) {
        Date date = new Date(timeAlarm);
        DateFormat timeformatter = android.text.format.DateFormat.getTimeFormat(ctx);

        return timeformatter.format(date);
        
	}

}
