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

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.w("methodCalled", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent != null) {
            handleIntent(intent);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        // Set delay notice
        String delayNotice = "";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String delay = prefs.getString("io.github.mikeflynn.remotewhoopeecushion.fart_delay", "0");
        if(!delay.equals("0")) {
            delayNotice = delay + " SECOND DELAY";
        }

        TextView t = (TextView)findViewById(R.id.delay_notice);
        t.setText(delayNotice);
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
            if (methodName.equals("playFart")) {
                playFart();
            }
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

    public void startFart(View view) {
        playFart();
    }

    public void playFart() {
        //Log.w("whatsHappening", "Playing fart!");
        // Pull the user's preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String wavName = prefs.getString("io.github.mikeflynn.remotewhoopeecushion.fart_type", "chipotle");
        final int wavDelay = Integer.valueOf(prefs.getString("io.github.mikeflynn.remotewhoopeecushion.fart_delay", "0"));
        final boolean wavNotify = prefs.getBoolean("io.github.mikeflynn.remotewhoopeecushion.fart_notify", false);

        int wavId = getResources().getIdentifier("raw/"+wavName, null, this.getPackageName());
        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), wavId);

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
                        mp.start();
                        if(wavNotify) {
                            triggerNotification("Just Farted.", "Was it funny? Yes.");
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

