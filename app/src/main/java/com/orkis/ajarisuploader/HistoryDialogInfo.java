package com.orkis.ajarisuploader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryDialogInfo extends DialogFragment {

    UploadAdapter adapter;
    RecyclerView recyclerView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Contribution contribution = Contribution.stringToContribution(getArguments().getString("contribution"));

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.history_dialog, null);
        builder.setView(view).setNegativeButton(R.string.dialog_ok, (dialog, id) -> HistoryDialogInfo.this.getDialog().cancel());

        TextView dialog_id = view.findViewById(R.id.dialog_contribution_id);
        dialog_id.setText(getString(R.string.dialog_contribution_id) + " : " + contribution.getId());

        ArrayList<Upload> uploads;
        this.recyclerView = view.findViewById(R.id.dialog_history_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        uploads = contribution.getUploads();
        this.adapter = new UploadAdapter(getContext(), uploads);
        recyclerView.setAdapter(this.adapter);
        this.adapter.notifyDataSetChanged();

        return builder.create();
    }
}