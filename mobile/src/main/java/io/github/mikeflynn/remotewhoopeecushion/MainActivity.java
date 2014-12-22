package io.github.mikeflynn.remotewhoopeecushion;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends Activity {
    private static Boolean isFarting = false;
    private ObjectAnimator recordBtnThrobber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.w("methodCalled", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent != null) {
            handleIntent(intent);
        }

        if(this.recordBtnThrobber == null) {
            this.recordBtnThrobber = ObjectAnimator.ofObject(findViewById(R.id.card_bg), "backgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.recording_start), getResources().getColor(R.color.recording_end));
            this.recordBtnThrobber.setDuration(750);
            this.recordBtnThrobber.setRepeatCount(ValueAnimator.INFINITE);
            this.recordBtnThrobber.setRepeatMode(ValueAnimator.REVERSE);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        resetFartButton();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //Log.w("methodCalled", "onNewIntent");
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        String methodName = intent.getStringExtra("methodName");
        if(methodName != null) {
            if (methodName.equals("playFart") && !isFarting) {
                playFart();
            }

            getIntent().removeExtra("methodName");
        }

        //if (methodName == null) {
        //    methodName = "NULL";
        //}
        //Log.w("intentMethodName", methodName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch(item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void isFarting(boolean flag) {
        isFarting = flag;
    }

    public void tempFn(View view) {
        resetFartButton();
    }

    protected void resetFartButton() {
Log.w("MAIN", "resetFartButton() START");
        // Kill the recording throbber animation.
        recordBtnThrobber.cancel();
Log.w("MAIN", "Throbber killed.");
        // Set background color
        View btn = findViewById(R.id.card_bg);
        btn.setBackgroundColor(getResources().getColor(R.color.green));
        Log.w("MAIN", "BG reset");
        // Set the button text
        TextView btnText = (TextView)findViewById(R.id.card_start_text);
        btnText.setText(R.string.fart_button);
        Log.w("MAIN", "Text reset.");
        // Set delay notice
        String delayNotice = "";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String delay = prefs.getString("io.github.mikeflynn.remotewhoopeecushion.fart_delay", "0");
        if(!delay.equals("0")) {
            if(delay.equals("-1")) {
                delayNotice = "RANDOM DELAY";
            } else {
                delayNotice = delay + " SECOND DELAY";
            }
        }
        Log.w("MAIN", "Delay notice" + delayNotice);
        TextView t = (TextView)findViewById(R.id.delay_notice);
        t.setText(delayNotice);

        Log.w("MAIN", "resetFartButton() END");
    }

    protected void recordingFartButton() {
        TextView btnText = (TextView)findViewById(R.id.card_start_text);
        btnText.setText(R.string.fart_button_recording);

        this.recordBtnThrobber.start();
    }

    public void startFart(View view) {
        if(!isFarting) {
            playFart();
        }
    }

    protected int getDelay() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int delay = Integer.valueOf(prefs.getString("io.github.mikeflynn.remotewhoopeecushion.fart_delay", "0"));
        if(delay == -1) {
            Random r = new Random();
            delay = r.nextInt(10 - 1) + 1;
        }

        return delay;
    }

    protected String getFile() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String name = prefs.getString("io.github.mikeflynn.remotewhoopeecushion.fart_type", "chipotle");

        if(name.equals("-1")) {
            ArrayList<String> options = new ArrayList<String>();
            Collections.addAll(options, getResources().getStringArray(R.array.settings_vals_fart_type));
            options.remove(options.size() - 1);

            int idx = new Random().nextInt(options.size());
            name = options.get(idx);
        }

        return name;
    }

    public void playFart() {
        isFarting(true);

        // Pull the user's preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String wavName = getFile();
        final int wavDelay = getDelay();
        final boolean wavNotify = prefs.getBoolean("io.github.mikeflynn.remotewhoopeecushion.fart_notify", false);
        final boolean doRecord = prefs.getBoolean("io.github.mikeflynn.remotewhoopeecushion.record", false);

        int wavId = getResources().getIdentifier("raw/"+wavName, null, this.getPackageName());
        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), wavId);
        mp.setVolume(1, 1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(wavDelay * 1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Start the recording?
                        if(doRecord) {
                            // Update the button.
                            recordingFartButton();

                            Long tsLong = System.currentTimeMillis()/1000;
                            String ts = tsLong.toString();
                            final Recording rec = new Recording(ts, getApplicationContext());
                            rec.startRecording();
                            rec.stopRecording(5, new Recording.stopRecordingCallback() {
                                public void onStopRecording() {
                                    isFarting(false);
                                    resetFartButton();
                                }
                            });
                        }

                        // Play the fart
                        mp.start();

                        // Display notification?
                        if(wavNotify) {
                            triggerNotification("Just Farted.", "Was it funny? Yes.");
                        }

                        // Shake button
                        YoYo.with(Techniques.Wobble)
                                .duration(750)
                                .playOn(findViewById(R.id.card_start));

                        if(!doRecord) {
                            isFarting(false);
                        }
                    }
                });
            }
        }).start();
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    protected void triggerNotification(String title, String body) {
        // Create an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // Create the stack for navigating backward from the activity.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(viewPendingIntent);

        // Create the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(0, mBuilder.build());
    }
}

