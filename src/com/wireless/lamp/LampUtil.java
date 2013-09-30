package com.wireless.lamp;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

public class LampUtil {
	public static long getTimeAlarmFor(long timeSet) {
    	long currentTime = System.currentTimeMillis();
		long timeAlarm;
		if(currentTime >= timeSet){
			Calendar c = Calendar.getInstance();
//			if(timeSet > 0) {
//    			c.setTimeInMillis(timeSet);
//			} else {
//    			c.setTimeInMillis(currentTime);
//			}
			c.setTimeInMillis(currentTime);
			c.add(Calendar.DATE, 1);
			timeAlarm = c.getTimeInMillis();
		} else {
			timeAlarm = timeSet; 
		}

		return timeAlarm;
	}

	public static Calendar getCalendarFor(long timeSet) {
    	long currentTime = System.currentTimeMillis();
		Calendar c = Calendar.getInstance();
		if(currentTime >= timeSet){
//			if(timeSet > 0) {
//    			c.setTimeInMillis(timeSet);
//			} else {
//    			c.setTimeInMillis(currentTime);
//			}
			c.setTimeInMillis(currentTime);
			c.add(Calendar.DATE, 1);
		} else {
			c.setTimeInMillis(timeSet);
		}

		return c;
	}

	public static String getFormattedDateFor(long timeAlarm, Context ctx) {
        Date date = new Date(timeAlarm);
        DateFormat dateformatter = android.text.format.DateFormat.getDateFormat(ctx);
        
        return dateformatter.format(date);
        
	}

	public static String getFullFormattedDateFor(long timeAlarm, Context ctx) {
        Date date = new Date(timeAlarm);
        DateFormat dateformatter = android.text.format.DateFormat.getLongDateFormat(ctx);
        DateFormat timeformatter = android.text.format.DateFormat.getTimeFormat(ctx);

        
        String strDate = timeformatter.format(date)  + " " + dateformatter.format(date);
        
        return strDate;
        
	}

	public static String getFormattedTimeFor(long timeAlarm, Context ctx) {
        Date date = new Date(timeAlarm);
        DateFormat timeformatter = android.text.format.DateFormat.getTimeFormat(ctx);

        return timeformatter.format(date);
        
	}

	public static Spannable getAlarmSummarySpanText(String text) {
		
        Spannable sText = new SpannableString(text);
        sText.setSpan(new RelativeSizeSpan(0.6f), 6, text.length(), 0); 
        sText.setSpan(new ForegroundColorSpan(Color.rgb(0, 25, 42)), 8, text.length(), 0);
        sText.setSpan(new ForegroundColorSpan(Color.rgb(230, 254, 178)), (text.length() - 4), text.length(), 0);

//        String strDate = "<small>" + dateformatter.format(date) + "</small> at <br />" + timeformatter.format(date);
//        Spanned sText = Html.fromHtml(strDate);
        
        return sText;
        
	}
	
	public static Spannable getSmsSummarySpanText(String text) {
		
        Spannable sText = new SpannableString(text);
        sText.setSpan(new RelativeSizeSpan(0.8f), 0, 10, 0); 
        sText.setSpan(new ForegroundColorSpan(Color.rgb(0, 25, 42)), 0, 10, 0);
        sText.setSpan(new RelativeSizeSpan(1.2f), 10, text.length(), 0); 

//        String strDate = "<small>" + dateformatter.format(date) + "</small> at <br />" + timeformatter.format(date);
//        Spanned sText = Html.fromHtml(strDate);
        
        return sText;
        
	}
}
