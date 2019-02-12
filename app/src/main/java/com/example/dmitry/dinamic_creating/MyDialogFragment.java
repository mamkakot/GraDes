package com.example.dmitry.dinamic_creating;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class MyDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] namesArray = {"Кал", "Экскеkменты", "Моча"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Что кушац буш?")
                .setItems(namesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),
                                "Выбранная пища: " + namesArray[which],
                                Toast.LENGTH_SHORT).show();
                    }
                });
        return builder.create();
    }
}