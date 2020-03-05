package com.example.ajarisuploader;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

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


        ArrayList<Profile> profiles = new ArrayList<Profile>();
        Profile profile = new Profile("Adrien CANINO", "Cadrew", "test", "test", 1, "test");
        Profile profile2 = new Profile("Alexandre DO-O ALMEIDA", "Thulium", "test", "test", 1, "test");
        Preferences.removeAllPreferences(getContext());
        Preferences.addPreference(profile, getContext());
        Preferences.addPreference(profile2, getContext());
        ProfileAdapter adapter = new ProfileAdapter(getContext(), profiles);

        profiles = Preferences.getPreferences(getContext());
        for (Profile p : profiles) {
            adapter.add(p);
        }
        // Attach the adapter to a ListView
        ListView listView = (ListView) this.view.findViewById(R.id.profiles_list);
        listView.setAdapter(adapter);
    }

}
