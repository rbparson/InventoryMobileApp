package gov.nysenate.inventory.android;

import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.activity.VerScanActivity.spinSortListComparator;
import gov.nysenate.inventory.adapter.AutosaveListViewAdapter;
import gov.nysenate.inventory.adapter.CommodityListViewAdapter;
import gov.nysenate.inventory.listener.AutosaveFoundDialogListener;
import gov.nysenate.inventory.model.AutoSaveItem;
import gov.nysenate.inventory.model.Commodity;

import android.content.Context;
import android.content.DialogInterface.OnShowListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableRow;

//...G
@SuppressLint("ValidFragment")
public class AutosaveFoundDialog extends DialogFragment
{

    public static Spinner spnAutosaveType;
    SenateActivity senateActivity;
    static public ListView lvAutosaves = null;
    static public ListView lvTest = null;
    public String title = null;
    public String msg = null;
    public String deacttype = null;
    public int nuxracttype = -1;
    public String naactivity = null;
    public int nuxractivity = -1;
    public Set autosaveTypes;
    public ArrayList<AutoSaveItem> autosaveList;
    public ArrayAdapter spnAutosaveTypeAdapter = null;
    static public AutosaveListViewAdapter lvAutosavesAdapter = null;
    static public CommodityListViewAdapter lvTestAdapter = null;
    public ArrayList<Commodity> testList = new ArrayList<Commodity>();
    Context context;
    TableRow tableRow1;

    List<AutosaveFoundDialogListener> listeners = new ArrayList<AutosaveFoundDialogListener>();

    public AutosaveFoundDialog(SenateActivity senateActivity, String title,
            String msg, Set autosaveTypes, ArrayList autosaveList,
            AutosaveListViewAdapter lvAutosavesAdapter) {
        this.senateActivity = senateActivity;
        this.context = (Context) senateActivity;
        this.title = title;
        this.msg = msg;
        this.autosaveTypes = autosaveTypes;
        this.autosaveList = autosaveList;
        Commodity currCommodity = new Commodity();
        currCommodity.setCdcommodity("TEST1");
        currCommodity.setDecommodityf("This is a test of the commodity code");
        testList.add(currCommodity);
        currCommodity = new Commodity();
        currCommodity.setCdcommodity("TEST2");
        currCommodity
                .setDecommodityf("This is another test of the commodity code");
        testList.add(currCommodity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater
                .inflate(R.layout.dialog_autosave_found, null);
        spnAutosaveType = (Spinner) dialogView
                .findViewById(R.id.spnAutosaveType);
        spnAutosaveTypeAdapter = new ArrayAdapter((Context) senateActivity,
                android.R.layout.simple_spinner_dropdown_item,
                autosaveTypes.toArray());

        spnAutosaveType.setAdapter(spnAutosaveTypeAdapter);
        spnAutosaveType.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int pos, long arg3) {
                String autosaveType = spnAutosaveTypeAdapter.getItem(pos).toString();
                lvAutosavesAdapter.filterOn(autosaveType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                
            }
        });

        lvAutosaves = (ListView) dialogView.findViewById(R.id.lvAutosaves);

        lvAutosaves = (ListView) dialogView.findViewById(R.id.lvTest);

        lvTest = (ListView) dialogView.findViewById(R.id.lvTest);
        Commodity testCommodity = new Commodity();
        testCommodity.setCdcategory("TESTCAT");
        testCommodity.setCdcommodity("COMMODITY1");
        testCommodity.setDecommodityf("TEST COMMODITY 1");
        testCommodity.setDecomments("TEST COMMENTS 1");

        testList.add(testCommodity);

        testCommodity = new Commodity();
        testCommodity.setCdcategory("TESTCAT");
        testCommodity.setCdcommodity("COMMODITY2");
        testCommodity.setDecommodityf("TEST COMMODITY 2");
        testCommodity.setDecomments("TEST COMMENTS 2");

        testList.add(testCommodity);

        lvAutosavesAdapter = new AutosaveListViewAdapter(context,
                senateActivity, R.layout.row_autosave, deacttype, autosaveList,
                this);
        lvTestAdapter = new CommodityListViewAdapter(context,
                R.layout.commoditylist_row, testList, null);

        lvAutosaves.setAdapter(lvAutosavesAdapter);
        lvAutosaves.setEnabled(true);

        /*
         * This OnClick Listener was not working no matter what I tried to do
         * (even when blocking the ListView's Children from being focusable) So
         * the listener is moved to the Children in the Adapter.
         */

        lvAutosaves.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                System.out.println("Clicked on #:" + position);
                if (position < 0) {
                    return;
                }
                // AutosaveListViewAdapter adapter = (AutosaveListViewAdapter)
                // lvAutosaves.getAdapter();
            }

        });

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setTitle(
                        Html.fromHtml("<font color='#000055'>" + title
                                + "</font>"))
                .setMessage(Html.fromHtml(msg))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                for (AutosaveFoundDialogListener autosaveFoundDialogListener : listeners)
                                    autosaveFoundDialogListener
                                            .onAutosaveOKButtonClicked(
                                                    deacttype, nuxracttype,
                                                    naactivity, nuxractivity);
                                dismiss();
                            }
                        })
                .setNegativeButton(Html.fromHtml("<b>No</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // cancelMsg();
                                dismiss();
                            }
                        });

        /*
         * if (senateActivity.dialogComments != null) {
         * senateActivity.dialogComments = null; }
         */

        this.setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        /*
         * dialog.setOnShowListener(new OnShowListener() {
         * 
         * @Override public void onShow(DialogInterface dialog) {
         * ((AlertDialog)dialog
         * ).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); } });
         */
        // Create the AlertDialog object and return it
        return dialog;
    }

    public void addListener(AutosaveFoundDialogListener addListener) {
        listeners.add(addListener);
    }

    public class AutosaveTypeChangedListener implements OnItemSelectedListener
    {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                long id) {
            String currentAutosaveTypeValue = parent.getItemAtPosition(pos)
                    .toString();
            lvAutosavesAdapter.filterOn(currentAutosaveTypeValue);
            lvAutosavesAdapter.notifyDataSetChanged();
            // listView.setAdapter(adapter);

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

}