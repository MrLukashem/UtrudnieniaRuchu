package com.example.mrlukashem.utrudnieniaruchu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by mrlukashem on 07.05.15.
 */
public class CantLogInDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder _builder = new AlertDialog.Builder(getActivity());

        _builder
                .setTitle("Błąd logowania")
                .setMessage("Błędne hasło lub meila")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface __dialog, int __which) {
                        __dialog.dismiss();
                    }
                });

        return  _builder.create();
    }
}
