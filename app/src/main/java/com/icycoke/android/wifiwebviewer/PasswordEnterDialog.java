package com.icycoke.android.wifiwebviewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PasswordEnterDialog extends DialogFragment {

    private static final String TAG = "PasswordEnterDialog";

    private PasswordEnterDialogListener listener;
    private EditText passwordText;
    private ScanResult target;

    private PasswordEnterDialog() {
    }

    public static PasswordEnterDialog newInstance(ScanResult target) {
        PasswordEnterDialog dialog = new PasswordEnterDialog();
        dialog.setTarget(target);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setTitle(getResources().getString(R.string.enter_the_password))
                .setView(inflater.inflate(R.layout.dialog_password_enter, null));

        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.connectWithPassword(target, passwordText.getText().toString());
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        passwordText = getDialog().findViewById(R.id.passwordText);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (PasswordEnterDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement PasswordEnterDialogListener");
        }
    }

    private void setTarget(ScanResult target) {
        this.target = target;
    }

    public interface PasswordEnterDialogListener {
        void connectWithPassword(ScanResult target, String password);
    }
}
