package com.example.e_rikshaw.HistoryRecyleView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_rikshaw.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

public TextView rideId;
public TextView time;
public HistoryViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        rideId=(TextView) itemView.findViewById(R.id.rideId);
        time=itemView.findViewById(R.id.time);

    }

    @Override
    public void onClick(View v) {

    }
}
