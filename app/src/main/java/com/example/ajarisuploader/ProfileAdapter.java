package com.example.ajarisuploader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {
    public ProfileAdapter(Context context, ArrayList<Profile> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Profile profile = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_profile, parent, false);
        }
        // Lookup view for data population
        TextView profileName = (TextView) convertView.findViewById(R.id.name);
        TextView profileLogin = (TextView) convertView.findViewById(R.id.login);
        // Populate the data into the template view using the data object
        profileName.setText(profile.getName());
        profileLogin.setText(profile.getLogin());
        // Return the completed view to render on screen
        return convertView;
    }
}