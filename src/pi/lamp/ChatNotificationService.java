package pi.lamp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class ChatNotificationService extends AccessibilityService {
	
//	private OnChatServiceListener listener;
//
//	public ChatNotificationService(OnChatServiceListener listener) {
//		  this.listener = listener;
//		
//	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent evt) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(LampActivity.CHAT_LAMP_ACTION);
		broadcastIntent.putExtra("chat", evt);
		getApplicationContext().sendBroadcast(broadcastIntent);
		
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceConnected() {
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
	    // Set the type of events that this service wants to listen to.  Others
	    // won't be passed to this service.
	    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED; // AccessibilityEvent.TYPE_VIEW_CLICKED | AccessibilityEvent.TYPE_VIEW_FOCUSED;  

	    // If you only want this service to work with specific applications, set their
	    // package names here.  Otherwise, when the service is activated, it will listen
	    // to events from all applications.
	    
	    // tango = com.sgiggle.production
	    // wechat = com.tencent.mm
	    // line = jp.naver.line.android
	    
	    info.packageNames = new String[] {
    		"com.whatsapp", 
    		"com.skype.rider", 
    		"com.viber.voip", 
    		"com.sgiggle.production", 
    		"com.tencent.mm", 
    		"jp.naver.line.android", 
    		"com.facebook.orca", 
    		"com.oovoo",
    		"com.vpho",
    		"com.twitter.android",
    		"com.camshare.camfrog.android"
	    };

	    // Set the type of feedback your service will provide.
	    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;

	    // Default services are invoked only if no package-specific ones are present
	    // for the type of AccessibilityEvent generated.  This service *is*
	    // application-specific, so the flag isn't necessary.  If this was a
	    // general-purpose service, it would be worth considering setting the
	    // DEFAULT flag.

	    // info.flags = AccessibilityServiceInfo.DEFAULT;

	    info.notificationTimeout = 100;

	    this.setServiceInfo(info);

	}
	
}
