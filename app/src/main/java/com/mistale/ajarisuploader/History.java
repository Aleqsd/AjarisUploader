package com.mistale.ajarisuploader;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class History extends Fragment {

    private View view;
    HistoryAdapter adapter;
    RecyclerView recyclerView;
    FrameLayout historyLayout;

    public static History newInstance() {
        return new History();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.history_fragment, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /********************** For tests purpose ***********************/
        ArrayList<Upload> uploads = new ArrayList<>();
        DateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        //UploadHistory.removeAllPreferences(getContext());
        uploads.add(new Upload("test.png", Calendar.getInstance().getTime(), "test comment", new Profile()));
        uploads.add(new Upload("test.png", Calendar.getInstance().getTime(), "test comment", new Profile()));
        uploads.add(new Upload("test.png", Calendar.getInstance().getTime(), "test comment", new Profile()));
        uploads.add(new Upload("test.png", Calendar.getInstance().getTime(), "test comment", new Profile()));
        Contribution contribution = new Contribution(1245, uploads);
        System.out.println(contribution.toString());
        UploadHistory.addPreference(contribution, getContext());
        System.out.println(contribution.getNumberOfUploads());
        /****************************************************************/

        ArrayList<Contribution> contributions;
        this.historyLayout = this.view.findViewById(R.id.historyLayout);
        this.recyclerView = this.view.findViewById(R.id.history_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        contributions = UploadHistory.getPreferences(getContext());
        this.adapter = new HistoryAdapter(getContext(), contributions);
        recyclerView.setAdapter(this.adapter);
        this.adapter.notifyDataSetChanged();
    }
}
