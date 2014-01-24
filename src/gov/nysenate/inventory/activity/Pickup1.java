package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.MsgAlert;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RequestTask;
import gov.nysenate.inventory.android.R.anim;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.android.R.menu;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.util.TransactionParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Pickup1 extends SenateActivity
{
    public final static String loc_code_intent = "gov.nysenate.inventory.android.loc_code_str";
    private String res = null;
    private String URL = "";
    private String originSummary = null;
    private String destinationSummary = null;
    public String status = null;
    public String loc_code_str = null;
    public String cdloctypefrom = null;
    public String cdlocatfrom = null;
    public String cdrespctrhdfrom = null;
    public String adstreet1from = null;
    public String adcityfrom = null;
    public String adstatefrom = null;
    public String adzipcodefrom = null;
    public String descriptfrom = null;
    public String nucountfrom = null;
    public String cdloctypeto = null;
    public String cdlocatto = null;
    public String cdrespctrhdto = null;
    public String adstreet1to = null;
    public String adcityto = null;
    public String adstateto = null;
    public String adzipcodeto = null;
    public String descriptto = null;
    public String nucountto = null;
    private Location origin;
    private Location destination;
    private ArrayList<String> allLocations = new ArrayList<String>();
    private ClearableAutoCompleteTextView autoCompleteTextView1;
    private ClearableAutoCompleteTextView autoCompleteTextView2;
    private Button btnPickup1Cont;
    private Button btnPickup1Cancel;
    private TextView tvOffice1;
    private TextView tvDescript1;
    private TextView tvCount1;
    private TextView tvOffice2;
    private TextView tvDescript2;
    private TextView tvCount2;
    private boolean fromLocationBeingTyped = false;
    private boolean toLocationBeingTyped = false;
    public static ProgressBar progBarPickup1;
    String timeoutFrom = "pickup1";
    public final int LOCCODELIST_TIMEOUT = 101,
            FROMLOCATIONDETAILS_TIMEOUT = 102, TOLOCATIONDETAILS_TIMEOUT = 103;

    private int lastSize = 0;
    String className = this.getClass().getSimpleName();
    static String nuxractivity = "";
    private List<Location> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup1);
        registerBaseActivityReceiver();
        try {
            autosave = getIntent().getParcelableExtra("autosave");
            if (autosave != null) {
                System.out.println("****PICKUP1: TRYING TO RESTORE....");
            }
        } catch (Exception e) {
            e.printStackTrace();
            autosave = null;
            System.out.println("****PICKUP1: NOTHING TO RESTORE.");
        }
                
        tvOffice1 = (TextView) this.findViewById(R.id.tvOffice1);
        tvDescript1 = (TextView) this.findViewById(R.id.tvDescript1);
        tvCount1 = (TextView) this.findViewById(R.id.tvCount1);
        tvOffice2 = (TextView) this.findViewById(R.id.tvOffice2);
        tvDescript2 = (TextView) this.findViewById(R.id.tvDescript2);
        tvCount2 = (TextView) this.findViewById(R.id.tvCount2);
        progBarPickup1 = (ProgressBar) this.findViewById(R.id.progBarPickup1);
        autoCompleteTextView1 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        autoCompleteTextView2 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);
        btnPickup1Cont = (Button) findViewById(R.id.btnPickup1Cont);
        btnPickup1Cancel = (Button) findViewById(R.id.btnPickup1Cancel);

        autoCompleteTextView1.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable arg0) {
                int currentSize = autoCompleteTextView1.getText().toString()
                        .length();
                if (currentSize == 0 || currentSize < lastSize) {
                    tvOffice1.setText("N/A");
                    tvDescript1.setText("N/A");
                    tvCount1.setText("N/A");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
                lastSize = autoCompleteTextView1.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // TODO Auto-generated method stub

            }
        });
        autoCompleteTextView2.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable arg0) {
                int currentSize = autoCompleteTextView2.getText().toString()
                        .length();

                if (currentSize == 0 || currentSize < lastSize) {
                    tvOffice2.setText("N/A");
                    tvDescript2.setText("N/A");
                    descriptto =  tvDescript2.getText().toString();
                    tvCount2.setText("N/A");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
                lastSize = autoCompleteTextView2.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // TODO Auto-generated method stub

            }
        });

        try {
            // TODO: RequestDispatcher.getInstance() as parameter for tests DI.
            getAllLocations();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, allLocations);

        setupautoCompleteTextView1(adapter);
        setupautoCompleteTextView2(adapter);
        handleAutosave();
    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>NO SERVER RESPONSE</font>"));
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
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnPickup1Cont = (Button) findViewById(R.id.btnPickup1Cont);
        btnPickup1Cont.getBackground().setAlpha(255);
        btnPickup1Cancel = (Button) findViewById(R.id.btnPickup1Cancel);
        btnPickup1Cancel.getBackground().setAlpha(255);
        if (progBarPickup1 == null) {
            progBarPickup1 = (ProgressBar) this
                    .findViewById(R.id.progBarPickup1);
        }
        progBarPickup1.setVisibility(View.INVISIBLE);
    }

    private TextWatcher originTextWatcher = new TextWatcher()
    {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            fromLocationBeingTyped = true;
            /*
             * if (autoCompleteTextView1.getText().toString().length() >= 3) {
             * getOriginLocationDetails(); }
             */
        }
    };

    private TextWatcher destinationTextWatcher = new TextWatcher()
    {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            toLocationBeingTyped = true;
            /*
             * if (autoCompleteTextView2.getText().toString().length() >= 3) {
             * getDestinationLocationDetails(); }
             */
        }
    };

    public void handleAutosave() {
        System.out.println("called handleAutosave");
        if (this.autosave != null) {
            currentlyRestoringAutosave = true;
            // System.out.println
            // ("handleAutosave trying to restore from autosave:"+this.autosave.ge;
            nuxractivity = String.valueOf(this.autosave.getNuxractivity());
            this.autoCompleteTextView1.setAdapter(null);
            autoCompleteTextView1.setText(this.autosave.getLocationEntry());
            this.tvDescript1.setText(this.autosave.getDescript());
            
            this.autoCompleteTextView2.setAdapter(null);
            autoCompleteTextView2.setText(this.autosave.getLocationToEntry());
            this.tvDescript2.setText(this.autosave.getDescriptto());
            
            descriptfrom =  this.autosave.getDescript();
            System.out
                    .println("handleAutosave trying to restore from autosave:"
                            + this.autosave.getDescript());
            this.tvOffice1.setText(this.autosave.getCdrespctrhd());
            System.out
                    .println("handleAutosave trying to restore from autosave:"
                            + this.autosave.getCdrespctrhd());
            cdlocatfrom = this.autosave.getCdlocat();
            cdloctypefrom = this.autosave.getCdloctype();
            cdrespctrhdfrom = this.autosave.getCdrespctrhd();
            adstreet1from = this.autosave.getAdstreet1();
            adcityfrom = this.autosave.getAdcity();
            adstatefrom = this.autosave.getAdstate();
            adzipcodefrom = this.autosave.getAdzipcode();
            cdlocatto = this.autosave.getCdlocatto();
            cdloctypeto = this.autosave.getCdloctypeto();
            cdrespctrhdto = this.autosave.getCdrespctrhdto();
            adstreet1to = this.autosave.getAdstreet1to();
            adcityto = this.autosave.getAdcityto();
            adstateto = this.autosave.getAdstateto();
            adzipcodeto = this.autosave.getAdzipcodeto();
            descriptto =  this.autosave.getDescriptto();
            if (this.autosave.getLocationEntry() != null
                    && this.autosave.getLocationEntry().trim().length() > 0) {
                getOriginLocationDetails();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        autoCompleteTextView1.getWindowToken(), 0);
            }
            if (this.autosave.getLocationToEntry() != null
                    && this.autosave.getLocationToEntry().trim().length() > 0) {
                getDestinationLocationDetails();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        autoCompleteTextView2.getWindowToken(), 0);
            }
            System.out.println("Autosave Activity:"
                    + this.autosave.getNaactivity());
            if (!this.autosave.getNaactivity().equalsIgnoreCase(className)) {
                System.out.println("Autosave Continue to ");
                continueButton(null);
            }
            System.out
                    .println("handleAutosave trying to restore from autosave:"
                            + this.autosave.getCdlocat());
        }
        currentlyRestoringAutosave = false;
    }

    public void continueButton(View view) {
        // For testing...
        // SessionManager.getSessionManager().checkServerResponse(true) == OK
        if (checkServerResponse(true) == OK) {
            int duration = Toast.LENGTH_SHORT;
            String currentFromLocation = this.autoCompleteTextView1.getText()
                    .toString();
            String currentToLocation = this.autoCompleteTextView2.getText()
                    .toString();

            if (currentFromLocation.trim().length() == 0) {
                Toast toast = Toast.makeText(this.getApplicationContext(),
                        "!!ERROR: You must first pick a from location.",
                        duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                boolean focusRequested = autoCompleteTextView1.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

            } else if (allLocations.indexOf(currentFromLocation) == -1) {
                Toast toast = Toast.makeText(this.getApplicationContext(),
                        "!!ERROR: From Location Code \"" + currentFromLocation
                                + "\" is invalid.", duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                boolean focusRequested = autoCompleteTextView1.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

            } else if (currentToLocation.trim().length() == 0) {
                Toast toast = Toast
                        .makeText(this.getApplicationContext(),
                                "!!ERROR: You must first pick a to location.",
                                duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                boolean focusRequested = autoCompleteTextView2.requestFocus();

            } else if (allLocations.indexOf(currentFromLocation) == -1) {
                Toast toast = Toast.makeText(this.getApplicationContext(),
                        "!!ERROR: To Location Code \"" + currentToLocation
                                + "\" is invalid.", duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                boolean focusRequested = autoCompleteTextView2.requestFocus();

            } else if (currentToLocation.equalsIgnoreCase(currentFromLocation)) {
                Toast toast = Toast
                        .makeText(
                                this.getApplicationContext(),
                                "!!ERROR: The Pickup Location \""
                                        + currentToLocation
                                        + "\" cannot be the same as the Delivery Location.",
                                duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                boolean focusRequested = autoCompleteTextView2.requestFocus();

            } else if (Integer.valueOf(tvCount1.getText().toString()) < 1) {
                Toast toast = Toast.makeText(this,
                        "!!ERROR: Origin Location must have at least one item",
                        duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            } else {
                btnPickup1Cont.getBackground().setAlpha(70);
                System.out.println("AutoSaving....");
                autoSaveNow();
                Intent intent = new Intent(this, Pickup2Activity.class);
                origin = locations.get(allLocations.indexOf(currentFromLocation));
                destination = locations.get(allLocations.indexOf(currentToLocation));
                intent.putExtra("origin", origin);
                intent.putExtra("destination", destination);
                startActivity(intent);
                overridePendingTransition(R.anim.in_right, R.anim.out_left);
            }
        }
    }

    public void autoSaveNow() {

        if (autosave == null) {
            System.out.println("in autoSave");

            try {
                ContentValues values = new ContentValues();
                values.put("naactivity", className);
                values.put("nuxracttype", "2");
                values.put("dttxnorigin",
                        MenuActivity.invSaveDB.getNow());
                values.put("natxnorguser", LoginActivity.nauser);
                values.put("dttxnupdate",
                        MenuActivity.invSaveDB.getNow());
                values.put("natxnupduser", LoginActivity.nauser);

                long rowid = MenuActivity.invSaveDB.insert(
                        "AM12ACTIVITY", values);
                nuxractivity = String.valueOf(rowid);
                System.out.println("nuxractivity SET TO "
                        + nuxractivity + " FROM " + rowid);

                values = new ContentValues();
                values.put("nuxractivity", rowid);
                values.put("locationfromentry", this.autoCompleteTextView1
                        .getText().toString());
                values.put("cdlocatfrom", cdlocatfrom);
                values.put("cdloctypefrom", cdloctypefrom);
                values.put("cdrespctrhdfrom", cdrespctrhdfrom);
                values.put("adstreet1from", adstreet1from);
                values.put("adcityfrom", adcityfrom);
                values.put("adstatefrom", adstatefrom);
                values.put("adzipcodefrom", adzipcodefrom);
                values.put("descriptfrom", descriptfrom);
                values.put("locationtoentry", this.autoCompleteTextView2
                        .getText().toString());
                values.put("cdlocatto", cdlocatto);
                values.put("cdloctypeto", cdloctypeto);
                values.put("cdrespctrhdto", cdrespctrhdto);
                values.put("adstreet1to", adstreet1to);
                values.put("adcityto", adcityto);
                values.put("adstateto", adstateto);
                values.put("adzipcodeto", adzipcodeto);
                values.put("descriptto", descriptto);
                values.put("dttxnorigin", MenuActivity.invSaveDB.getNow());
                values.put("natxnorguser", LoginActivity.nauser);
                values.put("dttxnupdate", MenuActivity.invSaveDB.getNow());
                values.put("natxnupduser", LoginActivity.nauser);
                System.out.println("autosave locationfromentry:"
                        + this.autoCompleteTextView1.getText().toString()
                        + ", cdlocatfrom:" + cdlocatfrom + ", adstreet1from:"
                        + adstreet1from + ", cdlocatto:" + cdlocatto
                        + ", locationtoentry:"
                        + this.autoCompleteTextView2.getText().toString()
                        + ", adstreet1to:" + adstreet1to 
                        );

                long rowid2 = MenuActivity.invSaveDB.insert("Am12pickup",
                        values);
                nuxractivity = String.valueOf(rowid);
                System.out.println("autosave nuxractivity SET TO "
                        + nuxractivity + " FROM " + rowid + " NUXRPICKUPLOC:"
                        + rowid2);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void cancelButton(View view) {
        btnPickup1Cancel.getBackground().setAlpha(70);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_pickup1, menu);
        return true;
    }

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case LOCCODELIST_TIMEOUT:
            if (resultCode == RESULT_OK) {
                try {
                    // TODO: RequestDispatcher.getInstance() as parameter for
                    // testing.
                    getAllLocations();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        case FROMLOCATIONDETAILS_TIMEOUT:
            if (resultCode == RESULT_OK) {
                if (fromLocationBeingTyped) {
                    autoCompleteTextView1.setText(autoCompleteTextView1
                            .getText());
                    autoCompleteTextView1.setSelection(autoCompleteTextView1
                            .getText().length());
                } else {
                    getOriginLocationDetails();
                    autoCompleteTextView2.requestFocus();
                }
                break;
            }
        case TOLOCATIONDETAILS_TIMEOUT:
            if (resultCode == RESULT_OK) {
                if (toLocationBeingTyped) {
                    autoCompleteTextView2.setText(autoCompleteTextView2
                            .getText());
                    autoCompleteTextView2.setSelection(autoCompleteTextView2
                            .getText().length());
                } else {
                    getDestinationLocationDetails();
                    new Timer().schedule(new TimerTask()
                    {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    autoCompleteTextView2.getWindowToken(), 0);
                        }
                    }, 50);
                }
                break;
            }

        }
    }

    public ArrayList<String> getAllLocations() throws InterruptedException,
            ExecutionException, JSONException {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (LoginActivity.properties != null) {
                URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                        .toString();
            } else {
                MsgAlert msgAlert = new MsgAlert(
                        this,
                        "Properties cannot be loaded.",
                        "!!ERROR: Cannot load properties information. The app is no longer reliable. Please close the app and start again.");
            }
            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocCodeList");
            res = resr1.get();
            if (res == null) {
                noServerResponse();
            } else if (res.indexOf("Session timed out") > -1) {
                startTimeout(LOCCODELIST_TIMEOUT);
            }

            locations = TransactionParser.parseMultipleLocations(res);
            for (Location loc: locations) {
                allLocations.add(loc.getLocationSummaryString());
            }
        }
        return allLocations;
    }

    public void getOriginLocationDetails() {
        originSummary = autoCompleteTextView1.getText().toString().trim();
        String locCode = originSummary.split("-")[0];

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocationDetails?barcode_num=" + locCode);
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(FROMLOCATIONDETAILS_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                try {
                    cdlocatfrom = null;
                    cdloctypefrom = null;
                    cdrespctrhdfrom = null;
                    adstreet1from = null;
                    adcityfrom = null;
                    adstatefrom = null;
                    adzipcodefrom = null;
                    nucountfrom = null;

                    System.out.println("ORIGINAL res:" + res);

                    JSONObject object = (JSONObject) new JSONTokener(res)
                            .nextValue();
                    cdlocatfrom = object.getString("cdlocat");
                    cdloctypefrom = object.getString("cdloctype");
                    cdrespctrhdfrom = object.getString("cdrespctrhd");
                    adstreet1from = object.getString("adstreet1").replaceAll(
                            "&#34;", "\"");
                    System.out.println("adstreet1from:"+adstreet1from);
                    adcityfrom = object.getString("adcity").replaceAll("&#34;",
                            "\"");
                    adstatefrom = object.getString("adstate").replaceAll(
                            "&#34;", "\"");
                    adzipcodefrom = object.getString("adzipcode").replaceAll(
                            "&#34;", "\"");
                    nucountfrom = object.getString("nucount");

                    tvOffice1.setText(cdrespctrhdfrom);
                    // tvLocCd1.setText( object.getString("cdlocat"));
                    tvDescript1.setText(adstreet1from + " ," + adcityfrom
                            + ", " + adstatefrom + " " + adzipcodefrom);
                    descriptfrom =  tvDescript1.getText().toString();

                    tvCount1.setText(nucountfrom);

                } catch (JSONException e) {
                    tvOffice1.setText("!!ERROR: " + e.getMessage());
                    tvDescript1.setText("Please contact STS/BAC.");
                    tvCount1.setText("N/A");
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void getDestinationLocationDetails() {
        destinationSummary = autoCompleteTextView2.getText().toString().trim();
        String locCode = destinationSummary.split("-")[0];

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocationDetails?barcode_num=" + locCode);
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(TOLOCATIONDETAILS_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }

                try {
                    cdlocatto = null;
                    cdloctypeto = null;
                    cdrespctrhdto = null;
                    adstreet1to = null;
                    adcityto = null;
                    adstateto = null;
                    adzipcodeto = null;
                    nucountto = null;
                    System.out.println("DESTINATION res:" + res);

                    JSONObject object = (JSONObject) new JSONTokener(res)
                            .nextValue();
                    cdlocatto = object.getString("cdlocat");
                    cdloctypeto = object.getString("cdloctype");
                    cdrespctrhdto = object.getString("cdrespctrhd");
                    adstreet1to = object.getString("adstreet1").replaceAll(
                            "&#34;", "\"");
                    System.out.println("adstreet1to:"+adstreet1to);
                    
                    adcityto = object.getString("adcity").replaceAll("&#34;",
                            "\"");
                    adstateto = object.getString("adstate").replaceAll("&#34;",
                            "\"");
                    adzipcodeto = object.getString("adzipcode").replaceAll(
                            "&#34;", "\"");
                    nucountto = object.getString("nucount");
                    tvOffice2.setText(cdrespctrhdto);
                    // tvLocCd2.setText( object.getString("cdlocat"));
                    tvDescript2.setText(adstreet1to + " ," + adcityto + ", "
                            + adstateto + " " + adzipcodeto);
                    descriptto =  tvDescript2.getText().toString();
                    
                    tvCount2.setText(nucountto);

                } catch (JSONException e) {
                    tvDescript2.setText("!!ERROR: " + e.getMessage());
                    descriptto =  tvDescript2.getText().toString();
                    tvOffice2.setText("Please contact STS/BAC.");
                    tvCount2.setText("N/A");
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupautoCompleteTextView1(ArrayAdapter<String> adapter) {
        autoCompleteTextView1.setThreshold(1);
        autoCompleteTextView1.setAdapter(adapter);
        autoCompleteTextView1
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        Log.i("ItemClicked", "ITEM CLICKED");
                        if (autoCompleteTextView1.getText().toString().trim()
                                .length() > 0) {
                            getOriginLocationDetails();
                        }
                        if (autoCompleteTextView2.getText().toString().trim()
                                .length() > 0) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    autoCompleteTextView1.getWindowToken(), 0);
                        } else {
                            boolean focusRequested = autoCompleteTextView2
                                    .requestFocus();
                        }
                        fromLocationBeingTyped = false;
                    }
                });

        autoCompleteTextView1.addTextChangedListener(originTextWatcher);
    }

    private void setupautoCompleteTextView2(ArrayAdapter<String> adapter) {
        autoCompleteTextView2.setThreshold(1);
        autoCompleteTextView2.setAdapter(adapter);
        autoCompleteTextView2
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        if (autoCompleteTextView2.getText().toString().trim()
                                .length() > 0) {
                            getDestinationLocationDetails();
                        }

                        int duration = Toast.LENGTH_SHORT;
                        toLocationBeingTyped = false;
                        if (autoCompleteTextView1.getText().toString().trim()
                                .length() == 0) {
                            boolean focusRequested = autoCompleteTextView1
                                    .requestFocus();
                            Toast toast = Toast.makeText(
                                    getApplicationContext(),
                                    "Please pick a from location.", duration);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        } else {
                            if (autoCompleteTextView1.getText().toString()
                                    .trim().length() > 0) {
                                if (autoCompleteTextView1.getText().toString()
                                        .trim().length() > 0) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(
                                            autoCompleteTextView1
                                                    .getWindowToken(), 0);
                                } else {

                                }
                            } else {
                                boolean focusRequested = autoCompleteTextView1
                                        .requestFocus();
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(0,
                                        InputMethodManager.SHOW_IMPLICIT);
                            }

                        }
                    }
                });

        autoCompleteTextView2.addTextChangedListener(destinationTextWatcher);
    }

}
