package io.github.mikeflynn.remotewhoopeecushion;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.ViewHolder> {
    //private Context theContext;
    protected ArrayList<Recording> theData;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView recordingView;
        public ViewHolder(CardView v) {
            super(v);
            recordingView = v;
        }
    }

    public RecordingsAdapter(ArrayList<Recording> dataset) {
        theData = dataset;
    }

    public RecordingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Save the application context for later use
        //theContext = parent.getContext();

        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.recording, parent, false);

        return new ViewHolder(v);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        final Recording row = theData.get(position);

        final TextView title = (TextView) holder.recordingView.findViewById(R.id.recording_title);

        String prefix = row.getPrefix();
        if(prefix.equals("rec_")) {
            java.util.Date time = new java.util.Date((long) Integer.parseInt(row.getFilename()) * 1000);
            title.setText(time.toString());
        } else {
            title.setText(row.getFilename());
        }

    //    // Set the click event on the play button
    //    holder.recordingView.setOnClickListener(new View.OnClickListener() {
    //        @Override
    //        public void onClick(View v) {
    //
    //        }
    //    });
    }

    public int getItemCount() {
        return theData.size();
    }
}