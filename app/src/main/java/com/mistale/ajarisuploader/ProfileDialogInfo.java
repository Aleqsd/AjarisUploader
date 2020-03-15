package com.mistale.ajarisuploader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ProfileDialogInfo extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String name = getArguments().getString("name");
        String login = getArguments().getString("login");
        String url = getArguments().getString("url");
        String base = getArguments().getString("base");
        String importProfile = getArguments().getString("import");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.profile_dialog, null);
        builder.setView(view).setNegativeButton(R.string.dialog_ok, (dialog, id) -> ProfileDialogInfo.this.getDialog().cancel());

        TextView dialog_name = view.findViewById(R.id.dialog_name);
        TextView dialog_login = view.findViewById(R.id.dialog_login);
        TextView dialog_url = view.findViewById(R.id.dialog_url);
        TextView dialog_base = view.findViewById(R.id.dialog_base);
        TextView dialog_import = view.findViewById(R.id.dialog_import);
        dialog_name.setText(name);
        dialog_login.setText(getString(R.string.dialog_login) + " : " + login);
        dialog_url.setText(getString(R.string.dialog_url) + " : " + url);
        dialog_base.setText(getString(R.string.dialog_base) + " : " + base);
        dialog_import.setText(getString(R.string.dialog_import) + " : " + importProfile);

        return builder.create();
    }
}