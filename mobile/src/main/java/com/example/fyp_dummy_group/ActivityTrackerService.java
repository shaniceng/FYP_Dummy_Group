package com.example.fyp_dummy_group;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ActivityTrackerService extends WearableListenerService {
    String TAG = "mobile Listener";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {


         if (messageEvent.getPath().equals("/steps-count-path")) {
            final String message = new String(messageEvent.getData());
            Log.v(TAG, "Message path received on phone is: " + messageEvent.getPath());
            Log.v(TAG, "Message received on phone is: " + message);

            // Broadcast message to MainActivity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("countSteps", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else if (messageEvent.getPath().equals("/heart-rate-path")) {
            final String message = new String(messageEvent.getData());
            Log.v(TAG, "Message path received on phone is: " + messageEvent.getPath());
            Log.v(TAG, "Message received on phone is: " + message);

            // Broadcast message to MainActivity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("heartRate", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
         else if (messageEvent.getPath().equals("/max-heart-path")) {
             final String message = new String(messageEvent.getData());
             Log.v(TAG, "Message path received on phone is: " + messageEvent.getPath());
             Log.v(TAG, "Message received on phone is: " + message);

             // Broadcast message to MainActivity for display
             Intent messageIntent = new Intent();
             messageIntent.setAction(Intent.ACTION_SEND);
             messageIntent.putExtra("max_heartyRate", message);
             LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
         }

        else {
            super.onMessageReceived(messageEvent);
        }

    }

}