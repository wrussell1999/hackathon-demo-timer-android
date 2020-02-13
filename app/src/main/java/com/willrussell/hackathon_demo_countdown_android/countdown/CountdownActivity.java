package com.willrussell.hackathon_demo_countdown_android.countdown;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.willrussell.hackathon_demo_countdown_android.R;
import com.willrussell.hackathon_demo_countdown_android.AboutActivity;
import com.willrussell.hackathon_demo_countdown_android.time.TimeActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CountdownActivity extends AppCompatActivity {

    private final String TAG = "CountdownActivity";
    protected TextView countdownTimeView;
    protected View decorView;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("countdown");
    private Thread countdownThread;
    private Time time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        countdownTimeView = findViewById(R.id.time);
        decorView = getWindow().getDecorView();


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Time value = dataSnapshot.getValue(Time.class);
                Log.d(TAG, "Value is: " + value);

                if (time == null || !time.equals(value)){
                    time = value;
                    CountdownThread countdown = new CountdownThread(time);

                    if (countdownThread == null){
                        Log.w(TAG, "creating first countdown");
                        task.execute();
                        //countdownThread = new Thread(countdown);
                        //countdownThread.start();
                    } else {
                        Log.w(TAG, "Interupting existing countdown");
                        //countdownThread.interrupt();
                        task.cancel(true);
                        try {
                            //countdownThread.join();
                        } catch (Exception ignored) {}
                        Log.w(TAG, "Creating new countdown");
                        //countdownThread = new Thread(countdown);
                        //countdownThread.start();
                        task.execute();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    AsyncTask task = new AsyncTask() {
        @Override
        protected Boolean doInBackground(Object[] objects) {


            return true;
        }
    };

    class CountdownThread implements Runnable {
        private Time time;
        private Date endTime;
        protected String countdown;
        private boolean flash;
        private boolean finish;

        public CountdownThread(Time time) {
            this.time = time;
            this.countdown = "";
            this.flash = false;
            this.finish = false;
            if (this.time.getStart()){
                Calendar date = Calendar.getInstance();
                date.add(Calendar.MINUTE, this.time.getTime());
                this.endTime = date.getTime();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                countdown = getTime();
                runOnUiThread(() -> countdownTimeView.setText(countdown));

                /*
                if (flash) {
                    runOnUiThread(() -> {
                        countdownTimeView.setTextColor(getResources().getColor(android.R.color.white));
                        decorView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    });
                } else {
                    runOnUiThread(() -> {
                        countdownTimeView.setTextColor(getResources().getColor(android.R.color.black));
                        decorView.setBackgroundColor(getResources().getColor(android.R.color.background_light));
                    });
                }
                 */

                if (finish) {
                    break;
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public String getTime() {
            String timeFormatted;
            long minutes;
            long seconds;
            if (this.time.getStart()) {

                long timeLeft = (this.endTime.getTime() - (new Date()).getTime()) / 1000;
                minutes = timeLeft / 60;
                seconds = timeLeft % 60;
            } else {
                minutes = this.time.getTime();
                seconds = 0;
            }
            if (minutes == 0 && seconds <= 10 && seconds % 2 == 0) {
                this.flash = true;
            } else {
                this.flash = false;
            }

            if (minutes == 0 && seconds < 0 && seconds > -10) {
                timeFormatted = "0:00";
            } else if (minutes == 0 && seconds <= -10) {
                this.finish = true;
                this.flash = false;
                timeFormatted = "0:00";
            } else {
                timeFormatted = String.format(Locale.ENGLISH,"%d:%02d", (int) minutes, (int) seconds);
            }

            return timeFormatted;
        }
    }
}
