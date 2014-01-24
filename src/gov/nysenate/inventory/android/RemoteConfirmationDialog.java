package gov.nysenate.inventory.android;

import java.util.ArrayList;

import gov.nysenate.inventory.activity.Delivery3;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.Toasty;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class RemoteConfirmationDialog extends DialogFragment
{

    private ArrayList<String> employeeNameList;
    private Transaction delivery;
    private Spinner verMethod = null;
    private ClearableAutoCompleteTextView remoteSigner = null;
    private ClearableEditText remoteComment;
    private ClearableEditText remoteHelpReferenceNum;

    public static RemoteConfirmationDialog newInstance(
            ArrayList<String> employeeNameList, Transaction delivery) {
        RemoteConfirmationDialog frag = new RemoteConfirmationDialog();
        frag.employeeNameList = employeeNameList;
        frag.delivery = delivery;
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptView = li.inflate(R.layout.prompt_dialog_remote, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder.setView(promptView);

        verMethod = (Spinner) promptView.findViewById(R.id.remote_method);
        remoteSigner = (ClearableAutoCompleteTextView) promptView
                .findViewById(R.id.remote_employee_signer);
        remoteComment = (ClearableEditText) promptView
                .findViewById(R.id.remote_comments);
        remoteHelpReferenceNum = (ClearableEditText) promptView
                .findViewById(R.id.remote_helprefnum);
        remoteHelpReferenceNum.setVisibility(ClearableEditText.INVISIBLE);

        // Display a text box for OSR reference number only if they selected OSR
        // as the verification method.
        verMethod.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (verMethod.getSelectedItem() != null) {
                    if (verMethod.getSelectedItem().toString()
                            .equalsIgnoreCase("OSR Verified")) {
                        remoteHelpReferenceNum
                                .setVisibility(ClearableEditText.VISIBLE);
                    } else {
                        remoteHelpReferenceNum
                                .setVisibility(ClearableEditText.INVISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.remote_ver_method,
                        android.R.layout.simple_spinner_item);
        spinAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        verMethod.setAdapter(new NothingSelectedSpinnerAdapter(spinAdapter,
                R.layout.spinner_nothing_selected, getActivity()));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, employeeNameList);

        remoteSigner.setAdapter(adapter);
        remoteSigner.setThreshold(1);
        remoteSigner
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        InputMethodManager imm = (InputMethodManager) getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                remoteSigner.getWindowToken(), 0);
                    }
                });

        alertDialogBuilder.setTitle("Remote Information");
        alertDialogBuilder.setPositiveButton("OK", null);
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();

        // Use our own View.onClickListener so the dialog is not automatically
        // dismissed
        // when the positive button is pressed.
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view) {
                        if (completelyFilledOut()) {
                            delivery.setVerificationMethod(verMethod
                                    .getSelectedItem().toString().split(" ")[0]);
                            delivery.setVerificationComments(remoteComment
                                    .getText().toString());
                            int nuxrefem = ((Delivery3) getActivity())
                                    .getEmployeeId(remoteSigner.getText()
                                            .toString());
                            delivery.setEmployeeId(nuxrefem);
                            delivery.setHelpReferenceNum(remoteHelpReferenceNum
                                    .getText().toString());
                            ((Delivery3) getActivity()).positiveDialog();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
        return alertDialog;
    }

    private boolean completelyFilledOut() {
        if (verMethod.getSelectedItem() != null) {
            String methodSelected = verMethod.getSelectedItem().toString();
            boolean isEmployeeEntered = !remoteSigner.getText().toString()
                    .isEmpty();
            boolean isCommentEntered = !remoteComment.getText().toString()
                    .isEmpty();
            boolean isOsrNumEntered = !remoteHelpReferenceNum.getText()
                    .toString().isEmpty();

            if (methodSelected.equals("Paperwork")) {
                if (!isEmployeeEntered) {
                    Toasty.displayCenteredMessage(getActivity(),
                            "Please enter an employee name", Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            } else if (methodSelected.equals("Phone Verified")) {
                if (!isEmployeeEntered) {
                    Toasty.displayCenteredMessage(getActivity(),
                            "Please enter an employee name", Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            } else if (methodSelected.equals("OSR Verified")) {
                if (!isOsrNumEntered) {
                    Toasty.displayCenteredMessage(getActivity(),
                            "Please enter an OSR Reference Number",
                            Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            } else if (methodSelected.equals("Other")) {
                if (!isCommentEntered) {
                    Toasty.displayCenteredMessage(
                            getActivity(),
                            "Please enter comments explaining how this was verified.",
                            Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            }
        }
        Toasty.displayCenteredMessage(getActivity(),
                "Please select a verification method.", Toast.LENGTH_SHORT);
        return false;
    }
}
