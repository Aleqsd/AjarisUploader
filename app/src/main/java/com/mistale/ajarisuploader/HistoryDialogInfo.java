package com.mistale.ajarisuploader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class HistoryDialogInfo extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Contribution contribution = Contribution.stringToContribution(getArguments().getString("contribution"));

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.history_dialog, null);
        builder.setView(view).setNegativeButton(R.string.dialog_ok, (dialog, id) -> HistoryDialogInfo.this.getDialog().cancel());

        TextView dialog_id = view.findViewById(R.id.dialog_contribution_id);
        dialog_id.setText(getString(R.string.dialog_contribution_id) + " : " + contribution.getId());

        // TODO: display list of uploads

        return builder.create();
    }
}