package com.example.ajarisuploader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileHolder> {
    private ArrayList<Profile> profileList;
    private Context mContext;

    public ProfileAdapter(Context context, ArrayList<Profile> contactsList) {
        this.profileList = contactsList;
        this.mContext = context;
    }

    @Override
    public ProfileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.item_profile, parent, false);
        return new ProfileHolder(view);
    }

    @Override
    public int getItemCount() {
        return profileList == null ? 0 : profileList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileHolder holder, final int position) {
        final Profile profile = profileList.get(position);
        holder.setProfileName(profile.getName());
        holder.setProfileLogin(profile.getLogin());
    }

    public class ProfileHolder extends RecyclerView.ViewHolder {

        private TextView profileName;
        private TextView profileLogin;

        public ProfileHolder(View itemView) {
            super(itemView);

            profileName = itemView.findViewById(R.id.name);
            profileLogin = itemView.findViewById(R.id.login);
        }

        public void setProfileName(String name) {
            profileName.setText(name);
        }

        public void setProfileLogin(String login) {
            profileLogin.setText(login);
        }
    }
}