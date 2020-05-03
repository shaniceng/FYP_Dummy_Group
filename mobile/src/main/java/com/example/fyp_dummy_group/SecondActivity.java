package com.example.fyp_dummy_group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SecondActivity extends AppCompatActivity {
    Button logout;
    private int currentHeartRate, MaxHeartRate, currentStepsCount;
    private TextView tv3, tv4 , tv5, tv6;

    private String heart, steps, message, max_HeartRate;

    private static final String TAG = "LineChartActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference, stepsDataBaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        firebaseAuth=FirebaseAuth.getInstance();

        tv3=findViewById(R.id.textView3);
        tv4=findViewById(R.id.textView4);
        tv5=findViewById(R.id.textView5);
        tv6=findViewById(R.id.textView6);

        logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(SecondActivity.this, MainActivity.class));
            }
        });

        firebaseDatabase= FirebaseDatabase.getInstance();
        firebaseAuth= FirebaseAuth.getInstance();
        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String date = dateFormat.format(currentDate.getTime()).replaceAll("[\\D]","");
        databaseReference = firebaseDatabase.getReference("Chart Values/" + currentuser +"/" + date);
        stepsDataBaseRef=firebaseDatabase.getReference("Steps Count/" +currentuser + "/" + date );


        //get Max heart rate for each individual age
        DatabaseReference mydatabaseRef = firebaseDatabase.getReference("Users/" + firebaseAuth.getUid());
        mydatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                MaxHeartRate = 220 - Integer.parseInt(userProfile.getUserAge().replaceAll("[\\D]",""));
                tv4.setText(String.valueOf(MaxHeartRate));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SecondActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });


        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(SecondActivity.this).registerReceiver(messageReceiver, messageFilter);

        retrieveStepsData();
        retrieveData();

    }

    private void insertData() {
        String id = databaseReference.push().getKey();
        Calendar currentTime = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        int x = Integer.parseInt(format.format(currentTime.getTime()).replaceAll("[\\D]",""));
        int y = currentHeartRate;
        PointValue pointValue = new PointValue(x,y);
        databaseReference.child(id).setValue(pointValue);

        retrieveData();
    }

    private void retrieveData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()){
                        PointValue pointValue = myDataSnapshot.getValue(PointValue.class);
                        tv3.setText(String.valueOf(pointValue.getyValue()));

                    }
                }else{
                    Toast.makeText(SecondActivity.this, "Error in retrieving heart rate", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void insertStepsData() {
        String id = stepsDataBaseRef.push().getKey();
        StepsPointValue pointSteps = new StepsPointValue(currentStepsCount);
        stepsDataBaseRef.child(id).setValue(pointSteps);

        retrieveStepsData();
    }

    private void retrieveStepsData() {
        stepsDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()){
                        StepsPointValue stepsPointValue = myDataSnapshot.getValue(StepsPointValue.class);
                        tv5.setText(String.valueOf(stepsPointValue.getSteps()));
                    }
                }else{
                    Toast.makeText(SecondActivity.this,"Error in retrieving steps", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


     public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getStringExtra("heartRate")!=null){
                heart = intent.getStringExtra("heartRate");
                Log.v(TAG, "Main activity received message: " + message);
                tv6.setText(heart);
                currentHeartRate=Integer.parseInt(heart.replaceAll("[\\D]",""));
                //ratedMaxHR.setText(String.valueOf(currentHeartRate));
                insertData();
            }
            else if(intent.getStringExtra("countSteps")!=null){
                steps = intent.getStringExtra("countSteps");
                Log.v(TAG, "Main activity received message: " + message);

                currentStepsCount = Integer.parseInt(steps);
                insertStepsData();

            }
        }
    }
}
