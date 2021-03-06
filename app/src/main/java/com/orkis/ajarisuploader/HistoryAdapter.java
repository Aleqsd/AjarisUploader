package com.orkis.ajarisuploader;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {
    private ArrayList<Contribution> contributionList;
    private Context mContext;
    private FragmentActivity activity;

    public HistoryAdapter(Context context, ArrayList<Contribution> contributionList) {
        this.contributionList = contributionList;
        this.mContext = context;
    }

    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.item_history, parent, false);
        return new HistoryHolder(view);
    }

    @Override
    public int getItemCount() {
        return this.contributionList == null ? 0 : this.contributionList.size();
    }

    public void setActivity(@NonNull FragmentActivity activity) {
        this.activity = activity;
    }

    public ArrayList<Contribution> getData() {
        return this.contributionList;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, final int position) {
        final Contribution contribution = contributionList.get(position);
        String description = contribution.getUploads().get(0).getComment();
        if (description.length() > 40)
            description = description.substring(0,40) + "...";
        holder.setIdAndComment(contribution.getId() + " : " + description);
        holder.setLogin(Integer.toString(contribution.getNumberOfUploads()));
        holder.setProfile(contribution.getUploads().get(0).getProfile().getName());
    }

    public class HistoryHolder extends RecyclerView.ViewHolder {

        private TextView contributionId;
        private TextView contributionLength;
        private TextView contributionProfile;

        public HistoryHolder(View itemView) {
            super(itemView);

            contributionId = itemView.findViewById(R.id.identifiant);
            contributionLength = itemView.findViewById(R.id.length);
            contributionProfile = itemView.findViewById(R.id.profile);

            itemView.setOnClickListener(v -> {
                HistoryDialogInfo dialog = new HistoryDialogInfo();
                Bundle bundle = new Bundle();
                Contribution contribution = contributionList.get(getLayoutPosition());
                bundle.putString("contribution", contribution.toString());
                dialog.setArguments(bundle);
                dialog.show(activity.getSupportFragmentManager(), "dialog");
            });
        }

        public void setIdAndComment(String idAndComment) {
            contributionId.setText(idAndComment);
        }

        public void setLogin(String login) {
            contributionLength.setText(login);
        }

        public void setProfile(String profile) {
            contributionProfile.setText(profile);
        }

    }
}