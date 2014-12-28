package io.github.mikeflynn.remotewhoopeecushion;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.support.v4.view.GestureDetectorCompat;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.Collections;


public class FartsActivity extends Activity implements RecyclerView.OnItemTouchListener,
                                                       GestureDetector.OnGestureListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private GestureDetectorCompat mDetector;

    private ArrayList<Recording> allRecordings;

    static private boolean isPlaying = false;

    static private boolean isRecording = false;
    static private Recording activeRecording;
    private ObjectAnimator recordBtnThrobber = null;

    static public ArrayList<String> getAllFarts(Context ctx) {
        ArrayList<String> farts = new ArrayList<String>();

        // Resources
        farts.add("beans");
        farts.add("chipotle");
        farts.add("grunter");
        farts.add("moist");
        farts.add("muffled");
        farts.add("quick");
        farts.add("random");

        // Custom recordings
        ArrayList<Recording> recordings = Recording.getList(ctx, "fart_");
        for (int i = 0; i < recordings.size(); i++) {
            farts.add(recordings.get(i).getFilename());
        }

        return farts;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_farts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch(item.getItemId()) {
            case R.id.farts_help:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage(R.string.farts_help_message)
                        .setTitle(R.string.farts_help_title);
                alert.setPositiveButton(R.string.farts_help_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farts);

        // Get the file list
        allRecordings = Recording.getList(getApplicationContext(), "fart_");
        Collections.reverse(allRecordings);
        displayEmptyMsg();

        // Set up the list view
        mRecyclerView = (RecyclerView) findViewById(R.id.recordingsList);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RecordingsAdapter(allRecordings);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Handle touch events
        mDetector = new GestureDetectorCompat(getApplicationContext(), new RecyclerViewOnGestureListener());

        mRecyclerView.addOnItemTouchListener(this);

        // Create the record button animation
        if(this.recordBtnThrobber == null) {
            CardView btn = (CardView) findViewById(R.id.newCustomFart);
            this.recordBtnThrobber = ObjectAnimator.ofObject(btn, "cardBackgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.recording_start), getResources().getColor(R.color.recording_end));
            this.recordBtnThrobber.setDuration(750);
            this.recordBtnThrobber.setRepeatCount(ValueAnimator.INFINITE);
            this.recordBtnThrobber.setRepeatMode(ValueAnimator.REVERSE);
        }
    }

    protected void displayEmptyMsg() {
        if(allRecordings.size() > 0) {
            findViewById(R.id.noFartsMsg).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.noFartsMsg).setVisibility(View.VISIBLE);
        }
    }

    public void recordCustomFart(View view) {
        CardView btn = (CardView) findViewById(R.id.newCustomFart);
        if(isRecording) {
            // Stop recording
            activeRecording.stopRecording();

            // Reset the button
            this.recordBtnThrobber.cancel();
            ImageView icon = (ImageView) findViewById(R.id.newCustomFart_icon);
            icon.setImageResource(R.drawable.ic_add_white_48dp);
            ObjectAnimator.ofObject(btn, "cardBackgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.recording_end), getResources().getColor(R.color.green))
                          .setDuration(750)
                          .start();

            // Display name prompt
            final EditText filenameText = new EditText(this);
            filenameText.setHint(R.string.fart_save_dialog_hint);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.fart_save_dialog_title)
                   .setMessage(R.string.fart_save_dialog_message)
                   .setView(filenameText)
                   .setPositiveButton(R.string.farts_help_save, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int id) {
                           // Rename the audio file and update the list.
                           String filename = filenameText.getText().toString();
                           if(!filename.equals("")) {
                               activeRecording.rename(filename.replaceAll("[,\\.\\+%]", ""));
                           }
                           allRecordings.add(activeRecording);
                           displayEmptyMsg();

                           mAdapter.notifyItemInserted(allRecordings.size() - 1);
                       }
                   })
                   .setNegativeButton(R.string.farts_help_cancel, new DialogInterface.OnClickListener(){
                       @Override
                       public void onClick(DialogInterface dialog, int id) {
                           // Delete the audio.
                           activeRecording.delete();
                       }
                   })
                   .show();

            // Update recording flag
            isRecording = false;
        } else {
            // Switch button display
            ImageView icon = (ImageView) findViewById(R.id.newCustomFart_icon);
            icon.setImageResource(R.drawable.ic_mic_white_48dp);
            this.recordBtnThrobber.start();

            // Start recording
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            activeRecording = new Recording(ts, getApplicationContext(), "fart_");
            activeRecording.startRecording();

            // Set the recording flag
            isRecording = true;
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {
        mDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent event) {

    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            final View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            int position = mRecyclerView.getChildPosition(view);

            if(position == -1 || isPlaying) {
                return false;
            }

            isPlaying = true;

            final ImageView img = (ImageView) view.findViewById(R.id.play_img);
            img.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
            final TextView title = (TextView) view.findViewById(R.id.recording_title);
            title.setTextColor(getResources().getColor(R.color.white));
            view.setBackgroundColor(getResources().getColor(R.color.green));

            MediaPlayer mPlayer = allRecordings.get(position).play();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    title.setTextColor(getResources().getColor(R.color.offblack));
                    view.setBackgroundColor(getResources().getColor(R.color.white));
                    img.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);

                    isPlaying = false;
                }
            });

            return super.onSingleTapConfirmed(e);
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            View view = mRecyclerView.findChildViewUnder(e1.getX(), e1.getY());
            int position = mRecyclerView.getChildPosition(view);

            if(position == -1) {
                return false;
            }

            // Determine fling direction
            final int SWIPE_MIN_DISTANCE = 120;
            final int SWIPE_MAX_OFF_PATH = 250;
            final int SWIPE_THRESHOLD_VELOCITY = 200;

            final int NO_SWIPE = 0;
            final int LEFT_SWIPE = 1;
            final int RIGHT_SWIPE = 2;
            int direction = NO_SWIPE;

            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
                    direction = NO_SWIPE;
                }
                // right to left swipe
                else if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    direction = LEFT_SWIPE;
                }
                // left to right swipe
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    direction = RIGHT_SWIPE;
                }
            } catch (Exception e) {
                Log.e("Recording Fling", e.getMessage());
                direction = NO_SWIPE;
            }

            // Take action
            if(direction == NO_SWIPE) {
                return false;
            } else {
                if(allRecordings.get(position).delete()) {
                    allRecordings.remove(position);
                    mAdapter.notifyItemRemoved(position);

                    if(direction == LEFT_SWIPE) {
                        YoYo.with(Techniques.SlideOutLeft)
                                .duration(500)
                                .playOn(view);
                    } else {
                        YoYo.with(Techniques.SlideOutRight)
                                .duration(500)
                                .playOn(view);
                    }

                    displayEmptyMsg();
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
