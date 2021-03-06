package io.github.mikeflynn.remotewhoopeecushion;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
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

import java.io.File;
import java.io.IOException;
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

        // Set the volume controls to control media playback volume rather than the ringer.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Set the long press handler on the fart button
        findViewById(R.id.card_start).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isFarting = true;

                final TextView btnText = (TextView) findViewById(R.id.card_start_text);
                btnText.setText("Easter Egg!");

                try {
                    int eggId = getResources().getIdentifier("stepbrothers_fart", "raw", getApplicationContext().getPackageName());
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), eggId);
                    //mp.prepare();
                    mp.setVolume(1, 1);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            isFarting = false;
                            btnText.setText(R.string.fart_button);
                        }
                    });

                    YoYo.with(Techniques.Tada)
                            .duration(1000)
                            .playOn(findViewById(R.id.card_start));

                    mp.start();
                } catch (Exception e) {
                    isFarting = false;
                    btnText.setText(R.string.fart_button);
                    return false;
                }

                return true;
            }
        });
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
            case R.id.action_recordings:
                openRecordings();
                return true;
            case R.id.action_farts:
                openFarts();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void isFarting(boolean flag) {
        isFarting = flag;
    }

    protected void resetFartButton() {
        // Kill the recording throbber animation.
        recordBtnThrobber.cancel();

        // Set background color
        View btn = findViewById(R.id.card_bg);
        btn.setBackgroundColor(getResources().getColor(R.color.green));

        // Set the button text
        TextView btnText = (TextView)findViewById(R.id.card_start_text);
        btnText.setText(R.string.fart_button);

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

        TextView t = (TextView)findViewById(R.id.delay_notice);
        t.setText(delayNotice);
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

    protected MediaPlayer getPlayer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String name = prefs.getString("io.github.mikeflynn.remotewhoopeecushion.fart_type", "chipotle");

        if(name.equals("random")) {
            ArrayList<String> options = FartsActivity.getAllFarts(getApplicationContext());
            int idx = new Random().nextInt(options.size());
            name = options.get(idx);
        }

        int resourceId = getResources().getIdentifier("raw/"+name, null, this.getPackageName());
        if(resourceId > 0) {
            return MediaPlayer.create(getApplicationContext(), resourceId);
        } else {
            Recording mediaFile = new Recording(name, getApplicationContext(), "fart_");
            if(mediaFile.fileExists()) {
                return mediaFile.getPlayer();
            }

            // Default fart
            return MediaPlayer.create(getApplicationContext(), getResources().getIdentifier("raw/chipotle", null, this.getPackageName()));
        }
    }

    public void playFart() {
        isFarting(true);

        // Pull the user's preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final int wavDelay = getDelay();
        final boolean wavNotify = prefs.getBoolean("io.github.mikeflynn.remotewhoopeecushion.fart_notify", false);
        final boolean doRecord = prefs.getBoolean("io.github.mikeflynn.remotewhoopeecushion.record", false);

        final MediaPlayer mp = getPlayer();
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

                            int ts = (int)(System.currentTimeMillis()/1000L);
                            final Recording rec = new Recording(ts+"", getApplicationContext());
                            rec.startRecording();
                            stopRecording(rec, 10);
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

    protected void stopRecording(final Recording rec, final int delay) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay * 1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rec.stopRecording();
                        isFarting(false);
                        resetFartButton();
                    }
                });
            }
        }).start();
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openRecordings() {
        Intent intent = new Intent(this, RecordingsActivity.class);
        startActivity(intent);
    }

    public void openFarts() {
        Intent intent = new Intent(this, FartsActivity.class);
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

