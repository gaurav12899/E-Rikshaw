package com.example.e_rikshaw.ui.History;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryFragment extends AppCompatActivity {
/*
    private HistoryViewModel historyViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel =
                ViewModelProviders.of(this).get(HistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        historyViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }


private RecyclerView mHistoryRecylerView;//mhistory recycle view
private RecyclerView.Adapter mHistoryAdopter;
private RecyclerView.LayoutManager mHistoryLayoutManager;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_history);
        mHistoryRecylerView =(RecyclerView) findViewById(R.id.history_recycle_view);
        mHistoryRecylerView.setNestedScrollingEnabled(false);
        mHistoryRecylerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(HistoryFragment.this);
        mHistoryRecylerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdopter = new HistoryAdapter(getDataSetHistory(),HistoryFragment.this);
        mHistoryRecylerView.setAdapter(mHistoryAdopter);

        for(int i =0 ;i<100;i++){
            HistoryObjects obj = new HistoryObjects(Integer.toString(i));

            resultHistory.add(obj);

        }
        mHistoryAdopter.notifyDataSetChanged();

    }

    private ArrayList resultHistory = new ArrayList<HistoryObjects>();
    private List<HistoryObjects> getDataSetHistory(){
        return resultHistory;
    }
}*/
}
