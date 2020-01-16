package com.willrussell.hackathon_demo_timer_android;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CountdownFragment extends Fragment {

    private final String SERVER_URL = "https://2c0e0446.ngrok.io"; // Must be https
    private TextView countdownView;
    private View background;
    private Handler handler;

    public static CountdownFragment newInstance() {
        CountdownFragment fragment = new CountdownFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.countdown_fragment, container, false);
        countdownView = view.findViewById(R.id.time);
        background = view;
        handler = new Handler();

        RequestQueue queue = Volley.newRequestQueue(view.getContext());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, SERVER_URL,
                response -> {
                    try {


                        JSONObject obj = new JSONObject(response);
                        CountdownThread countdownThread = new CountdownThread(obj.getString("time"));
                        new Thread(countdownThread).start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        return view;
    }

    class CountdownThread implements Runnable {
        private String endTime;
        private String countdown;
        private boolean flash;
        private boolean finish;

        public CountdownThread(String endTime) {
            this.endTime = endTime;
            this.countdown = "";
            this.flash = false;
            this.finish = false;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    countdown = getTime();
                    handler.post(new UIThread(this.countdown, this.flash));
                    if (finish) {
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        public String getTime() throws ParseException {
            Date dateNow = new Date();
            SimpleDateFormat formatterEnd = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
            Date dateEnd = formatterEnd.parse(endTime);

            long timeLeft = (dateEnd.getTime() - dateNow.getTime()) / 1000;
            String timeFormatted;
            long minutes = timeLeft / 60;
            long seconds = timeLeft % 60;

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
                timeFormatted = String.format("%d:%02d", (int) minutes, (int) seconds);
            }

            return timeFormatted;
        }
    }

    class UIThread implements Runnable {
        private String output;
        private boolean flash;

        public UIThread(String output, Boolean flash) {
            this.output = output;
            this.flash = flash;
        }

        @Override
        public void run() {
            countdownView.setText(output);
            if (flash) {
                //background.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                //background.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
        }
    }
}
