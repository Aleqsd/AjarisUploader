package com.mistale.ajarisuploader;

import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
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
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class Profiles extends Fragment {

    private ProfilesViewModel mViewModel;
    private View view;
    ProfileAdapter adapter;
    RecyclerView recyclerView;
    FrameLayout profilesLayout;

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
        /*Profile profile = new Profile("Adrien CANINO", "Cadrew", "test", "test", 1, "test");
        Profile profile2 = new Profile("Alexandre DO-O ALMEIDA", "Thulium", "test", "test", 1, "test");
        Preferences.removeAllPreferences(getContext());
        Preferences.addPreference(profile, getContext());
        Preferences.addPreference(profile2, getContext());*/
        /*************************************************************/


        ArrayList<Profile> profiles;
        this.profilesLayout = this.view.findViewById(R.id.profilesLayout);
        this.recyclerView = this.view.findViewById(R.id.profiles_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        profiles = Preferences.getPreferences(getContext());
        this.adapter = new ProfileAdapter(getContext(), profiles);
        recyclerView.setAdapter(this.adapter);
        this.adapter.notifyDataSetChanged();

        this.enableSwipeToDeleteAndUndo();
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeProfileCallback swipeToDeleteCallback = new SwipeProfileCallback(getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final Profile item = adapter.getData().get(position);
                adapter.removeItem(position);
                Snackbar snackbar = Snackbar.make(profilesLayout, R.string.action_delete_profile, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.action_delete_undo, view -> {
                    adapter.restoreItem(item, position);
                    recyclerView.scrollToPosition(position);
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

}
