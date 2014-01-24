package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.adapter.InvListViewAdapter;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RequestTask;
import gov.nysenate.inventory.android.R.anim;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.android.R.menu;
import gov.nysenate.inventory.model.InvItem;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class VerSummaryActivity extends SenateActivity
{
    ArrayList<InvItem> AllScannedItems = new ArrayList<InvItem>();
    ArrayList<InvItem> missingItems = new ArrayList<InvItem>(); // Items in the
                                                                // location
                                                                // which have
                                                                // not been
                                                                // verified.
    ArrayList<InvItem> newItems = new ArrayList<InvItem>(); // Items in the
                                                            // location but
                                                            // listed elsewhere
                                                            // in the database.
    ArrayList<InvItem> scannedBarcodeNumbers = new ArrayList<InvItem>();
    TextView tvTotItemVSum;
    TextView tvTotScanVSum;
    TextView tvMisItems;
    TextView tvNewItems;
    String barcodeNum = "";

    public String res = null;
    String loc_code = null;
    String cdloctype = null;

    static Button btnVerSumBack;
    static Button btnVerSumCont;
    ProgressBar progressVerSum;
    boolean positiveButtonPressed = false;
    Activity currentActivity;
    String timeoutFrom = "VERIFICATIONSUMMARY";
    public final int VERIFICATIONREPORTS_TIMEOUT = 101,
            CONTINUEBUTTON_TIMEOUT = 102;
    String URL = null;
    String className = this.getClass().getSimpleName();
    boolean currentlyRestoringAutosave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_summary);
        registerBaseActivityReceiver();
        System.out.println("CLASSNAME:" + className);
        currentActivity = this;

        try {
            autosave = getIntent().getParcelableExtra("autosave");
            if (autosave != null) {
                System.out
                        .println("****VERIFICATIONSAN: TRYING TO RESTORE....");
            }
        } catch (Exception e) {
            e.printStackTrace();
            autosave = null;
            System.out.println("****VERIFICATIONSAN: NOTHING TO RESTORE.");
        }

        try {
            ContentValues values = new ContentValues();
            values.put("naactivity", className);
            values.put("dttxnupdate", MenuActivity.invSaveDB.getNow());
            values.put("natxnupduser", LoginActivity.nauser);

            long rowcnt = MenuActivity.invSaveDB.update("AM12ACTIVITY", values,
                    "nuxractivity = ?",
                    new String[] { Verification.nuxractivity });

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Summary Fields

        tvTotItemVSum = (TextView) findViewById(R.id.tvTotItemVSum);
        tvTotScanVSum = (TextView) findViewById(R.id.tvTotScanVSum);
        tvMisItems = (TextView) findViewById(R.id.tvMisItems);
        tvNewItems = (TextView) findViewById(R.id.tvNewItems);

        // Code for tab

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabSpec spec1 = tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Scanned");

        TabSpec spec2 = tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Unscanned");
        spec2.setContent(R.id.tab2);

        TabSpec spec3 = tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("New/Found");
        spec3.setContent(R.id.tab3);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);

        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i)
                    .findViewById(android.R.id.title);
            tv.setTextSize(20);
        }

        // Find the ListView resource.
        ListView ListViewTab1 = (ListView) findViewById(R.id.listView1);
        ListView ListViewTab2 = (ListView) findViewById(R.id.listView2);
        ListView ListViewTab3 = (ListView) findViewById(R.id.listView3);

        // Setup Buttons and Progress Bar
        this.progressVerSum = (ProgressBar) findViewById(R.id.progressVerSum);
        VerSummaryActivity.btnVerSumBack = (Button) findViewById(R.id.btnVerSumBack);
        VerSummaryActivity.btnVerSumBack.getBackground().setAlpha(255);
        VerSummaryActivity.btnVerSumCont = (Button) findViewById(R.id.btnVerSumCont);
        VerSummaryActivity.btnVerSumCont.getBackground().setAlpha(255);

        // get Lists from intent of previous activity
        AllScannedItems = VerScanActivity.AllScannedItems;
        /*
         * AllScannedItems = this.getInvArrayListFromJSON(getIntent()
         * .getStringArrayListExtra("scannedList"));
         */
        missingItems = VerScanActivity.missingItems;
        /*
         * missingItems = this.getInvArrayListFromJSON(getIntent()
         * .getStringArrayListExtra("missingList"));
         */
        newItems = VerScanActivity.newItems;
        /*
         * newItems = this.getInvArrayListFromJSON(getIntent()
         * .getStringArrayListExtra("newItems"));
         */
        scannedBarcodeNumbers = VerScanActivity.scannedItems;
        /*
         * scannedBarcodeNumbers = this.getInvArrayListFromJSON(getIntent()
         * .getStringArrayListExtra("scannedBarcodeNumbers"));
         */
        loc_code = getIntent().getStringExtra("loc_code");
        cdloctype = getIntent().getStringExtra("cdloctype");
        String summary = getIntent().getStringExtra("summary");
        try {
            JSONObject jsonObject = new JSONObject(summary);
            try {
                tvTotItemVSum.setText(jsonObject.getString("nutotitems"));

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                tvTotScanVSum.setText(jsonObject.getString("nuscanitems"));

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                tvMisItems.setText(jsonObject.getString("numissitems"));

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                tvNewItems.setText(jsonObject.getString("nunewitems"));

            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (JSONException e) {

        }

        // summary =
        // "{\"nutotitems\":\""+numItems+"\",\"nuscanitems\":\""+AllScannedItems.size()+"\",\"numissitems\":\""+missingItems.size()+"\",\"nunewitems\":\""+newItems.size()+"\"}";

        TextView locCodeView = (TextView) findViewById(R.id.textView2);
        locCodeView.setText(Verification.autoCompleteTextView1.getText());

        // Create ArrayAdapter using the planet list.
        Log.i("VerSumActivity", "onCreate before listAdapter1");
        InvListViewAdapter listAdapter1 = new InvListViewAdapter(this,
                R.layout.invlist_item, AllScannedItems);
        Log.i("VerSumActivity", "onCreate before listAdapter2");
        InvListViewAdapter listAdapter2 = new InvListViewAdapter(this,
                R.layout.invlist_item, missingItems);
        Log.i("VerSumActivity", "onCreate before listAdapter3");
        InvListViewAdapter listAdapter3 = new InvListViewAdapter(this,
                R.layout.invlist_item, newItems);
        // Set the ArrayAdapter as the ListView's adapter.
        ListViewTab1.setAdapter(listAdapter1);
        ListViewTab2.setAdapter(listAdapter2);
        ListViewTab3.setAdapter(listAdapter3);
        Log.i("VerSumActivity", "onCreate done");
        handleAutosave();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.i("onActivityResult", "requestCode:" + requestCode
        // + ", resultCode:" + resultCode + " = " + RESULT_OK);
        switch (requestCode) {
        case VERIFICATIONREPORTS_TIMEOUT:
            if (resultCode == RESULT_OK) {
                submitVerification();
                break;
            }
        case CONTINUEBUTTON_TIMEOUT:
            break;
        }
    }

    public ArrayList<InvItem> getInvArrayListFromJSON(ArrayList<String> ar) {
        ArrayList<InvItem> returnList = new ArrayList<InvItem>();
        InvItem curInvItem;
        if (ar != null) {
            for (int x = 0; x < ar.size(); x++) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(ar.get(x));
                    curInvItem = new InvItem();
                    try {
                        curInvItem
                                .setNusenate(jsonObject.getString("nusenate"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    try {
                        curInvItem.setCdcategory(jsonObject
                                .getString("cdcategory"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    try {
                        curInvItem.setType(jsonObject.getString("type"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    try {
                        curInvItem.setDecommodityf(jsonObject
                                .getString("decommodityf"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }

                    try {
                        curInvItem.setDecomments(jsonObject
                                .getString("decomments"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }

                    try {
                        curInvItem.setCdcommodity(jsonObject
                                .getString("cdcommodity"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }

                    returnList.add(curInvItem);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Log.i("System.err",
                            "ERROR CONVERTING FROM ARRAYLIST OF JSON" + x + ":"
                                    + ar.get(x));
                    e.printStackTrace();
                }
            }
        }
        return returnList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_ver_summary, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        positiveButtonPressed = false;
        // Setup Buttons and Progress Bar
        this.progressVerSum = (ProgressBar) findViewById(R.id.progressVerSum);
        VerSummaryActivity.btnVerSumBack = (Button) findViewById(R.id.btnVerSumBack);
        VerSummaryActivity.btnVerSumBack.getBackground().setAlpha(255);
        VerSummaryActivity.btnVerSumCont = (Button) findViewById(R.id.btnVerSumCont);
        VerSummaryActivity.btnVerSumCont.getBackground().setAlpha(255);
        if (progressVerSum == null) {
            progressVerSum = (ProgressBar) this
                    .findViewById(R.id.progressVerSum);
        }
        progressVerSum.setVisibility(View.INVISIBLE);
    }

    public void backButton(View view) {
        if (checkServerResponse(true) == OK) {
            btnVerSumBack.getBackground().setAlpha(45);
            this.onBackPressed();
        }
    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>NO SERVER RESPONSE</font>"));

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                Context context = getApplicationContext();

                                CharSequence text = "No action taken due to NO SERVER RESPONSE";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text,
                                        duration);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                dialog.dismiss();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) == OK) {
            /*
             * Log.i("continueButton",
             * "Check for Session by using KeepSessionAlive");
             */
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();
            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/KeepSessionAlive");
            String response = null;
            try {
                response = resr1.get().toString();
                // Log.i("continueButton", "KeepSessionAlive RESPONSE:" +
                // response);
                if (resr1 == null || response == null
                        || response.trim().length() == 0) {
                    this.noServerResponse();
                    return;
                } else if (response.indexOf("Session timed out") > -1) {
                    continueButtonTimeout();
                    return;
                }
            } catch (Exception e) {
                if (resr1 == null || response == null
                        || response.trim().length() == 0) {
                    this.noServerResponse();
                    return;
                }
            }

            // Since AlertDialogs are asynchronous, need logic to display one at
            // a
            // time.
            if (foundItemsScanned()) {
                displayFoundItemsDialog();
            } else if (newItemsScanned()) {
                displayNewItemsDialog();
            } else {
                displayVerificationDialog();
            }
        }
    }

    public void continueButtonTimeout() {
        Intent intentTimeout = new Intent(VerSummaryActivity.this,
                LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, CONTINUEBUTTON_TIMEOUT);
    }

    private void displayFoundItemsDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>Warning</font>"));
        dialogBuilder
                .setMessage(Html
                        .fromHtml("<font color='RED'><b>***WARNING:</font> The "
                                + numFoundItems()
                                + " Item/s found in OTHER</b> locations will be moved to the current location: <b>"
                                + loc_code
                                + "</b>. <br><br>"
                                + "Continue with Verification Submission (Y/N)?"));
        dialogBuilder.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (newItemsScanned()) {
                            displayNewItemsDialog();
                        } else {
                            displayVerificationDialog();
                        }
                    }
                });

        dialogBuilder.setNegativeButton(Html.fromHtml("<b>No</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void displayNewItemsDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setTitle(Html
                        .fromHtml("<font color='#000055'>***WARNING: New items will not be tagged to location</font>"));
        dialogBuilder
                .setMessage(Html
                        .fromHtml("<font color='RED'><b>***WARNING:</font> The "
                                + " NEW Items scanned will "
                                + "NOT be tagged to location: "
                                + loc_code
                                + ".</b><br><br>"
                                + "Issue information for these items must be completed via the Inventory Issue Record E/U.<br><br>"
                                + "Continue with Verification Submission (Y/N)?"));
        dialogBuilder.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        displayVerificationDialog();
                    }
                });

        dialogBuilder.setNegativeButton(Html.fromHtml("<b>No</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void displayVerificationDialog() {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
        confirmDialog
                .setTitle(Html
                        .fromHtml("<font color='#000055'>Verification Confirmation</font>"));
        confirmDialog
                .setMessage("Are you sure you want to submit this verification?");
        confirmDialog.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (positiveButtonPressed) {
                            /*
                             * Context context = getApplicationContext(); int
                             * duration = Toast.LENGTH_SHORT;
                             * 
                             * Toast toast = Toast.makeText(context,
                             * "Button was already been pressed.",
                             * Toast.LENGTH_SHORT);
                             * toast.setGravity(Gravity.CENTER, 0, 0);
                             * toast.show();
                             */
                        } else {
                            positiveButtonPressed = true;
                            submitVerification();
                        }
                    }
                });

        confirmDialog.setNegativeButton(Html.fromHtml("<b>No</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = confirmDialog.create();
        dialog.show();
    }

    private boolean newItemsScanned() {
        boolean exist = false;
        for (InvItem item : newItems) {
            if (item.getType().equalsIgnoreCase("NEW")) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    private boolean foundItemsScanned() {
        boolean exist = false;
        for (InvItem item : newItems) {
            if (!item.getType().equalsIgnoreCase("NEW")
                    && !item.getType().equalsIgnoreCase("INACTIVE")) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    private int numNewItems() {
        int numNewItems = 0;
        for (InvItem item : newItems) {
            if (item.getType().equalsIgnoreCase("NEW")
                    || item.getType().equalsIgnoreCase("INACTIVE")) {
                numNewItems++;
            }
        }
        return numNewItems;
    }

    private int numFoundItems() {
        return newItems.size() - numNewItems();
    }

    public void handleAutosave() {
        if (this.autosave != null) {
            currentlyRestoringAutosave = true;
        }
        currentlyRestoringAutosave = false;
    }

    private void submitVerification() {
        Log.i("submitVerification", "start");

        VerSummaryActivity.btnVerSumCont.getBackground().setAlpha(45);
        progressVerSum.setVisibility(View.VISIBLE);
        Log.i("submitVerification", "2");
        barcodeNum = "";
        for (int i = 0; i < scannedBarcodeNumbers.size(); i++) {
            barcodeNum += scannedBarcodeNumbers.get(i).getNusenate() + ",";
        }
        Log.i("submitVerification", "get Tag #'s done");

        // Create a JSON string from the arraylist

        // Send it to the server

        // check network connection
        // Log.i("submitVerification", "connection");
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.i("submitVerification", "network");
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i("submitVerification", "network connection available");

            AsyncTask<String, String, String> resr1;
            try {
                // Get the URL from the properties

                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < scannedBarcodeNumbers.size(); i++) {
                    jsonArray.put(scannedBarcodeNumbers.get(i).getJSONObject());
                }
                ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
                postParams.add(new BasicNameValuePair("cdlocat", loc_code));
                postParams.add(new BasicNameValuePair("cdloctype", cdloctype));
                postParams.add(new BasicNameValuePair("scannedItems", jsonArray
                        .toString()));
                resr1 = new RequestTask(postParams)
                        .execute("/VerificationReports");

                Log.i("submitVerification", "SUBMIT TO URL");

                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    Log.i("submitVerification", "RESULTS:" + res);
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(VERIFICATIONREPORTS_TIMEOUT);
                        return;
                    }

                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
        }

        // Display Toaster
        Context context = getApplicationContext();
        CharSequence text = res;
        int duration = Toast.LENGTH_LONG;
        // Log.i("SubmitVerification", "Server Response:" + res);
        if (res == null) {
            text = "!!ERROR: NO RESPONSE FROM SERVER";
        } else if (res.length() == 0) {
            text = "Database not updated";
        } else if (res.trim().startsWith("!!ERROR:")
                || res.trim().startsWith("***WARNING:")) {
            text = res.trim();
        } else {
            duration = Toast.LENGTH_SHORT;
        }

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        /*
         * If there was some kind of error, don't leave the Activity. User will
         * have to then call or cancel the verification. At least the user will
         * be aware that some problem occured instead of ignoring the message.
         */

        if (text.equals("Database not updated")
                || text.toString().startsWith("!!ERROR:")
                || text.toString().startsWith("***WARNING:")) {
            return;
        }

        // ===================ends

        // If all appears to have gone well, then we can get rid of the records
        // for the autosave feature
        long rowCnt = MenuActivity.invSaveDB.delete("AM12ACTIVITY",
                "nuxractivity = ?", new String[] { Verification.nuxractivity });
        System.out.println("*******DELETED " + rowCnt
                + " record(s) from AM12ACTIVITY for nuxractivity = "
                + Verification.nuxractivity);
        rowCnt = MenuActivity.invSaveDB.delete("ad12verinv",
                "nuxractivity = ?", new String[] { Verification.nuxractivity });
        System.out.println("*******DELETED " + rowCnt
                + " record(s) from ad12verinv for nuxractivity = "
                + Verification.nuxractivity);

        Log.i("submitVerification", "go to menu");
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    @Override
    public void startTimeout(int timeoutType) {
        this.progressVerSum.setVisibility(View.INVISIBLE);
        VerSummaryActivity.btnVerSumCont.getBackground().setAlpha(255);
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

}
