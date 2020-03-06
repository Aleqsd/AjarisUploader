package com.example.ajarisuploader;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class Profiles extends Fragment {

    private ProfilesViewModel mViewModel;
    private View view;

    public static Profiles newInstance() {
        return new Profiles();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.profiles_fragment, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ProfilesViewModel.class);


        /********************* For tests purposes *********************/
        Profile profile = new Profile("Adrien CANINO", "Cadrew", "test", "test", 1, "test");
        Profile profile2 = new Profile("Alexandre DO-O ALMEIDA", "Thulium", "test", "test", 1, "test");
        Preferences.removeAllPreferences(getContext());
        Preferences.addPreference(profile, getContext());
        Preferences.addPreference(profile2, getContext());
        /*************************************************************/


        ArrayList<Profile> profiles;
        RecyclerView recyclerView = this.view.findViewById(R.id.profiles_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        profiles = Preferences.getPreferences(getContext());
        ProfileAdapter adapter = new ProfileAdapter(getContext(), profiles);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // TODO: Remove item from backing list here
                adapter.notifyDataSetChanged();
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

}
