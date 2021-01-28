package com.icycoke.android.wifiwebviewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class ScanResDialog extends DialogFragment {

    private List<ScanResult> results;
    private ScanResDialogListener listener;

    private ScanResDialog() {
    }

    public static ScanResDialog newInstance(List<ScanResult> results) {
        ScanResDialog fragment = new ScanResDialog();
        fragment.setResults(results);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String[] ssidArr = new String[results.size()];
        for (int i = 0; i < results.size(); i++) {
            ssidArr[i] = results.get(i).SSID;
        }

        builder.setTitle(R.string.select_a_network)
                .setItems(ssidArr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.connectTo(results.get(which));
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ScanResDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ScanResDialogListener");
        }
    }

    private void setResults(List<ScanResult> results) {
        this.results = results;
    }

    public interface ScanResDialogListener {
        void connectTo(ScanResult target);
    }
}

