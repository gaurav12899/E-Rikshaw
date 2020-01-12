package com.example.e_rikshaw;

import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_rikshaw.HistoryRecyleView.HistoryAdapter;
import com.example.e_rikshaw.HistoryRecyleView.HistoryObjects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private String customerOrDriver,userId;
    private RecyclerView mHistoryRecylerView;
    private RecyclerView.Adapter mHistoryAdopter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mHistoryRecylerView =(RecyclerView) findViewById(R.id.history_recycle_view);
        mHistoryRecylerView.setNestedScrollingEnabled(true);
        mHistoryRecylerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecylerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdopter = new HistoryAdapter(getDataSetHistory(),HistoryActivity.this);
        mHistoryRecylerView.setAdapter(mHistoryAdopter);

        customerOrDriver =getIntent().getExtras().getString("customerOrDriver");
        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();
    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(customerOrDriver).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference HistoryDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        HistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String rideId=dataSnapshot.getKey();
                    Long timestamp=0L;
                    for(DataSnapshot child:  dataSnapshot.getChildren()){
                        if(child.getKey().equals("timestamp")){
                            timestamp=Long.valueOf(child.getValue().toString());
                        }
                    }
                    HistoryObjects obj = new HistoryObjects(rideId,getDate(timestamp)) ;
                    resultHistory.add(obj);
                        mHistoryAdopter.notifyDataSetChanged();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);

        String date = DateFormat.format("dd-MM--yyyy",cal).toString();
    return date;}

    private ArrayList resultHistory = new ArrayList<HistoryObjects>();
    private List<HistoryObjects> getDataSetHistory(){
        return resultHistory;
    }
}