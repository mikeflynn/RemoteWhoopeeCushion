package io.github.mikeflynn.remotewhoopeecushion;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    public void playFart(View view) {
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
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(body);
        // Create an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // Create the stack for navigating backward from the activity.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // Create the notification
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }
}
