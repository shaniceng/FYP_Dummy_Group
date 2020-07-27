package com.example.fyp_dummy_group;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.sql.Ref;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends WearableActivity implements SensorEventListener {
    private TextView hr, sc;

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView mTextView, currentDay;
    private Calendar calendar;

    private static final String AMBIENT_UPDATE_ACTION = "com.your.package.action.AMBIENT_UPDATE";
    private AlarmManager ambientUpdateAlarmManager;
    private PendingIntent ambientUpdatePendingIntent;
    private BroadcastReceiver ambientUpdateBroadcastReceiver;

    //heart
    private String heart_msg;
    String heartPath = "/heart-rate-path";
    private static final String TAG = "FitActivity";

    //steps
    private SharedPreferences prefs;
    private static final String Initial_Count_Key = "FootStepInitialCount";
    String stepsPath = "/steps-count-path";
    String maxheartpath = "/max-heart-path";
    private Calendar currentTime, time;
    private int currentSteps;
    private String step,step_msg;

    private  SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentDay=findViewById(R.id.tvCurrentTime);
        mTextView = (TextView) findViewById(R.id.textView_);
        currentTime=Calendar.getInstance();
        calendar=Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        currentDay.setText(currentDate);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String time = "Current Time:" + format.format(calendar.getTime());
        mTextView.setText(time);

        hr=findViewById(R.id.textView);
        sc=findViewById(R.id.textView2);

        // Enables Always-on
        setAmbientEnabled();
        Refresh();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        getHartRate();

        ambientUpdateAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent ambientUpdateIntent = new Intent(AMBIENT_UPDATE_ACTION);
        ambientUpdatePendingIntent = PendingIntent.getBroadcast(this, 0, ambientUpdateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ambientUpdateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) { refreshDisplayAndSetNextUpdate(); }
        };
    }
    private void getHartRate() {
       mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        //heartRate
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //suggesting android to take data in every 5s, if nth to do, android will auto collect data.


        //stepsCount
        Sensor mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        time = Calendar.getInstance();
        if ((time.get(Calendar.HOUR_OF_DAY) >= 6) && (time.get(Calendar.HOUR_OF_DAY) < 23)) {
            if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                heart_msg = "" + (int) event.values[0];
                if (heart_msg != null) {
                    hr.setText(heart_msg + "BPM");
                    if (!prefs.contains("getMaxcurrentHeartRate")) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("getMaxcurrentHeartRate", 0);
                        editor.commit();
                    } else if (prefs.getInt("getMaxcurrentHeartRate", -1) < ((int) event.values[0])) {
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putInt("getMaxcurrentHeartRate", (int) event.values[0]);
                        edit.commit();
                    }
                }
            } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                resetSteps();
                prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                // Initialize if it is the first time use
                if (!prefs.contains(Initial_Count_Key) || ((int) event.values[0] == 0)) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(Initial_Count_Key, (int) event.values[0]);
                    editor.commit();
                }


                int startingStepCount = prefs.getInt(Initial_Count_Key, -1);
                int stepCount = (int) event.values[0] - startingStepCount;
                currentSteps = (int) event.values[0];

                step = String.valueOf(stepCount);
                step_msg = "Steps count:\n " + step + " steps";
                sc.setText(step_msg);
                Log.d(TAG, step_msg);

                //display starting steps count to phone app database
            } else
                Log.d(TAG, "Unknown sensor type");
        }else{
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }

    //Send msg from wear to hp at fixed intervals
    private static final long AMBIENT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(60);
    private void refreshDisplayAndSetNextUpdate() {
        if(!prefs.contains("dailyCurrentSteps")){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("dailyCurrentSteps", 0);
            editor.commit();
        }
        if(!prefs.contains("previousHeartRate")){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("previousHeartRate", 0);
            editor.commit();
        }
        if(!prefs.contains("previousStepsCount")){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("previousStepsCount", 0);
            editor.commit();
        }

        if (isAmbient()) {
            // Implement data retrieval and update the screen for ambient mode
            if(heart_msg != null && (String.valueOf(prefs.getInt("previousHeartRate",-1))!=heart_msg.replaceAll("[\\D]",""))) {
                new MainActivity.SendThread(heartPath, heart_msg + "BPM").start();
                new MainActivity.SendThread(maxheartpath, prefs.getInt("getMaxcurrentHeartRate", -1) + "BPM").start();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("previousHeartRate", Integer.parseInt(heart_msg.replaceAll("[\\D]","")));
                editor.commit();
            }
            if((String.valueOf(prefs.getInt("dailyCurrentSteps", -1))!=null) && (prefs.getInt("previousStepsCount",-1))!=prefs.getInt("dailyCurrentSteps", -1)){
                new MainActivity.SendThread(stepsPath, String.valueOf(prefs.getInt("dailyCurrentSteps", -1))).start();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("previousStepsCount", prefs.getInt("dailyCurrentSteps", -1));
                editor.commit();
            }

        } else {
            // Implement data retrieval and update the screen for interactive mode
            if(heart_msg != null && (String.valueOf(prefs.getInt("previousHeartRate",-1))!=heart_msg.replaceAll("[\\D]",""))) {
                new MainActivity.SendThread(heartPath, heart_msg + "BPM").start();
                new MainActivity.SendThread(maxheartpath, prefs.getInt("getMaxcurrentHeartRate", -1) + "BPM").start();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("previousHeartRate", Integer.parseInt(heart_msg.replaceAll("[\\D]","")));
                editor.commit();
            }
        }
        long timeMs = System.currentTimeMillis();
        // Schedule a new alarm
        if (isAmbient()) {
            // Calculate the next trigger time
            long delayMs = AMBIENT_INTERVAL_MS - (timeMs % AMBIENT_INTERVAL_MS);
            long triggerTimeMs = timeMs + delayMs;
            ambientUpdateAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    ambientUpdatePendingIntent);
        } else {
            // Calculate the next trigger time for interactive mode
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        startAlarm();
        getHartRate();
        refreshDisplayAndSetNextUpdate();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        refreshDisplayAndSetNextUpdate();
    }


    protected void onResume() {
        super.onResume();
        startAlarm();
        getHartRate();
        //sensorManager.registerListener(this, this.sensor, 1000);
        IntentFilter filter = new IntentFilter(AMBIENT_UPDATE_ACTION);
        registerReceiver(ambientUpdateBroadcastReceiver, filter);
        refreshDisplayAndSetNextUpdate();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //finish();
        startAlarm();
        getHartRate();
        refreshDisplayAndSetNextUpdate();
        IntentFilter filter = new IntentFilter(AMBIENT_UPDATE_ACTION);
        registerReceiver(ambientUpdateBroadcastReceiver, filter);
//        unregisterReceiver(ambientUpdateBroadcastReceiver);
//        ambientUpdateAlarmManager.cancel(ambientUpdatePendingIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        startAlarm();
        //refreshDisplayAndSetNextUpdate();
        mSensorManager.unregisterListener(this);
        unregisterReceiver(ambientUpdateBroadcastReceiver);
        ambientUpdateAlarmManager.cancel(ambientUpdatePendingIntent);
    }

    class SendThread extends Thread {
        String path;
        String message;

        //constructor
        SendThread(String p, String msg) {
            path = p;
            message = msg;
        }

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        public void run() {
            //first get all the nodes, ie connected wearable devices.
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                List<Node> nodes = Tasks.await(nodeListTask);

                //Now send the message to each device.
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        Integer result = Tasks.await(sendMessageTask);
                        Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                    } catch (ExecutionException exception) {
                        Log.e(TAG, "Task failed: " + exception);

                    } catch (InterruptedException exception) {
                        Log.e(TAG, "Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                Log.e(TAG, "Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Interrupt occurred: " + exception);
            }
        }
    }


    //resetSteps at 12mn
    public void resetSteps(){
        Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar firingCal= Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();

        firingCal.set(Calendar.HOUR, 0); // At the hour you wanna fire
        firingCal.set(Calendar.MINUTE, 0); // Particular minute
        firingCal.set(Calendar.SECOND, 0); // particular second

        long intendedTime = firingCal.getTimeInMillis();
        long currentTime = currentCal.getTimeInMillis();

        if(intendedTime >= currentTime){
            // you can add buffer time too here to ignore some small differences in milliseconds
            // set from today
            alarmManager.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent);
        } else{
            // set from next day
            // you might consider using calendar.add() for adding one day to the current day
            firingCal.add(Calendar.DAY_OF_MONTH, 1);
            intendedTime = firingCal.getTimeInMillis();

            alarmManager.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }


    //Refresh to display time
    public void Refresh(){
        Calendar currentTime = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a");
        String time = "Current Time:" + format.format(currentTime.getTime());
        mTextView.setText(time);
        runnable(1000);
    }

    public void runnable(int milliseconds){
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Refresh();
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    private void startAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        Calendar firingCal= Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();

        firingCal.set(Calendar.HOUR_OF_DAY, 00); // At the hour you wanna fire
        firingCal.set(Calendar.MINUTE, 00); // Particular minute
        firingCal.set(Calendar.SECOND, 00); // particular second

        long intendedTime = firingCal.getTimeInMillis();
        long currentTime = currentCal.getTimeInMillis();

        if(intendedTime >= currentTime){
            // you can add buffer time too here to ignore some small differences in milliseconds
            // set from today
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent);
        } else{
            // set from next day
            // you might consider using calendar.add() for adding one day to the current day
            firingCal.add(Calendar.DAY_OF_MONTH, 1);
            intendedTime = firingCal.getTimeInMillis();

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startAlarm();
    }
}
