package io.github.mikeflynn.remotewhoopeecushion;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Recording {
    private static final String LOG_TAG = "Recording";
    private static final String PREFIX = "rec_";

    private String filename;
    private Context ctx;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    public Recording(String outfile, Context ctx) {
        this.filename = outfile;
        this.ctx = ctx;
    }

    /* Static methods */
    static public ArrayList<Recording> getList(Context ctx, String prefix) {
        ArrayList<Recording> allFiles = new ArrayList<Recording>();

        String[] fileList = ctx.fileList();
        for(String f : fileList) {
            if (f.startsWith(prefix)) {
                String filename = f.substring(PREFIX.length());
                allFiles.add(new Recording(filename, ctx));
            }
        }

        return allFiles;
    }

    /* Public getter methods */
    public String getPath() {
        return this.ctx.getFilesDir() + PREFIX + this.filename;
    }

    public String getFilename() {
        return this.filename;
    }

    /* Public setter methods */
    public void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(getPath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public void play() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(this.getPath());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void stop() {
        mPlayer.release();
        mPlayer = null;
    }

    public boolean delete() {
        return new File(this.getPath()).delete();
    }

    /* Private methods */
    protected boolean save() {
        return true;
    }
}
