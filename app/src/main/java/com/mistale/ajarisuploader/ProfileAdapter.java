package com.mistale.ajarisuploader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileHolder> {
    private ArrayList<Profile> profileList;
    private Context mContext;
    private FragmentActivity activity;

    public ProfileAdapter(Context context, ArrayList<Profile> profileList) {
        this.profileList = profileList;
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
        return this.profileList == null ? 0 : this.profileList.size();
    }

    public void setActivity(@NonNull FragmentActivity activity) {
        this.activity = activity;
    }


    public void removeItem(int position) {
        Preferences.removePreferenceFromPosition(position, this.mContext);
        this.profileList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Profile item, int position) {
        this.profileList.add(position, item);
        Preferences.addPreferenceToPosition(item, position, this.mContext);
        notifyItemInserted(position);
    }

    public ArrayList<Profile> getData() {
        return this.profileList;
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: display dialog profile
                    ProfileDialogInfo dialog = new ProfileDialogInfo();
                    dialog.show(activity.getSupportFragmentManager(), "dialog");
                }
            });
        }

        public void setProfileName(String name) {
            profileName.setText(name);
        }

        public void setProfileLogin(String login) {
            profileLogin.setText(login);
        }
    }
}