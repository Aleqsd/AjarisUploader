package com.mistale.ajarisuploader;

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
        holder.setId(Integer.toString(contribution.getId()));
        holder.setLogin(Integer.toString(contribution.getNumberOfUploads()));
    }

    public class HistoryHolder extends RecyclerView.ViewHolder {

        private TextView contributionId;
        private TextView contributionLength;

        public HistoryHolder(View itemView) {
            super(itemView);

            contributionId = itemView.findViewById(R.id.identifiant);
            contributionLength = itemView.findViewById(R.id.length);

            itemView.setOnClickListener(v -> {
                HistoryDialogInfo dialog = new HistoryDialogInfo();
                Bundle bundle = new Bundle();
                Contribution contribution = contributionList.get(getLayoutPosition());
                bundle.putString("contribution", contribution.toString());
                dialog.setArguments(bundle);
                dialog.show(activity.getSupportFragmentManager(), "dialog");
            });
        }

        public void setId(String id) {
            contributionId.setText(id);
        }

        public void setLogin(String login) {
            contributionLength.setText(login);
        }
    }
}